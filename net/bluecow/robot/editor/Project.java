/*
 * Created on Sep 26, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.robot.editor;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import net.bluecow.robot.GameConfig;
import net.bluecow.robot.LevelConfig;
import net.bluecow.robot.LevelStore;
import net.bluecow.robot.Robot;
import net.bluecow.robot.LevelConfig.Switch;
import net.bluecow.robot.resource.JarResourceManager;
import net.bluecow.robot.resource.ResourceManager;
import net.bluecow.robot.resource.ResourceUtils;

public class Project {

    private static final boolean debugOn = false;

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
     * the project's initial JAR file containing the ROBO-INF subdirectory.
     * 
     * @param file The file that will hold this project.  It must not exist yet.
     * @return The new project.
     * @throws IOException If the file already exists, or it can't be created.
     */
    public static Project createNewProject(File file) throws IOException {
        if (file.exists()) {
            throw new IOException(
                    "File "+file.getAbsolutePath()+" already exists.");
        }

        // now populate with the default resource collection
        ResourceManager defaultResources = 
            new JarResourceManager(Project.class.getClassLoader(),
                                   "net/bluecow/robot/default_resources.jar");
        
        defaultResources.close();
        
        Project proj = load(file);
        
        return proj;
    }
    
    /**
     * Creates a new Project instance by loading it from a JAR file which
     * contains files laid out in a special way.
     * 
     * @param jar The JAR file to read the project descirption from.
     * @return
     * @throws IOException
     */
    public static Project load(File jar) throws IOException {
        ResourceManager resources = new JarResourceManager(jar);
        Project proj = new Project();
        proj.gameConfig = LevelStore.loadLevels(resources);
        return proj;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }
    
    /**
     * Returns the resource manager associated with this project.
     */
    public ResourceManager getResourceManager() {
        // note, we created the game config with a resource manager (not just a loader),
        // so this cast will work as long as no one changes its resource loader. 
        return (ResourceManager) gameConfig.getResourceLoader();
    }
    

    /**
     * Saves this project, then bundles all its resources into
     * a single JAR file.
     * 
     * FIXME this should preen the resource set a little bit: remove
     * old map backups, remove example solutions (when we get that working),
     * and whatever else isn't fit for mass distribution.  A good way to
     * implement this would be a file filter that knows which resources
     * to exclude.
     * 
     * @param location the file to save into (doesn't have to exist yet)
     * @throws IOException If there are any problems during the save operation
     */
    public void saveLevelPack(File location) throws IOException {
        save();
        ResourceUtils.createResourceJar(getResourceManager(), location);
    }
    
    /**
     * Saves the current game configuration to this project's resource manager.
     * To export a single JAR file that contains the whole project, use
     * {@link #saveLevelPack(File)}.
     * 
     * @throws IOException
     *             if there are any problems saving the resources
     */
    public void save() throws IOException {
        String encoding = "utf-8";
        OutputStream out = getResourceManager().openForWrite(LevelStore.DEFAULT_MAP_RESOURCE_PATH, true);
        Writer writer = new BufferedWriter(new OutputStreamWriter(out, encoding));
        LevelStore.save(writer, getGameConfig(), encoding);
        writer.flush();
        writer.close();
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
