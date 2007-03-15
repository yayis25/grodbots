/*
 * Created on Mar 16, 2006
 *
 * This code belongs to Jonathan Fuerth.
 */
package net.bluecow.robot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bluecow.robot.resource.ResourceLoader;
import net.bluecow.robot.resource.SystemResourceLoader;
import net.bluecow.robot.resource.ZipFileResourceLoader;

public class Main {

    private class SaveCircuitAction extends AbstractAction {
        
        private Collection<Robot> robots;
        
        public SaveCircuitAction() {
            super("Save Circuit...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        }
        
        public void actionPerformed(ActionEvent e) {
            Preferences recentFiles = RobotUtils.getPrefs().node("recentCircuitFiles");
            OutputStream out = null;
            try {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save Circuit Description File");
                fc.setCurrentDirectory(new File(recentFiles.get("0", System.getProperty("user.home"))));
                int choice = fc.showSaveDialog(playfieldFrame);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    out = new FileOutputStream(fc.getSelectedFile());
                    CircuitStore.save(out, robots);
                    RobotUtils.updateRecentFiles(recentFiles, fc.getSelectedFile());
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(playfieldFrame, "Save Failed: "+ex.getMessage());
            } catch (BackingStoreException ex) {
                System.err.println("Couldn't update user prefs");
                ex.printStackTrace();
            } finally {
                try {
                    if (out != null) out.close();
                } catch (IOException e1) {
                    System.err.println("Bad luck.. couldn't close output file!");
                    e1.printStackTrace();
                }
            }
        }

        public void setRobots(Collection<Robot> robots) {
            this.robots = robots;
        }
    }

    private class LoadCircuitAction extends AbstractAction {
        
        private Collection<Robot> robots;
        
        public LoadCircuitAction() {
            super("Open Circuit...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }
        
        public void actionPerformed(ActionEvent e) {
            InputStream in = null;
            Preferences recentFiles = RobotUtils.getPrefs().node("recentCircuitFiles");
            try {
                File f = new File(e.getActionCommand() == null ? "!@#$%^&*" : e.getActionCommand());
                if ( ! (f.isFile() && f.canRead()) ) {
                    JFileChooser fc = new JFileChooser();
                    fc.setDialogTitle("Open Circuit Description File");
                    fc.setCurrentDirectory(new File(recentFiles.get("0", System.getProperty("user.home"))));
                    int choice = fc.showOpenDialog(null);
                    if (choice != JFileChooser.APPROVE_OPTION) return;
                    f = fc.getSelectedFile();
                }
                in = new FileInputStream(f);
                for (Robot r : robots) {
                    r.getCircuit().removeAllGates();
                }
                CircuitStore.load(in, robots);
                RobotUtils.updateRecentFiles(recentFiles, f);
            } catch (BackingStoreException ex) {
                System.out.println("Couldn't update user prefs");
                ex.printStackTrace();
            } catch (FileFormatException ex) {
                RobotUtils.showFileFormatException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Load Failed: "+ex.getMessage());
            } finally {
                try {
                    if (in != null) in.close();
                } catch (IOException e1) {
                    System.out.println("Bad luck.. couldn't close input file!");
                    e1.printStackTrace();
                }
            }
        }

        public void setRobots(Collection<Robot> robots) {
            this.robots = robots;
        }
    }

    private class LoadGhostAction extends AbstractAction {
                
        private GameLoop gameLoop;

        private Playfield playfield;
        
        private Collection<Robot> robots;
        
        public LoadGhostAction() {
            super("Open Circuit Into New Ghost...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }
        
        public void actionPerformed(ActionEvent e) {
            InputStream in = null;
            Preferences recentFiles = RobotUtils.getPrefs().node("recentGhostFiles");
            try {
                File f = new File(e.getActionCommand() == null ? "!@#$%^&*" : e.getActionCommand());
                if ( ! (f.isFile() && f.canRead()) ) {
                    JFileChooser fc = new JFileChooser();
                    fc.setDialogTitle("Open Circuit Description File For Ghost");
                    fc.setCurrentDirectory(new File(recentFiles.get("0", System.getProperty("user.home"))));
                    int choice = fc.showOpenDialog(playfieldFrame);
                    if (choice != JFileChooser.APPROVE_OPTION) return;
                    f = fc.getSelectedFile();
                }
                in = new FileInputStream(f);
                LevelConfig ghostLevel = new LevelConfig(config.getLevels().get(levelNumber));
                
                /*
                 * remove existing robots.. should have a method in LevelConfig for creating this
                 * type of copy. 
                 */
                for (Robot robot : new ArrayList<Robot>(ghostLevel.getRobots())) {
                    ghostLevel.removeRobot(robot);
                }
                
                List<Robot> ghosts = new ArrayList<Robot>();
                for (Robot robot : robots) {
                    Robot ghost = new Robot(robot, ghostLevel);
                    ghosts.add(ghost);
                    ghostLevel.addRobot(ghost);
                }
                CircuitStore.load(in, ghosts);
                for (Robot ghost : ghosts) {
                    if (ghost.getLevel() != ghostLevel) {
                        throw new IllegalStateException("Ghost's level is not the ghostLevel");
                    }
                    if (!ghostLevel.getRobots().contains(ghost)) {
                        throw new IllegalStateException("ghostLevel doesn't contain ghost "+ghost);
                    }
                    CircuitEditor ghostCE = new CircuitEditor(ghost.getCircuit(), sm);
                    ghost.getCircuit().setLocked(true);
                    JFrame ghostFrame = new JFrame("Ghost of "+ghost.getLabel()+" from "+f.getName());
                    ghostFrame.addWindowListener(new BuffyTheGhostKiller(ghost));
                    ghostFrame.setContentPane(ghostCE);
                    ghostFrame.pack();
                    ghostFrame.setLocationRelativeTo(playfieldFrame);
                    ghostFrame.setVisible(true);
                    windowsToClose.add(ghostFrame);
                }
                
                ghostLevel.snapshotState();
                gameLoop.addGhostLevel(ghostLevel);
                
                RobotUtils.updateRecentFiles(recentFiles, f);
                
            } catch (FileFormatException ex) {
                RobotUtils.showFileFormatException(ex);
            } catch (BackingStoreException ex) {
                System.out.println("Couldn't update user prefs");
                ex.printStackTrace();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(playfieldFrame, "Load Failed: "+ex.getMessage());
            } finally {
                try {
                    if (in != null) in.close();
                } catch (IOException e1) {
                    System.out.println("Bad luck.. couldn't close ghost input file!");
                    e1.printStackTrace();
                }
            }
        }
        
        public void setGameLoop(GameLoop gameLoop) {
            this.gameLoop = gameLoop;
        }

        public void setPlayfield(Playfield playfield) {
            this.playfield = playfield;
        }

        public void setRobots(Collection<Robot> robots) {
            this.robots = robots;
        }
        
        private class BuffyTheGhostKiller extends WindowAdapter {
            
            private Robot ghostToKill;
            
            public BuffyTheGhostKiller(Robot ghostToKill) {
                this.ghostToKill = ghostToKill;
            }

            @Override
            public void windowClosing(WindowEvent e) {
                playfield.removeRobot(ghostToKill);
                gameLoop.removeRobot(ghostToKill);
                e.getWindow().dispose();
            }
        }
    }

    private class LoadLevelsAction extends AbstractAction {
        
        JFileChooser fc;
        
        public LoadLevelsAction() {
            super("Open Levels...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
            fc = new JFileChooser();
            fc.setDialogTitle("Choose a Robot Levels File");
        }
        
        public void actionPerformed(ActionEvent e) {
            Preferences recentFiles = RobotUtils.getPrefs().node("recentGameFiles");
            
            File f = new File(e.getActionCommand() == null ? "!@#$%^&*" : e.getActionCommand());
            try {
                if ( ! (f.isFile() && f.canRead()) ) {
                    fc.setCurrentDirectory(new File(recentFiles.get("0", System.getProperty("user.home"))));
                    int choice = fc.showOpenDialog(null);
                    if (choice != JFileChooser.APPROVE_OPTION) return;
                    f = fc.getSelectedFile();
                }
                RobotUtils.updateRecentFiles(recentFiles, f);
                ResourceLoader resourceLoader = new ZipFileResourceLoader(f);
                loadGameConfig(resourceLoader);
                setLevel(0);
            } catch (BackingStoreException ex) {
                System.out.println("Couldn't update user prefs");
                ex.printStackTrace();
            } catch (FileFormatException ex) {
                RobotUtils.showFileFormatException(ex);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "Couldn't load the levels:\n\n"
                        +ex.getMessage()+"\n\n"
                        +"A stack trace is available on the Java Console.",
                        "Load Error", JOptionPane.ERROR_MESSAGE, null);
            }
        }
    }
    
    private class QuitAction extends AbstractAction {
        public QuitAction() {
            super("Exit");
            putValue(MNEMONIC_KEY, KeyEvent.VK_X);
        }
        
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
    
    private Playfield playfield;
    
    private GameConfig config;
    
    private SaveCircuitAction saveCircuitAction = new SaveCircuitAction();
    private LoadCircuitAction loadCircuitAction = new LoadCircuitAction();
    private LoadLevelsAction loadLevelsAction = new LoadLevelsAction();
    private LoadGhostAction loadGhostAction = new LoadGhostAction();
    private QuitAction quitAction = new QuitAction();
    
    private JFrame playfieldFrame;

    /**
     * A list of windows that should get closed before moving to the next level.
     */
    private List<Window> windowsToClose = new ArrayList<Window>();
    
    private int levelNumber;

    private SoundManager sm;

    private GameStateHandler gameStateHandler;

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
        playfieldFrame.setLocation(
                RobotUtils.getPrefs().getInt("PlayfieldFrame.x", 30),
                RobotUtils.getPrefs().getInt("PlayfieldFrame.y", 30));
        
        JMenuBar mb;
        JMenu menu;
        JMenuItem item;
        playfieldFrame.setJMenuBar(mb = new JMenuBar());
        mb.add(menu = new JMenu("File"));
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(item = new JMenuItem(loadLevelsAction));
        menu.add(item = new RecentFilesMenu("Open Recent Levels", loadLevelsAction, RobotUtils.getPrefs().node("recentGameFiles")));
        item.setMnemonic(KeyEvent.VK_T);
        menu.add(item = new JMenuItem(loadCircuitAction));
        menu.add(item = new RecentFilesMenu("Open Recent Circuit", loadCircuitAction, RobotUtils.getPrefs().node("recentCircuitFiles")));
        item.setMnemonic(KeyEvent.VK_R);
        menu.add(item = new JMenuItem(saveCircuitAction));
        menu.add(item = new JMenuItem(quitAction));
        
        mb.add(menu = new JMenu("Ghost"));
        menu.setMnemonic(KeyEvent.VK_G);
        menu.add(item = new JMenuItem(loadGhostAction));
        menu.add(item = new RecentFilesMenu("Open Recent Ghost", loadGhostAction, RobotUtils.getPrefs().node("recentGhostFiles")));
        item.setMnemonic(KeyEvent.VK_R);

        try {
            ResourceLoader builtInResourceLoader = new SystemResourceLoader();
            loadGameConfig(builtInResourceLoader);
            
            sm = new SoundManager(builtInResourceLoader);
            
            // this could be moved into a section of the game config xml file, then the
            // levelstore could init the sound manager and stash it in the gameconfig
            sm.addClip("create-AND", "ROBO-INF/sounds/create-AND.wav");
            sm.addClip("create-OR",  "ROBO-INF/sounds/create-OR.wav");
            sm.addClip("create-NOT", "ROBO-INF/sounds/create-NOT.wav");
            sm.addClip("delete_gate", "ROBO-INF/sounds/delete_gate.wav");
            sm.addClip("drag-AND", "ROBO-INF/sounds/drag-AND.wav");
            sm.addClip("drag-OR",  "ROBO-INF/sounds/drag-OR.wav");
            sm.addClip("drag-NOT", "ROBO-INF/sounds/drag-NOT.wav");
            sm.addClip("enter_gate", "ROBO-INF/sounds/enter_gate.wav");
            sm.addClip("leave_gate", "ROBO-INF/sounds/leave_gate.wav");
            sm.addClip("pull_wire", "ROBO-INF/sounds/pull_wire.wav");
            sm.addClip("start_drawing_wire", "ROBO-INF/sounds/start_drawing_wire.wav");
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
     * Reads in a new game config, replacing the currently-loaded one with
     * the one in the given ResourceLoader.
     * 
     * @param builtInResourceLoader
     * @throws IOException
     */
    private void loadGameConfig(ResourceLoader builtInResourceLoader) throws IOException {
        config = LevelStore.loadLevels(builtInResourceLoader);
    }

    void setLevel(int newLevelNum) {
        if (gameStateHandler != null) {
            gameStateHandler.setState(GameState.RESET);
        }
        for (Window w : windowsToClose) {
            w.dispose();
        }
        
        levelNumber = newLevelNum;
        final LevelConfig level = config.getLevels().get(newLevelNum);
        level.resetState();
//        if (playfield != null) {
//            // this is necessary because the playfield cleans up after itself when removed
//            playfield.getParent().remove(playfield);
//        }
        playfield = new Playfield(config, level);
        playfield.setLabellingOn(true);
        Map<Robot,CircuitEditor> robots = new LinkedHashMap<Robot,CircuitEditor>();
        for (Robot robot : level.getRobots()) {
            robots.put(robot,
                    new CircuitEditor(robot.getCircuit(), sm));
        }
        final GameLoop gameLoop = new GameLoop(robots.keySet(), level, playfield);

        saveCircuitAction.setRobots(robots.keySet());
        loadCircuitAction.setRobots(robots.keySet());
        loadGhostAction.setGameLoop(gameLoop);
        loadGhostAction.setPlayfield(playfield);
        loadGhostAction.setRobots(robots.keySet());
        
        System.out.println("Starting level "+level.getName());
        
        final JSpinner frameDelaySpinner = new JSpinner();
        frameDelaySpinner.setValue(new Integer(gameLoop.getFrameDelay()));
        frameDelaySpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                gameLoop.setFrameDelay((Integer) frameDelaySpinner.getValue());
            }
        });

        gameStateHandler = new GameStateHandler(gameLoop, sm, robots);

        // have to handle next level events ourselves; all others are handled
        // by the GameStateHandler.
        gameStateHandler.getNextLevelButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (levelNumber+1 < config.getLevels().size()) {
                    setLevel(++levelNumber);
                } else {
                    JOptionPane.showMessageDialog(playfield, "There are no more levels.", "A message for you", JOptionPane.INFORMATION_MESSAGE);
                    JOptionPane.showConfirmDialog(playfield, "What, were you expecting some fanfare?", "Inquiry", JOptionPane.YES_NO_OPTION);
                    JOptionPane.showOptionDialog(playfield, "Well, there are no more levels. I guess that means you won.", "Retort", 0, 0, null, new String[] {"Yay", "Drat"}, "Drat");
                }
            }
        });
        
        final JButton saveCircuitButton = new JButton();
        saveCircuitButton.setAction(saveCircuitAction);

        final JButton loadCircuitButton = new JButton();
        loadCircuitButton.setAction(loadCircuitAction);

        final JButton loadLevelsButton = new JButton();
        loadLevelsButton.setAction(loadLevelsAction);
        
        JPanel topButtonPanel = new JPanel(new FlowLayout());
        topButtonPanel.add(gameStateHandler.getStartButton());
        topButtonPanel.add(gameStateHandler.getStepButton());
        topButtonPanel.add(gameStateHandler.getResetButton());
        topButtonPanel.add(gameStateHandler.getNextLevelButton());
        
        JPanel bottomButtonPanel = new JPanel(new FlowLayout());
        bottomButtonPanel.add(loadCircuitButton);
        bottomButtonPanel.add(saveCircuitButton);
        bottomButtonPanel.add(loadLevelsButton);
        bottomButtonPanel.add(new JLabel("Frame Delay:"));
        bottomButtonPanel.add(frameDelaySpinner);

        if (System.getProperty("net.bluecow.robot.DEBUG") != null) {
            final JSpinner levelSpinner = new JSpinner();
            levelSpinner.setValue(new Integer(levelNumber));
            levelSpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent evt) {
                    int newLevel = (Integer) levelSpinner.getValue();
                    if (newLevel < 0) {
                        Toolkit.getDefaultToolkit().beep();
                        System.out.println("Silly person tried to go to level "+newLevel);
                    } else if (newLevel < config.getLevels().size()) {
                        setLevel(newLevel);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                        System.out.println("Silly person tried to go to level "+newLevel);
                    }
                }
            });
            bottomButtonPanel.add(new JLabel("Level: "));
            bottomButtonPanel.add(levelSpinner);
        }
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(topButtonPanel, BorderLayout.NORTH);
        buttonPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
        
        JPanel floaterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        floaterPanel.add(playfield);

        JComponent pffcp = new JPanel(new BorderLayout());
        pffcp.add(new JLabel(level.getName(), JLabel.CENTER), BorderLayout.NORTH);
        pffcp.add(floaterPanel, BorderLayout.CENTER);
        pffcp.add(buttonPanel, BorderLayout.SOUTH);
        
        playfieldFrame.setTitle("CakeBots: Level "+newLevelNum);
        playfieldFrame.setContentPane(pffcp);
        playfieldFrame.pack();
        playfieldFrame.setVisible(true);

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point newFrameLocation = new Point(
                playfieldFrame.getX() + playfieldFrame.getWidth() + 5,
                playfieldFrame.getY());
        for (Map.Entry<Robot, CircuitEditor> entry : robots.entrySet()) {
            Robot robot = entry.getKey();
            CircuitEditor ce = entry.getValue();
            JPanel efcp = new JPanel(new BorderLayout());
            efcp.add(ce, BorderLayout.CENTER);
            JFrame editorFrame = new JFrame("Circuit Editor: "+robot.getLabel());
            editorFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            editorFrame.setContentPane(efcp);
            editorFrame.pack();
            editorFrame.setLocation(newFrameLocation);
            editorFrame.setVisible(true);
            windowsToClose.add(editorFrame);
            newFrameLocation.x += editorFrame.getWidth();
            if (newFrameLocation.x + editorFrame.getWidth() > screenSize.width) {
                newFrameLocation.x = 0;
                newFrameLocation.y += editorFrame.getHeight();
                if (newFrameLocation.y + editorFrame.getHeight() > screenSize.height) {
                    newFrameLocation.y = screenSize.height - editorFrame.getHeight();
                }
            }
        }
    }
    
    public GameConfig getGameConfig() {
        return config;
    }

}
