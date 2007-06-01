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
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import net.bluecow.robot.RobotUtils;

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
}
