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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class JarResourceManager extends AbstractResourceManager {

    /**
     * Controls the debugging features of this class.
     */
    private static final boolean debugOn = false;
    
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
     * The temporary directory where the jar file contents have been
     * unpacked so we can manipulate them.
     */
    private File dir;
    
    /**
     * Once the JAR file has been unpacked, this DirectoryResourceManager does all the
     * heavy lifting for us.
     */
    private final DirectoryResourceManager delegate;

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
        delegate = new DirectoryResourceManager(dir);
    }

    /**
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
        
        delegate = new DirectoryResourceManager(dir);
    }
    
    
    // ------------------ Interface Methods (delegated to DirectoryResourceManager) ---------------------
    
    public void close() throws IOException {
        delegate.close();
        super.close();
        recursiveRmdir(dir);
    }
    
    public void createDirectory(String targetDir, String newDirName) throws IOException {
        delegate.createDirectory(targetDir, newDirName);
    }

    public InputStream getResourceAsStream(String resourceName) throws IOException {
        return delegate.getResourceAsStream(resourceName);
    }

    public List<String> list(String path, ResourceNameFilter filter) throws IOException {
        return delegate.list(path, filter);
    }

    public List<String> listAll(ResourceNameFilter filter) throws IOException {
        return delegate.listAll(filter);
    }

    public OutputStream openForWrite(String path, boolean create) throws IOException {
        return delegate.openForWrite(path, create);
    }

    public void remove(String path) throws IOException {
        delegate.remove(path);
    }

    public boolean resourceExists(String path) {
        return delegate.resourceExists(path);
    }    
    

    // ------------------ Helper Methods ----------------------

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

}
