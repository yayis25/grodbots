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
 * Created on Mar 27, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.resource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import net.bluecow.robot.RobotUtils;
import net.bluecow.robot.resource.url.ResourceURLStreamHandler;
import net.bluecow.robot.resource.url.ResourceURLStreamHandlerFactory;

/**
 * A collection of utility methods that operate on resource loaders
 * and resource managers.
 *
 * @author fuerth
 */
public class ResourceUtils {

    /**
     * Controls debugging features of this class.
     */
    private static final boolean debugOn = false;
    
    /**
     * Prints the given message to the system console.
     */
    private static final void debug(String msg) {
        if (debugOn) System.out.println(msg);
    }
    
    private static ResourceURLStreamHandler resourceURLStreamHandler;
    
    /**
     * This class is not instantiable.
     */
    private ResourceUtils() {
        // there will be no instances of this class
    }
    
    /**
     * Initializes the systemwide URL resolution system to recognize the
     * URL protocol "resource:" and provide URL content via the given
     * ResourceLoader instance.
     * <p>
     * Due to rules in Java's URL API, this method will only succeed if
     * nothing has yet tried to call URL.setURLStreamHandlerFactory().
     * Also, unfortunately, this has to be done in a static manner, so
     * it is not directly possible to provide multiple namespaces for
     * resource loader URL resolution.  If this becomes a problem, one
     * way to solve it would be to associate each resource loader instance
     * with a unique String value, then use the hostname part of the URL
     * (it's currently not in use) to specify which resource loader to use.
     */
    public static void initResourceURLHandler(ResourceLoader loader) {
        if (resourceURLStreamHandler == null) {
            resourceURLStreamHandler = new ResourceURLStreamHandler(loader);
            ResourceURLStreamHandlerFactory factory =
                new ResourceURLStreamHandlerFactory("resource", resourceURLStreamHandler);
            URL.setURLStreamHandlerFactory(factory);
        } else {
            resourceURLStreamHandler.setResourceLoader(loader);
        }
    }
    /**
     * Packs all resources in the given resource manager into a JAR file.
     * 
     * @param rm
     *            The resource manager whose contents should be jarred
     * @param location
     *            The location to store the created JAR file
     */
    public static void createResourceJar(ResourceManager rm, File location) throws IOException {
        JarOutputStream jout = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(location)));
        List<String> resources = rm.listAll();
        debug("About to create resource JAR. Entries:\n" + RobotUtils.listOnSeparateLines(resources));
        for (String path : resources) {
            jout.putNextEntry(new JarEntry(path));
            if (!path.endsWith("/")) {
                InputStream in = new BufferedInputStream(rm.getResourceAsStream(path));
                byte[] buf = new byte[4096];
                int count;
                while ((count = in.read(buf)) != -1) {
                    jout.write(buf, 0, count);
                }
                in.close();
            }
        }
        jout.flush();
        jout.close();
    }

    /**
     * Creates a ResourceNameFilter which only accepts directory entries.
     */
    public static ResourceNameFilter directoryOnlyFilter() {
        return new RegexResourceNameFilter(".*/", false);
    }
    
    /**
     * Copies the entire contents that can be read from the given input stream
     * into the given output stream.
     * 
     * @param in
     *            The input stream to copy from. All contents starting at the
     *            current position will be read, and if this method returns
     *            normally, the input stream will be positioned at its
     *            end-of-file.
     * @param out
     *            The output stream to copy to. All bytes read from the input
     *            stream will be written to this stream.
     * @return The number of bytes written.
     * @throws IOException
     *             If there is a problem either reading or writing the given
     *             streams.
     */
    public static long copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4096];
        long total = 0;
        int i;
        while ( (i = in.read(buf)) >= 0) {
            out.write(buf, 0, i);
            total += i;
        }
        return total;
    }

    /**
     * Copies all bytes that can be read from the given input stream into the
     * given file.
     * 
     * @param in
     *            The input stream to read from. All contents starting at the
     *            current position will be read, and if this method returns
     *            normally, the input stream will be positioned at its
     *            end-of-file.
     * @param file
     *            The file to copy to. If it exists, it will be overwritten.
     * @throws IOException
     *             If there are any problems creating or truncating the given
     *             output file, writing to the output file, or reading the given
     *             input stream. In this case, the output file may or may not
     *             have been created or truncated, and the input stream may or
     *             may not have been read to end-of-file.
     */
    public static void copyToFile(InputStream in, File file) throws IOException {
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            ResourceUtils.copy(in, out);
            out.flush();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                    System.err.println("Couldn't close output stream.  Squashing this exception:");
                    ex.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Creates the given directory path and any intermediate directories
     * as required.  No error will be reported if the given path already
     * exists as a directory.
     * 
     * @param resourceManager The resource manager in which to create
     * the given directories
     * @param path The directory or directories to create
     * @throws IOException If the resource manager cannot create the specified
     * path as a directory
     */
    public static void mkdirs(ResourceManager resourceManager, String path) throws IOException {
        if (path.endsWith("/")) {
            throw new IllegalArgumentException("Illegal path \""+path+"\" (must not end with slash)");
        }
        if (resourceManager.resourceExists(path)) return;
        String parentPath = path.substring(0, path.lastIndexOf('/'));
        mkdirs(resourceManager, parentPath);
        String newDirName = path.substring(path.lastIndexOf('/') + 1);
        resourceManager.createDirectory(parentPath, newDirName);
    }

    public static List<String> recursiveListResources(File dir, ResourceNameFilter filter) {
        return recursiveListResources("", dir, filter, new ArrayList<String>());
    }
    
    /**
     * Recursive subroutine that appends the names of all files
     * at and below the given directory.
     * 
     * @param resources The list to append to.
     * @return The <code>resources</code> list.
     */
    private static List<String> recursiveListResources(
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
}
