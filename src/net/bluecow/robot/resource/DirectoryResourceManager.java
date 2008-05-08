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
 * Created on Sep 26, 2006
 *
 * This code belongs to SQL Power Group Inc.
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
import java.util.List;

public class DirectoryResourceManager extends AbstractResourceManager {

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
    
    private File basedir;
    
    public DirectoryResourceManager(File basedir) {
        if (!basedir.isDirectory()) {
            if (!basedir.exists()) {
                throw new IllegalArgumentException(
                        "The given base directory '"+basedir.getAbsolutePath()+
                        "' does not exist");
            } else {
                throw new IllegalArgumentException(
                        "The given base directory '"+basedir.getAbsolutePath()+
                        " is not a directory");
            }
        }
        this.basedir = basedir;
    }
    
    public InputStream getResourceAsStream(String resourceName) throws IOException {
        File resourceFile = new File(basedir, resourceName);
        if (!resourceFile.exists()) {
            throw new FileNotFoundException("Resource file '"+resourceFile.getAbsolutePath()+"' not found");
        }
        if (!resourceFile.canRead()) {
            throw new IOException("Resource file '"+resourceFile.getAbsolutePath()+"' exists but is not readable");
        }
        return new FileInputStream(resourceFile);
    }

    /* docs come from interface */
    public void createDirectory(String targetDir, String newDirName) throws IOException {
        if (!targetDir.endsWith("/")) {
            debug("Appending slash to given targetDir: " + targetDir);
            targetDir += "/";
        }
        if (newDirName.contains("/")) {
            throw new IOException(
                    "New resource directory name \""+newDirName+"\" not valid: it contains the / character.");
        }
        File parent = new File(basedir, targetDir);
        if (!parent.exists()) {
            throw new IOException("Target resource directory \"" + targetDir + "\" does not exist.");
        }
        File newDir = new File(parent, newDirName);
        String newDirPath = targetDir + newDirName;
        if (!newDir.mkdir()) {
            throw new IOException("Could not create resource directory \"" + newDirPath + "\".");
        }
        fireResourceAdded(targetDir, newDirName + "/");
    }

    public List<String> list(String path, ResourceNameFilter filter) throws IOException {
        File resourceDir = new File(basedir, path);
        debug("Listing children of " + resourceDir.getAbsolutePath());
        if (resourceDir.isFile()) {
            debug("It's not a directory");
            return Collections.emptyList();
        }
        
        if (!path.endsWith("/")) {
            path += "/";
        }
        
        // ensure the root directory does not have a leading slash
        if (path.equals("/")) {
            path = "";
        }
        
        File[] children = resourceDir.listFiles();
        if (children == null) {
            throw new IOException("No such resource directory: \""+path+"\"");
        }
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

    public List<String> listAll(ResourceNameFilter filter) throws IOException {
        checkClosed();
        return ResourceUtils.recursiveListResources(basedir, filter);
    }
    
    public OutputStream openForWrite(String path, boolean create) throws IOException {
        checkClosed();
        File resourceFile = new File(basedir, path);
        if (!create && !resourceFile.exists()) {
            throw new FileNotFoundException(
                    "Resource \""+path+"\" cannot be written because it" +
                    " does not exist, and I was instructed not to create it.");
        }
        return new FileOutputStream(resourceFile);
    }

    
    public void remove(String path) throws IOException {
        checkClosed();
        File f = new File(basedir, path);
        if (!f.delete()) {
            boolean exists = f.exists();
            boolean canWrite = f.canWrite();
            boolean isDir = f.isDirectory();
            throw new IOException("Couldn't delete resource \""+path+"\" " +
                    "(exists="+exists+", canWrite="+canWrite+", isDir="+isDir+")");
        }
    }
    

    public boolean resourceExists(String path) {
        File f = new File(basedir, path);
        return f.exists();
    }
}
