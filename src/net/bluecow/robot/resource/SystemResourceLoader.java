/*
 * Created on Sep 25, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.robot.resource;

import java.io.IOException;
import java.io.InputStream;

public class SystemResourceLoader extends AbstractResourceLoader {

    public InputStream getResourceAsStream(String resourceName) throws IOException {
        InputStream resourceStream = ClassLoader.getSystemResourceAsStream(resourceName);
        if (resourceStream == null) {
            throw new IOException("Can't locate resource '"+resourceName+
                    "' on system classpath");
        }
        return resourceStream;
    }

}
