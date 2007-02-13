/*
 * Created on Sep 26, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.robot.editor;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import net.bluecow.robot.GameConfig;
import net.bluecow.robot.LevelConfig;
import net.bluecow.robot.LevelStore;
import net.bluecow.robot.Robot;
import net.bluecow.robot.LevelConfig.Switch;
import net.bluecow.robot.resource.DirectoryResourceLoader;
import net.bluecow.robot.resource.ResourceLoader;

public class Project {

    private static final boolean debugOn = false;
    
    /**
     * The location of this project in the file system.
     */
    private File dir;
    
    /**
     * The game config associated with this project.
     */
    private GameConfig gameConfig;
    
    /**
     * The prototype robot for creating new ones.  One day, this will
     * be modifiable by the user and get saved with the project.
     */
    private Robot defaultRobot = new Robot(
            "new_robot", "Grod", null, new ArrayList<GameConfig.SensorConfig>(),
            new ArrayList<GameConfig.GateConfig>(), null,
            new Point2D.Float(0.5f, 0.5f), 0.1f, null, 1);
    
    private Switch defaultSwitch = new LevelConfig.Switch(
            new Point(1, 1), "new_switch", "", null, null);
    
    /**
     * Creates a new project with a default empty level.  This operation creates
     * the project's directory and its ROBO-INF subdirectory.
     * 
     * @param dir The directory that will hold this project.  It must not exist yet.
     * @return The new project.
     * @throws IOException If the directory already exists, or it can't be created.
     */
    public static Project createNewProject(File dir) throws IOException {
        if (dir.exists()) {
            throw new IOException(
                    "Directory "+dir.getAbsolutePath()+" already exists.");
        }
        File newRoboInfDir = new File(dir, "ROBO-INF");
        if (!newRoboInfDir.mkdirs()) {
            throw new IOException(
                    "Failed to create project config dir "+
                    newRoboInfDir.getAbsolutePath());
        }
        
        // now populate with the default resource collection
        JarInputStream defaultResources =
            new JarInputStream(ClassLoader.getSystemResourceAsStream(
                    "net/bluecow/robot/default_resources.jar"));
        JarEntry resource;
        while ( (resource = defaultResources.getNextJarEntry()) != null ) {
            String path = resource.getName();
            File resourceFile = new File(dir, path);
            if (resource.isDirectory()) {
                debug("Creating resource directory "+resourceFile.getAbsolutePath());
                resourceFile.mkdir();
            } else {
                debug("Creating resource file "+resourceFile.getAbsolutePath());
                OutputStream out = new FileOutputStream(resourceFile);
                byte[] buffer = new byte[1024];
                int len;
                while ( (len = defaultResources.read(buffer)) != -1 ) {
                    out.write(buffer, 0, len);
                }
                out.flush();
                out.close();
            }
        }
        defaultResources.close();
        
        Project proj = load(dir);
        
        return proj;
    }
    
    public static Project load(File dir) throws IOException {
        ResourceLoader resourceLoader = new DirectoryResourceLoader(dir);
        Project proj = new Project();
        proj.gameConfig = LevelStore.loadLevels(resourceLoader);
        proj.dir = dir;
        return proj;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }
    
    /**
     * Returns a list of all resources that currently exist in this
     * project's directory.
     */
    public List<String> getAllResourceNames() {
        return recursiveListResources("", dir, new ArrayList<String>());
    }
    
    /**
     * Recursive subroutine that appends the names of all files
     * at and below the given directory.
     * 
     * @param resources The list to append to.
     * @return The resources list.
     */
    private List<String> recursiveListResources(String pathName, File dir, List<String> resources) {
        File[] files = dir.listFiles();
        Arrays.sort(files);
        for (File file : files) {
            String newPath;
            if (pathName.length() == 0) {
                newPath = file.getName();  // this prevents a leading slash in entry name
            } else {
                newPath = pathName + "/" + file.getName();
            }
            if (file.isDirectory()) {
                recursiveListResources(newPath, file, resources);
            } else {
                resources.add(newPath);
            }
        }
        return resources;
    }

    /**
     * Saves this project, then bundles all its resources into
     * a single JAR file.
     * 
     * @param location the file to save into (doesn't have to exist yet)
     * @throws IOException If there are any problems during the save operation
     */
    public void saveLevelPack(File location) throws IOException {
        save();
        JarOutputStream jout = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(location)));
        recursiveSaveFilesToJar(jout, dir, "");
        jout.flush();
        jout.close();
    }
    
    /**
     * Saves the files associated with the project under this project's current
     * directory. To export a single JAR file that contains the whole project,
     * use {@link #saveLevelPack(File)}.
     * @throws IOException if there are any problems saving the resources
     */
    public void save() throws IOException {
        String encoding = "utf-8";
        File destFile = new File(dir, LevelStore.DEFAULT_MAP_RESOURCE_PATH);
        LevelStore.save(destFile, getGameConfig(), encoding);
    }

    /**
     * Recursive subroutine that puts all files and directories under
     * basedir into the given jar output stream.
     * 
     * @param out An open JarOutputStream.  It will not be closed by this method.
     * @param baseDir The base location in the file system which will become the
     * root of the jar's file entries.
     * @param path The current path, both within the jar and under baseDir.  Path
     * elements are separated by the forward slash '/' character.
     * @throws IOException If there is trouble with either the input or output
     * files.
     */
    private void recursiveSaveFilesToJar(JarOutputStream out, File baseDir, String path) throws IOException {
        File thisDir = new File(baseDir, path);
        debug("JAR: starting dir "+thisDir);
        out.putNextEntry(new JarEntry(path + "/"));
        for (String subPath : thisDir.list()) {
            File f = new File(thisDir, subPath);
            debug("     entry "+f);
            if (f.isDirectory()) {
                String newPath;
                if (path.length() == 0) {
                    newPath = subPath;  // this prevents a leading slash in entry name
                } else {
                    newPath = path + "/" + subPath;
                }
                recursiveSaveFilesToJar(out, baseDir, newPath);
            } else {
                InputStream in = new BufferedInputStream(new FileInputStream(f));
                debug("     adding to jar "+path + "/" + subPath);
                out.putNextEntry(new JarEntry(path + "/" + subPath));
                byte[] buf = new byte[4096];
                int count;
                while ((count = in.read(buf)) != -1) {
                    out.write(buf, 0, count);
                }
                in.close();
            }
        }
    }
    
    /**
     * Creates a new Robot which has properties the same as the default
     * robot.
     * 
     * @return the new robot instance.
     */
    public Robot createRobot(LevelConfig targetLevel) {
        Robot r = new Robot(defaultRobot, targetLevel);
        return r;
    }
    
    public Switch createSwitch() {
        return new LevelConfig.Switch(defaultSwitch);
    }
    
    private static void debug(String msg) {
        if (debugOn) System.out.println(msg);
    }
}
