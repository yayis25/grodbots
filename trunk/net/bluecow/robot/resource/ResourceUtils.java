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
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import net.bluecow.robot.RobotUtils;

/**
 * A collection of utility methods that operate on resource loaders
 * and resource managers.
 *
 * @author fuerth
 * @version $Id$
 */
public class ResourceUtils {

    /**
     * Controls debugging features of this class.
     */
    private static final boolean debugOn = true;
    
    /**
     * Prints the given message to the system console.
     */
    private static final void debug(String msg) {
        if (debugOn) System.out.println(msg);
    }
    
    /**
     * This class is not instantiable.
     */
    private ResourceUtils() {
        // there will be no instances of this class
    }
    
    /**
     * Packs all resources in the given resource manager into a JAR file.
     * 
     * @param rm The resource manager whose contents should be jarred
     * @param location The location to store the created JAR file
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

}
