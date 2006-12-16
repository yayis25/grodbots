/*
 * Created on Sep 25, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.resource;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The ResourceLoader is the abstract base class for locating and retrieving
 * resources from a hierarchical namespace.  Concrete implementations will get
 * resources from different places, including jar files, the system classpath,
 * the local filesystem, etc.
 * 
 * <p>In all cases, resource names are paths relative to the root of whatever
 * resource store the implementation uses.  For example, a resource namespace
 * might be rooted at a particular directory, URL, or ZIP file.  Resource path
 * name separators are always the forward slash character <tt>/</tt>.
 *
 * @author fuerth
 * @version $Id$
 */
public abstract class ResourceLoader {
    
    /**
     * Retrieves the contents of the named resource from this resource loader's
     * storage system.
     * 
     * @param resourceName
     * @return An InputStream that is ready to deliver all the bytes of the
     * named resource.
     * @throws FileNotFoundException If the named resource does not exist
     * @throws IOException If another I/O error occurs while retrieving the resource
     */
    public abstract InputStream getResourceAsStream(String resourceName) throws IOException;
    
    public byte[] getResourceBytes(String resourceName) throws IOException {
        List<Byte> bytes = new ArrayList<Byte>();
        InputStream in = new BufferedInputStream(getResourceAsStream(resourceName));
        int ch;
        while ((ch = in.read()) != -1) {
            bytes.add(new Byte((byte) ch));
        }
        in.close();
        byte[] byteArray = new byte[bytes.size()];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = bytes.get(i);
        }
        return byteArray;
    }
}
