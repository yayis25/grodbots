/*
 * Created on Sep 25, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipFileResourceLoader extends ResourceLoader {

    private ZipFile zipFile;
    
    public ZipFileResourceLoader(File file) throws ZipException, IOException {
        zipFile = new ZipFile(file);
    }
    
    @Override
    public InputStream getResourceAsStream(String resourceName) throws IOException {
        ZipEntry resource = zipFile.getEntry(resourceName);
        if (resource == null) {
            throw new FileNotFoundException(
                    "resource '"+resourceName+"' not found in zip file "+zipFile.getName());
        }
        return zipFile.getInputStream(resource);
    }

}
