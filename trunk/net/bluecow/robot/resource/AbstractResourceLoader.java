/*
 * Created on Mar 20, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.resource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The AbstractResourceLoader is a useful starting point for implementing
 * the ResourceLoader interface.  It defines a getResourceBytes() implementation
 * which depends on the abstract getResourceAsStream() method.  Therefore,
 * when you extend this class you only need to implement getResourceAsStream().
 *
 * @author fuerth
 * @version $Id$
 */
public abstract class AbstractResourceLoader implements ResourceLoader {

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
