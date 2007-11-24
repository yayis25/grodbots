/*
 * Copyright (c) 2007, Jonathan Fuerth
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Jonathan Fuerth nor the names of other
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * Created on Mar 16, 2006
 *
 * This code belongs to Jonathan Fuerth.
 */
package net.bluecow.robot;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.bluecow.robot.resource.CompoundResourceLoader;
import net.bluecow.robot.resource.PrefixResourceLoader;
import net.bluecow.robot.resource.ResourceLoader;
import net.bluecow.robot.resource.ResourceUtils;
import net.bluecow.robot.resource.SystemResourceLoader;

/**
 * The main type for the GrodBots game that ties everything together.  There is
 * also a static main() method which is the normal mechansim for launching the
 * game.
 *
 * @author fuerth
 * @version $Id$
 */
public class Main {

    /**
     * Controls whether or not the debugging features of this class are enabled.
     */
    private static final boolean debugOn = false;
    
    /**
     * Prints the given printf-formatted message, followed by a newline,
     * to the console if debugOn == true.
     */
    private void debugf(String fmt, Object ... args) {
        if (debugOn) debug(String.format(fmt, args));
    }

    /**
     * Prints the given string followed by a newline to the console if debugOn==true.
     */
    private void debug(String msg) {
        if (debugOn) System.out.println(msg);
    }

    /**
     * The configuration object for the current level pack.  To switch between gameconfigs,
     * use the {@link #loadGameConfig(ResourceLoader)} method.
     */
    GameConfig config;
    
    /**
     * The frame that contains the entire user interface for the game.  The content
     * pane itself is managed by the {@link @gameUI} object.
     */
    JFrame playfieldFrame;

    /**
     * Manages the user interface panel for the current level.  Gets set or replaced
     * in {@link #setLevel(int)}.
     */
    private GameUI gameUI;
    
    /**
     * A list of windows that should get closed before moving to the next level.
     * <p>
     * TODO the game is now supposed to be a single-window affair, so after code cleanup this list will be unnecessary
     */
    List<Window> windowsToClose = new ArrayList<Window>();
    
    /**
     * The index (within the current game config) of which level is currently active.
     */
    int levelNumber;

    /**
     * The sound manager for this session. This object will be moved into the GameConfig
     * soon.
     */
    SoundManager sm;
    
    /**
     * Whether or not to start in full screen mode.
     */
    private boolean fullscreen = false;

    /**
     * Creates a new instance of this class (it will load the game config from
     * the system classpath), and starts level 0.
     */
    public static void main(String[] args) {
        final Main main = new Main();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                main.setLevel(0);
            }
        });
    }
    
    public Main() {
        playfieldFrame = new JFrame("Grod - The Cake Assimilator!");
        playfieldFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        playfieldFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                RobotUtils.getPrefs().putInt("PlayfieldFrame.x", playfieldFrame.getX());
                RobotUtils.getPrefs().putInt("PlayfieldFrame.y", playfieldFrame.getY());
            }
        });
        
        if (fullscreen) {
            playfieldFrame.setUndecorated(true);
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(playfieldFrame);
        } else {
            playfieldFrame.setLocation(
                RobotUtils.getPrefs().getInt("PlayfieldFrame.x", 30),
                RobotUtils.getPrefs().getInt("PlayfieldFrame.y", 30));
        }
        
        try {
            ResourceLoader defaultLevelsResourceLoader =
                new PrefixResourceLoader(new SystemResourceLoader(), "default/");
            ResourceLoader builtinResourceLoader =
                new PrefixResourceLoader(new SystemResourceLoader(), "builtin/");
            CompoundResourceLoader compoundResourceLoader =
                new CompoundResourceLoader(defaultLevelsResourceLoader, builtinResourceLoader);
            loadGameConfig(compoundResourceLoader);
            
            sm = new SoundManager(compoundResourceLoader);
            
            // this could be moved into a section of the game config xml file, then the
            // levelstore could init the sound manager and stash it in the gameconfig
            sm.addClip("confused_robot", "ROBO-INF/sounds/confused_robot.wav");
            sm.addClip("create_prohibited", "ROBO-INF/sounds/create_prohibited.wav");
            sm.addClip("create-AND", "ROBO-INF/sounds/create-AND.wav");
            sm.addClip("create-OR",  "ROBO-INF/sounds/create-OR.wav");
            sm.addClip("create-NOT", "ROBO-INF/sounds/create-NOT.wav");
            sm.addClip("delete_all", "ROBO-INF/sounds/delete_all.wav");
            sm.addClip("delete_gate", "ROBO-INF/sounds/delete_gate.wav");
            sm.addClip("delete_prohibited", "ROBO-INF/sounds/delete_prohibited.wav");
            sm.addClip("drag-AND", "ROBO-INF/sounds/drag-AND.wav");
            sm.addClip("drag-OR",  "ROBO-INF/sounds/drag-OR.wav");
            sm.addClip("drag-NOT", "ROBO-INF/sounds/drag-NOT.wav");
            sm.addClip("enter_gate", "ROBO-INF/sounds/enter_gate.wav");
            sm.addClip("leave_gate", "ROBO-INF/sounds/leave_gate.wav");
            sm.addClip("pull_wire", "ROBO-INF/sounds/pull_wire.wav");
            sm.addClip("relay_clicking", "ROBO-INF/sounds/relay_clicking.wav");
            sm.addClip("start_drawing_wire", "ROBO-INF/sounds/start_drawing_wire.wav");
            sm.addClip("teleport", "ROBO-INF/sounds/teleport.wav");
            sm.addClip("unterminated_wire", "ROBO-INF/sounds/unterminated_wire.wav");
            sm.addClip("terminated_wire", "ROBO-INF/sounds/terminated_wire.wav");
            sm.addClip("win", "ROBO-INF/sounds/win.wav");
        } catch (FileFormatException ex) {
            ex.printStackTrace();
            RobotUtils.showFileFormatException(ex);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Couldn't initialise:\n\n"
                       +e.getClass().getName()+"\n"
                       +e.getMessage()+"\n\n"
                       +"A stack trace is available on the Java Console.",
                    "Startup Error", JOptionPane.ERROR_MESSAGE, null);
        }
    }

    /**
     * Reads in a new game config, replacing the currently-loaded one with the
     * one in the given ResourceLoader.
     * 
     * @param resourceLoader
     *            The resource loader to load the level and its associated
     *            resources from. Resources in this loader will be supplemented
     *            by the system resource loader, so that level packs need not
     *            include copies of the default sprites and skins (although they
     *            may provide overrides to the defaults if they want to alter
     *            the look and feel of the game UI).
     * @throws IOException
     *             If there are problems loading or parsing any the game
     *             resources.
     */
    void loadGameConfig(ResourceLoader resourceLoader) throws IOException {
        ResourceLoader builtinResourceLoader =
            new PrefixResourceLoader(new SystemResourceLoader(), "builtin/");
        ResourceLoader loader =
            new CompoundResourceLoader(resourceLoader, builtinResourceLoader);
        config = LevelStore.loadLevels(loader);
        ResourceUtils.initResourceURLHandler(loader);
    }

    void setLevel(int newLevelNum) {

        debugf("Change level %d -> %d", levelNumber, newLevelNum);

        if (gameUI != null) {
            gameUI.close();
        }

        // XXX won't be necessary once ghosts are integrated into gameui
        for (Window w : windowsToClose) {
            w.dispose();
        }
        
        levelNumber = newLevelNum;
        final LevelConfig level = config.getLevels().get(newLevelNum);
        level.resetState();
        
        Action nextLevelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (levelNumber+1 < config.getLevels().size()) {
                    setLevel(levelNumber + 1);
                } else {
                    JOptionPane.showMessageDialog(playfieldFrame, "There are no more levels.", "A message for you", JOptionPane.INFORMATION_MESSAGE);
                    JOptionPane.showConfirmDialog(playfieldFrame, "What, were you expecting some fanfare?", "Inquiry", JOptionPane.YES_NO_OPTION);
                    JOptionPane.showOptionDialog(playfieldFrame, "Well, there are no more levels. I guess that means you won.", "Retort", 0, 0, null, new String[] {"Yay", "Drat"}, "Drat");
                }
            }
        };

        try {
            gameUI = new GameUI(this, level, sm, nextLevelAction);
        } catch (IOException ex) {
            RobotUtils.showException("Failed to create game UI", ex);
            playfieldFrame.dispose();
            return;
        }
        
        System.out.println("Starting level "+level.getName());
        
        final JMenuBar menuBar = gameUI.getMenuBar();
        if (System.getProperty("net.bluecow.robot.DEBUG") != null) {
            JMenu m = new JMenu("Debug");
            menuBar.add(m);
            
            JMenu levelMenu = new JMenu("Go To Level");
            m.add(levelMenu);
            
            int i = 0;
            for (LevelConfig l : config.getLevels()) {
                JMenuItem mi = new JMenuItem(i + ": " + l.getName());
                levelMenu.add(mi);
                final int levelNum = i;
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setLevel(levelNum);
                    }
                });
                i++;
            }
        }
        
        playfieldFrame.setJMenuBar(menuBar);
        playfieldFrame.setTitle("GrodBots: Level "+newLevelNum);
        playfieldFrame.setBackground(Color.BLACK);
        playfieldFrame.setForeground(Color.WHITE);
        playfieldFrame.setContentPane(gameUI.getPanel());
        playfieldFrame.pack();
        playfieldFrame.setVisible(true);
        playfieldFrame.requestFocus();
    }
    
    /**
     * Returns this session's current game configuration.
     */
    public GameConfig getGameConfig() {
        return config;
    }

    /**
     * Returns the resource loader associated with this session's current
     * game config.  This is a convenience method for getGameConfig().getResourceLoader().
     */
    public ResourceLoader getResourceLoader() {
        return getGameConfig().getResourceLoader();
    }
}
