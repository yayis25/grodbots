/*
 * Created on Mar 16, 2006
 *
 * This code belongs to Jonathan Fuerth.
 */
package net.bluecow.robot;

import java.awt.AlphaComposite;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
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

public class Main {
    
    private static final String DEFAULT_MAP_RESOURCE_PATH = "ROBO-INF/default.map";

    private static final int ROBOT_ICON_COUNT = 8;

    private static enum GameState { NOT_STARTED, RESET, STEP, RUNNING, PAUSED, WON };
    
    private GameState state = GameState.NOT_STARTED;
    
    private Preferences prefs = Preferences.userNodeForPackage(Main.class);

    private class GameLoopResetter implements PropertyChangeListener {

        private GameLoop gl;
        private GameStateHandler stateHandler;
        private Collection<CircuitEditor> editors;
        private GameState nextState;
        
        public GameLoopResetter(GameLoop gl, GameStateHandler stateHandler, Collection<CircuitEditor> editors, GameState nextState) {
            this.gl = gl;
            this.stateHandler = stateHandler;
            this.editors = editors;
            this.nextState = nextState;
            if (gl.isRunning()) {
                gl.addPropertyChangeListener(this);
                gl.setStopRequested(true);
            } else {
                finishReset();
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("running") && evt.getNewValue().equals(false)) {
                gl.removePropertyChangeListener(this);
                finishReset();
            }
        }
        
        private void finishReset() {
            gl.resetState();
            playfield.setWinMessage(null);
            for (CircuitEditor ce : editors) {
                ce.setLocked(false);
            }
            stateHandler.setState(nextState);
        }
    }

    private class GameStateHandler implements ActionListener {
        private final GameLoop loop;

        private final JButton start;

        private final JButton reset;

        private final Map<Robot,CircuitEditor> robots;

        private final JButton step;

        private GameStateHandler(
                GameLoop loop, JButton start, JButton reset,
                Map<Robot,CircuitEditor> robots, JButton step) {
            super();
            this.loop = loop;
            this.start = start;
            this.reset = reset;
            this.robots = robots;
            this.step = step;
        }

        public void actionPerformed(ActionEvent e) {
            JButton source = (JButton) e.getSource();
            if (source == start) {
                if (state == GameState.RUNNING) {
                    setState(GameState.PAUSED);
                } else {
                    setState(GameState.RUNNING);
                }
            } else if (source == step) {
                setState(GameState.STEP);
            } else if (source == reset) {
                setState(GameState.RESET);
            }
        }

        public void setState(GameState newState) {
            System.out.printf("Switch state %s -> %s\n", state, newState);
            if (newState == GameState.RESET) {
                state = newState;
                new GameLoopResetter(loop, this, robots.values(), GameState.NOT_STARTED);
                // the rest of the work is deferred until the loop is really stopped
            } else if (newState == GameState.PAUSED) {
                state = newState;
                loop.setStopRequested(true);
                lockEditors(true);
                start.setText("Resume");
                step.setText("Step");
                reset.setText("Reset");
                playfield.setLabellingOn(false);
            } else if (newState == GameState.NOT_STARTED) {
                state = newState;
                lockEditors(false);
                start.setText("Start");
                step.setText("Step");
                reset.setText("Reset");
                playfield.setLabellingOn(true);
            } else if (newState == GameState.RUNNING) {
                if (state == GameState.WON) {
                    state = GameState.RESET;
                    new GameLoopResetter(loop, this, robots.values(), GameState.RUNNING);
                } else {
                    state = newState;
                    lockEditors(true);
                    loop.setStopRequested(false);
                    new Thread(loop).start();
                    start.setText("Pause");
                    step.setText("Step");
                    reset.setText("Reset");
                }
            } else if (newState == GameState.STEP) {
                if (state == GameState.WON) {
                    state = GameState.RESET;
                    new GameLoopResetter(loop, this, robots.values(), GameState.STEP);
                } else if (state == GameState.RUNNING) {
                    state = newState;
                    lockEditors(true);
                    loop.setStopRequested(true);
                    setState(GameState.PAUSED);
                } else {
                    state = newState;
                    lockEditors(true);
                    loop.setStopRequested(false);
                    loop.singleStep();
                    setState(GameState.PAUSED);
                }
            } else if (newState == GameState.WON) {
                state = newState;
                lockEditors(true);
                playfield.setWinMessage("¡¡CAKE!! ¿You Win?");
                sm.play("win");
                start.setText("Restart");
                step.setText("Restep");
                reset.setText("Reset");
            }
        }
        
        /** Locks or unlocks all editors in the robots map. */
        private void lockEditors(boolean locked) {
            for (CircuitEditor ce : robots.values()) {
                ce.setLocked(locked);
            }
        }
    }
    
    private class SaveCircuitAction extends AbstractAction {
        
        private CircuitEditor ce;
        
        public SaveCircuitAction() {
            super("Save Circuit...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        }
        
        public void actionPerformed(ActionEvent e) {
            Preferences recentFiles = prefs.node("recentCircuitFiles");
            OutputStream out = null;
            try {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save Circuit Description File");
                fc.setCurrentDirectory(new File(recentFiles.get("0", System.getProperty("user.home"))));
                int choice = fc.showSaveDialog(ce);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    out = new FileOutputStream(fc.getSelectedFile());
                    CircuitStore.save(out, ce.getCircuit()); // list of circuits? need robot names too
                    RobotUtils.updateRecentFiles(recentFiles, fc.getSelectedFile());
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ce, "Save Failed: "+ex.getMessage());
            } catch (BackingStoreException ex) {
                System.out.println("Couldn't update user prefs");
                ex.printStackTrace();
            } finally {
                try {
                    if (out != null) out.close();
                } catch (IOException e1) {
                    System.out.println("Bad luck.. couldn't close output file!");
                    e1.printStackTrace();
                }
            }
        }

        public void setCircuitEditor(CircuitEditor ce) {
            this.ce = ce;
        }
    }

    private class LoadCircuitAction extends AbstractAction {
        
        private CircuitEditor ce;
        private Robot robot;
        
        public LoadCircuitAction() {
            super("Open Circuit...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }
        
        public void actionPerformed(ActionEvent e) {
            InputStream in = null;
            Preferences recentFiles = prefs.node("recentCircuitFiles");
            try {
                File f = new File(e.getActionCommand() == null ? "!@#$%^&*" : e.getActionCommand());
                if ( ! (f.isFile() && f.canRead()) ) {
                    JFileChooser fc = new JFileChooser();
                    fc.setDialogTitle("Open Circuit Description File");
                    fc.setCurrentDirectory(new File(recentFiles.get("0", System.getProperty("user.home"))));
                    int choice = fc.showOpenDialog(ce);
                    if (choice != JFileChooser.APPROVE_OPTION) return;
                    f = fc.getSelectedFile();
                }
                in = new FileInputStream(f);
                ce.getCircuit().removeAllGates();
                CircuitStore.load(in, ce.getCircuit(), robot);
                RobotUtils.updateRecentFiles(recentFiles, f);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ce, "Load Failed: "+ex.getMessage());
            } catch (BackingStoreException ex) {
                System.out.println("Couldn't update user prefs");
                ex.printStackTrace();
            } finally {
                try {
                    if (in != null) in.close();
                } catch (IOException e1) {
                    System.out.println("Bad luck.. couldn't close input file!");
                    e1.printStackTrace();
                }
            }
        }

        public void setCircuitEditor(CircuitEditor ce) {
            this.ce = ce;
        }

        public void setRobot(Robot robot) {
            this.robot = robot;
        }
    }

    private class LoadGhostAction extends AbstractAction {
                
        private GameLoop gameLoop;

        private Playfield playfield;
        
        public LoadGhostAction() {
            super("Open Circuit Into New Ghost...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }
        
        public void actionPerformed(ActionEvent e) {
            InputStream in = null;
            Preferences recentFiles = prefs.node("recentGhostFiles");
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
                // FIXME: have to update ghost format to include sprite, start position, and step size
                Robot ghost = new Robot("Ghost", config.getLevels().get(levelNumber), config.getSensorTypes(),
                        config.getGateTypes(), (Sprite) null, null, 0.1f);
                CircuitEditor ghostCE = new CircuitEditor(ghost.getCircuit(), sm);
                CircuitStore.load(in, ghost.getCircuit(), ghost);
                gameLoop.addRobot(ghost);
                playfield.addRobot(ghost, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                JFrame ghostFrame = new JFrame("Ghost from "+f.getName());
                ghostFrame.addWindowListener(new BuffyTheGhostKiller(ghost));
                ghostFrame.setContentPane(ghostCE);
                ghostFrame.pack();
                ghostFrame.setLocationRelativeTo(playfieldFrame);
                ghostFrame.setVisible(true);
                windowsToClose.add(ghostFrame);
                RobotUtils.updateRecentFiles(recentFiles, f);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(playfieldFrame, "Load Failed: "+ex.getMessage());
            } catch (BackingStoreException ex) {
                System.out.println("Couldn't update user prefs");
                ex.printStackTrace();
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
            int choice = fc.showOpenDialog(null);
            if (choice == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try {
                    config = LevelStore.loadLevels(new FileInputStream(f));
                    setLevel(0);
                } catch (FileFormatException ex) {
                    showFileFormatException(ex);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Could not find file '"+f.getPath()+"'");
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
    
    private ImageIcon[] robotIcons;

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
                prefs.putInt("PlayfieldFrame.x", playfieldFrame.getX());
                prefs.putInt("PlayfieldFrame.y", playfieldFrame.getY());
            }
        });
        playfieldFrame.setLocation(
                prefs.getInt("PlayfieldFrame.x", 30),
                prefs.getInt("PlayfieldFrame.y", 30));
        
        JMenuBar mb;
        JMenu menu;
        JMenuItem item;
        playfieldFrame.setJMenuBar(mb = new JMenuBar());
        mb.add(menu = new JMenu("File"));
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(item = new JMenuItem(loadLevelsAction));
        menu.add(item = new RecentFilesMenu("Open Recent Circuit", loadCircuitAction, prefs.node("recentCircuitFiles")));
        item.setMnemonic(KeyEvent.VK_R);
        menu.add(item = new JMenuItem(loadCircuitAction));
        menu.add(item = new JMenuItem(saveCircuitAction));
        menu.add(item = new JMenuItem(quitAction));
        
        mb.add(menu = new JMenu("Ghost"));
        menu.setMnemonic(KeyEvent.VK_G);
        menu.add(item = new JMenuItem(loadGhostAction));
        menu.add(item = new RecentFilesMenu("Open Recent Ghost", loadGhostAction, prefs.node("recentGhostFiles")));
        item.setMnemonic(KeyEvent.VK_R);

        try {
            URL levelMapURL = ClassLoader.getSystemResource(DEFAULT_MAP_RESOURCE_PATH);
            if (levelMapURL == null) {
                throw new RuntimeException(
                        "Could not find default map at resource path " +
                        DEFAULT_MAP_RESOURCE_PATH);
            }
            URLConnection levelMapURLConnection = levelMapURL.openConnection();
            config = LevelStore.loadLevels(levelMapURLConnection.getInputStream());
            
            robotIcons = new ImageIcon[ROBOT_ICON_COUNT];
            for (int i = 0; i < ROBOT_ICON_COUNT; i++) {
                String resname = String.format("ROBO-INF/images/robot_%02d.png", i);
                URL imgurl = ClassLoader.getSystemResource(resname);
                if (imgurl == null) throw new RuntimeException("Couldn't load resource "+resname);
                robotIcons[i] = new ImageIcon(imgurl);
            }
            
            sm = new SoundManager();
            sm.addClip("create-AND", ClassLoader.getSystemResource("ROBO-INF/sounds/create-AND.wav"));
            sm.addClip("create-OR",  ClassLoader.getSystemResource("ROBO-INF/sounds/create-OR.wav"));
            sm.addClip("create-NOT", ClassLoader.getSystemResource("ROBO-INF/sounds/create-NOT.wav"));
            sm.addClip("delete_gate", ClassLoader.getSystemResource("ROBO-INF/sounds/delete_gate.wav"));
            sm.addClip("drag-AND", ClassLoader.getSystemResource("ROBO-INF/sounds/drag-AND.wav"));
            sm.addClip("drag-OR",  ClassLoader.getSystemResource("ROBO-INF/sounds/drag-OR.wav"));
            sm.addClip("drag-NOT", ClassLoader.getSystemResource("ROBO-INF/sounds/drag-NOT.wav"));
            sm.addClip("enter_gate", ClassLoader.getSystemResource("ROBO-INF/sounds/enter_gate.wav"));
            sm.addClip("leave_gate", ClassLoader.getSystemResource("ROBO-INF/sounds/leave_gate.wav"));
            sm.addClip("pull_wire", ClassLoader.getSystemResource("ROBO-INF/sounds/pull_wire.wav"));
            sm.addClip("start_drawing_wire", ClassLoader.getSystemResource("ROBO-INF/sounds/start_drawing_wire.wav"));
            sm.addClip("unterminated_wire", ClassLoader.getSystemResource("ROBO-INF/sounds/unterminated_wire.wav"));
            sm.addClip("terminated_wire", ClassLoader.getSystemResource("ROBO-INF/sounds/terminated_wire.wav"));
            sm.addClip("win", ClassLoader.getSystemResource("ROBO-INF/sounds/win.wav"));
        } catch (FileFormatException ex) {
            ex.printStackTrace();
            showFileFormatException(ex);
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

    void setLevel(int newLevelNum) {
        for (Window w : windowsToClose) {
            w.dispose();
        }
        
        levelNumber = newLevelNum;
        final LevelConfig level = config.getLevels().get(newLevelNum);
        level.resetState();
        playfield = new Playfield(level);
        playfield.setLabellingOn(true);
        Map<Robot,CircuitEditor> robots = new LinkedHashMap<Robot,CircuitEditor>();
        for (Robot robot : level.getRobots()) {
            robots.put(robot,
                    new CircuitEditor(robot.getCircuit(), sm));
        }
        final GameLoop gameLoop = new GameLoop(robots.keySet(), level, playfield);

        // TODO fix these
//        saveCircuitAction.setCircuitEditor(ce);
//        loadCircuitAction.setCircuitEditor(ce);
//        loadCircuitAction.setRobot(robot);
        loadGhostAction.setGameLoop(gameLoop);
        loadGhostAction.setPlayfield(playfield);
        
        System.out.println("Starting level "+level.getName());
        
        final JSpinner frameDelaySpinner = new JSpinner();
        frameDelaySpinner.setValue(new Integer(gameLoop.getFrameDelay()));
        frameDelaySpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                gameLoop.setFrameDelay((Integer) frameDelaySpinner.getValue());
            }
        });

        final JButton startButton = new JButton("Start");
        final JButton stepButton = new JButton("Step");
        final JButton resetButton = new JButton("Reset");

        final GameStateHandler gameStateHandler = new GameStateHandler(
                gameLoop, startButton, resetButton, robots, stepButton);

        startButton.addActionListener(gameStateHandler);
        stepButton.addActionListener(gameStateHandler);
        resetButton.addActionListener(gameStateHandler);
        
        final JButton saveCircuitButton = new JButton();
        saveCircuitButton.setAction(saveCircuitAction);

        final JButton loadCircuitButton = new JButton();
        loadCircuitButton.setAction(loadCircuitAction);

        final JButton loadLevelsButton = new JButton();
        loadLevelsButton.setAction(loadLevelsAction);
        
        gameLoop.addPropertyChangeListener("goalReached", new PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                System.out.println("Property change! goalReached "+evt.getOldValue()+" -> "+evt.getNewValue()+" (running="+gameLoop.isRunning()+"; goalReached="+gameLoop.isGoalReached()+")");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (gameLoop.isGoalReached()) {
                            gameStateHandler.setState(GameState.WON);
                        }
                    }
                });
            }
        });

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
        
        JPanel topButtonPanel = new JPanel(new FlowLayout());
        topButtonPanel.add(startButton);
        topButtonPanel.add(stepButton);
        topButtonPanel.add(resetButton);
        
        JPanel bottomButtonPanel = new JPanel(new FlowLayout());
        bottomButtonPanel.add(loadCircuitButton);
        bottomButtonPanel.add(saveCircuitButton);
        bottomButtonPanel.add(loadLevelsButton);
        bottomButtonPanel.add(new JLabel("Frame Delay:"));
        bottomButtonPanel.add(frameDelaySpinner);
        bottomButtonPanel.add(new JLabel("Level: "));
        bottomButtonPanel.add(levelSpinner);
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(topButtonPanel, BorderLayout.NORTH);
        buttonPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
        
        JComponent pffcp = new JPanel(new BorderLayout());
        pffcp.add(new JLabel(level.getName(), JLabel.CENTER), BorderLayout.NORTH);
        pffcp.add(playfield, BorderLayout.CENTER);
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
            JFrame editorFrame = new JFrame("Circuit Editor: "+robot.getName());
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

    private void showFileFormatException(FileFormatException ex) {
        JOptionPane.showMessageDialog(null, 
                "Syntax error in project file:\n\n" +
                ex.getMessage() + "\n\n" +
                "at line "+ex.getLineNum()+": "+ex.getBadLine());
    }

}
