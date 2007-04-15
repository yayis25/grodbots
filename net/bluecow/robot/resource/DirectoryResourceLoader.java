/*
 * Created on Sep 26, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.robot.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DirectoryResourceLoader extends AbstractResourceLoader {

    private File basedir;
    
    public DirectoryResourceLoader(File basedir) {
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

}
