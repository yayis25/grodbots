/*
 * Copyright (c) 2007, Jonathan Fuerth
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Jonathan Fuerth nor the names of other
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * Created on Mar 22, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import net.bluecow.robot.resource.event.ResourceManagerListener;

public class JarResourceManager extends AbstractResourceLoader implements ResourceManager {

    /**
     * Controls the debugging features of this class.
     */
    private static final boolean debugOn = true;
    
    /**
     * Prints the given message to System.out if debugOn is true.
     */
    private static void debug(String msg) {
        if (debugOn) System.out.println(msg);
    }
    
    /**
     * A filter that excludes the JAR manifest from the files being extracted.
     * If we extract the manifest and include it in the resource list, then
     * creating a JAR of this resource manager's contents will fail due to
     * a duplicate entry name exception.
     */
    private static ResourceNameFilter jarJunkFilter =
        new RegexResourceNameFilter("META-INF/MANIFEST.MF", true);
    
    /**
     * Indicates whether or not this resource manager has been closed.
     */
    private boolean closed = false;
    
    /**
     * The temporary directory where the jar file contents have been
     * unpacked so we can manipulate them.
     */
    private File dir;

    /**
     * Creates a new ResourceManager whose contents are populated initially from
     * a JAR file in the filesystem.
     * 
     * @param jar The JAR file to extract the initial set of resources from.
     * @throws IOException If there is a problem opening or extracting from
     * the JAR.
     */
    public JarResourceManager(File jar) throws IOException {
        dir = createTempDir();
        unjar(jar, dir);
    }

    /**
     * WARNING: The code for this constructor was transplanted from Project,
     * but due to other circumstances, it is not in use any more. I'm keeping
     * it in case I need to create a resource manager from a jar input stream
     * in the future.
     * <p>
     * Creates a new ResourceManager whose contents are populated initially from
     * a JAR file which is available as a classloader resource.
     * 
     * @param classLoader The class loader to read the resource from
     * @param resourcePath The resource's path within the given classloader's
     * namespace.
     * @throws IOException If there is a problem opening or extracting from
     * the JAR.
     */
    public JarResourceManager(ClassLoader classLoader, String resourcePath) throws IOException {
        dir = createTempDir();
        JarInputStream jin =
            new JarInputStream(classLoader.getResourceAsStream(resourcePath));
        JarEntry je;
        while ( (je = jin.getNextJarEntry()) != null ) {
            String path = je.getName();
            if (!jarJunkFilter.accepts(path)) {
                continue;
            }
            File resourceFile = new File(dir, path);
            if (je.isDirectory()) {
                debug("Creating resource directory "+resourceFile.getAbsolutePath());
                resourceFile.mkdir();
            } else {
                debug("Creating resource file "+resourceFile.getAbsolutePath());
                OutputStream out = new FileOutputStream(resourceFile);
                byte[] buffer = new byte[1024];
                int len;
                while ( (len = jin.read(buffer)) != -1 ) {
                    out.write(buffer, 0, len);
                }
                out.flush();
                out.close();
            }
        }
        jin.close();
    }
    
    
    // ------------------ Interface Methods ----------------------
    
    public List<String> listAll() throws IOException {
        return listAll(null);
    }
    
    public List<String> listAll(ResourceNameFilter filter) throws IOException {
        checkClosed();
        return recursiveListResources("", dir, filter, new ArrayList<String>());
    }

    public List<String> list(String path) {
        return list(path, null);
    }
    
    public List<String> list(String path, ResourceNameFilter filter) {
        File resourceDir = new File(dir, path);
        debug("Listing children of " + resourceDir.getAbsolutePath());
        if (resourceDir.isFile()) {
            debug("It's not a directory");
            return Collections.emptyList();
        }
        
        if (!path.endsWith("/")) {
            path += "/";
        }
        
        File[] children = resourceDir.listFiles();
        Arrays.sort(children);

        List<String> retval = new ArrayList<String>();
        for (File f : children) {
            String resourcePath = path + f.getName();
            if (f.isDirectory() && (!resourcePath.endsWith("/"))) {
                resourcePath += "/";
            }
            
            if (filter == null || filter.accepts(resourcePath)) {
                retval.add(resourcePath);
            }
        }
        
        debug("Children are: " + retval);
        
        return retval;
    }
    
    public OutputStream openForWrite(String path, boolean create) throws IOException {
        checkClosed();
        File resourceFile = new File(dir, path);
        if (!create && !resourceFile.exists()) {
            throw new FileNotFoundException(
                    "Resource \""+path+"\" cannot be written because it" +
                    " does not exist, and I was instructed not to create it.");
        }
        return new FileOutputStream(resourceFile);
    }

    public void remove(String path) throws IOException {
        checkClosed();
        File f = new File(dir, path);
        if (!f.delete()) {
            boolean exists = f.exists();
            boolean canWrite = f.canWrite();
            boolean isDir = f.isDirectory();
            throw new IOException("Couldn't delete resource \""+path+"\" " +
                    "(exists="+exists+", canWrite="+canWrite+", isDir="+isDir+")");
        }
    }

    public InputStream getResourceAsStream(String path) throws IOException {
        checkClosed();
        return new FileInputStream(new File(dir, path));
    }
    
    public void close() {
        recursiveRmdir(dir);
    }
    
    // ------------------ Helper Methods ----------------------
    
    /**
     * Throws an IOException with an appropriate message if this resource manager
     * is closed. Otherwise returns with no side effects.
     */
    private void checkClosed() throws IOException {
        if (closed) throw new IOException("This resource manager is closed");
    }
    
    /**
     * Deletes the given directory and all of its contents, including
     * any nested directories.
     */
    private static void recursiveRmdir(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                recursiveRmdir(f);
            } else {
                f.delete();
            }
        }
        dir.delete();
    }
    
    /**
     * Recursive subroutine that appends the names of all files
     * at and below the given directory.
     * 
     * @param resources The list to append to.
     * @return The resources list.
     */
    private List<String> recursiveListResources(
            String pathName, File dir, ResourceNameFilter filter, List<String> resources) {
        
        File[] files = dir.listFiles();
        Arrays.sort(files);
        
        //debug("rlr: pathName="+pathName+"; files="+Arrays.toString(files));
        
        for (File file : files) {
            String newPath;
            if (pathName.length() == 0) {
                newPath = file.getName();  // this prevents a leading slash in entry name
            } else {
                newPath = pathName + file.getName();
            }
            
            String resourceName;
            if (file.isDirectory()) {
                resourceName = newPath + "/";
            } else {
                resourceName = newPath;
            }
            
            boolean accepted;
            if (filter == null || filter.accepts(resourceName)) {
                accepted = true;
                resources.add(resourceName);
            } else {
                accepted = false;
            }
            
            debug("rlr:   newPath="+newPath+"; resourceName="+resourceName+"; accepted="+accepted);
            
            if (file.isDirectory()) {
                recursiveListResources(resourceName, file, filter, resources);
            }
        }
        return resources;
    }
    
    /**
     * Extracts all the files in the given JAR file to the target
     * directory (and subdirectories thereof).
     * 
     * @param jar The input JAR file
     * @param dir The base directory to store the extracted files into
     * @throws IOException If there are problems either reading the JAR or
     * writing the extracted files
     * @throws FileNotFoundException If the specified JAR file does not exist
     */
    private final static void unjar(File jar, File dir) throws IOException, FileNotFoundException {
        JarFile jf = new JarFile(jar);
        for (Enumeration<JarEntry> e = jf.entries() ; e.hasMoreElements() ;) {
            final JarEntry jarEntry = e.nextElement();
            final File outFile = new File(dir, jarEntry.getName());
            debug("JarEntry: " + jarEntry.getName() + "; outFile: " + outFile);
            if (!jarJunkFilter.accepts(jarEntry.getName())) {
                debug("  skipping (rejected by filter)");
                continue;
            }
            if (jarEntry.isDirectory()) {
                debug("  making dir");
                outFile.mkdirs();
                continue;
            }
            InputStream in = null;
            OutputStream out = null;
            try {
                debug("  extracting file");
                in = jf.getInputStream(jarEntry);
                out = new FileOutputStream(outFile);
                int count;
                byte[] buf = new byte[8192];
                while ( (count = in.read(buf)) > 0 ) {
                    out.write(buf, 0, count);
                }
                out.flush();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        System.err.println("Couldn't close input stream"); ex.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        System.err.println("Couldn't close output stream"); ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Creates a new directory inside the system's "java.io.tmpdir" area.
     * Some attempt is made to ensure the name of the created directory
     * will be unique.
     */
    private static File createTempDir() {
        File dir = new File(System.getProperty("java.io.tmpdir"), "robotmp_"+System.currentTimeMillis());
        dir.mkdir();
        return dir;
    }

    /* docs come from interface */
    public void createDirectory(String targetDir, String newDirName) throws IOException {
        if (newDirName.contains("/")) {
            throw new IOException("New resource directory name not valid: it contains the / character.");
        }
        File parent = new File(dir, targetDir);
        if (!parent.exists()) {
            throw new IOException("Target resource directory \"" + targetDir + "\" does not exist.");
        }
        File newDir = new File(parent, newDirName);
        if (!newDir.mkdir()) {
            throw new IOException("Could not create resource directory \"" + newDirName + "\".");
        }
    }
    
    // ------------- Events! ----------------
    
    private final List<ResourceManagerListener> listeners = new ArrayList<ResourceManagerListener>();
    
    
}
