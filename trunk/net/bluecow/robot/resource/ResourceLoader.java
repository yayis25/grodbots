/*
 * Created on Sep 25, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * ResourceLoader is the interface for locating and retrieving resources from a
 * hierarchical namespace. Implementations will get resources from different
 * places, including jar files, the system classpath, the local filesystem, etc.
 * 
 * <p>
 * In all cases, resource names are paths relative to the root of whatever
 * resource store the implementation uses. For example, a resource namespace
 * might be rooted at a particular directory, URL, or ZIP file. Resource path
 * name separators are always the forward slash character <tt>/</tt>.
 * 
 * @author fuerth
 * @version $Id$
 */
public interface ResourceLoader {
    
    /**
     * Retrieves the contents of the named resource from this resource loader's
     * storage system as an input stream.
     * 
     * @param resourceName the path (relative to this resource loader's base)
     * of the requested resource.  Path elements must be separated by forward
     * slash, regardless of the local native platform's custom.
     * @return An InputStream that is ready to deliver all the bytes of the
     * named resource.
     * @throws FileNotFoundException If the named resource does not exist
     * @throws IOException If another I/O error occurs while retrieving the resource
     */
    public InputStream getResourceAsStream(String resourceName) throws IOException;
    
    /**
     * Retrieves the contents of the named resource from this resource loader's
     * storage system as an array of bytes.
     * 
     * @param resourceName the path (relative to this resource loader's base)
     * of the requested resource.  Path elements must be separated by forward
     * slash, regardless of the local native platform's custom.
     * @return An array of bytes which contains the requested resource's data.
     * @throws FileNotFoundException If the named resource does not exist
     * @throws IOException If another I/O error occurs while retrieving the resource
     */
    public byte[] getResourceBytes(String resourceName) throws IOException;
}
