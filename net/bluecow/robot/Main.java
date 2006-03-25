/*
 * Created on Mar 16, 2006
 *
 * This code belongs to Jonathan Fuerth.
 */
package net.bluecow.robot;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.List;

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

    private class GameLoopResetter implements PropertyChangeListener {

        private GameLoop gl;
        private GameStateHandler stateHandler;
        private CircuitEditor ce;
        private GameState nextState;
        
        public GameLoopResetter(GameLoop gl, GameStateHandler stateHandler, CircuitEditor ce, GameState nextState) {
            this.gl = gl;
            this.stateHandler = stateHandler;
            this.ce = ce;
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
            ce.setLocked(false);
            stateHandler.setState(nextState);
        }
    }

    private class GameStateHandler implements ActionListener {
        private final GameLoop loop;

        private final JButton start;

        private final JButton reset;

        private final CircuitEditor ce;

        private final JButton step;

        private GameStateHandler(GameLoop loop, JButton start, JButton reset, CircuitEditor ce, JButton step) {
            super();
            this.loop = loop;
            this.start = start;
            this.reset = reset;
            this.ce = ce;
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
                new GameLoopResetter(loop, this, ce, GameState.NOT_STARTED);
                // the rest of the work is deferred until the loop is really stopped
            } else if (newState == GameState.PAUSED) {
                state = newState;
                loop.setStopRequested(true);
                ce.setLocked(true);
                start.setText("Resume");
                step.setText("Step");
                reset.setText("Reset");
            } else if (newState == GameState.NOT_STARTED) {
                state = newState;
                ce.setLocked(false);
                start.setText("Start");
                step.setText("Step");
                reset.setText("Reset");
            } else if (newState == GameState.RUNNING) {
                if (state == GameState.WON) {
                    state = GameState.RESET;
                    new GameLoopResetter(loop, this, ce, GameState.RUNNING);
                } else {
                    state = newState;
                    ce.setLocked(true);
                    loop.setStopRequested(false);
                    new Thread(loop).start();
                    start.setText("Pause");
                    step.setText("Step");
                    reset.setText("Reset");
                }
            } else if (newState == GameState.STEP) {
                if (state == GameState.WON) {
                    state = GameState.RESET;
                    new GameLoopResetter(loop, this, ce, GameState.STEP);
                } else if (state == GameState.RUNNING) {
                    state = newState;
                    ce.setLocked(true);
                    loop.setStopRequested(true);
                    setState(GameState.PAUSED);
                } else {
                    state = newState;
                    ce.setLocked(true);
                    loop.setStopRequested(false);
                    loop.singleStep();
                    setState(GameState.PAUSED);
                }
            } else if (newState == GameState.WON) {
                state = newState;
                ce.setLocked(true);
                playfield.setWinMessage("¡¡CAKE!! ¿You Win?");
                sm.play("win");
                start.setText("Restart");
                step.setText("Restep");
                reset.setText("Reset");
            }
        }
    }
    
    private class SaveCircuitAction extends AbstractAction {
        
        private CircuitEditor ce;
        private JFileChooser fc;
        
        public SaveCircuitAction() {
            super("Save Circuit...");
            fc = new JFileChooser();
            fc.setDialogTitle("Save Circuit Description File");
        }
        
        public void actionPerformed(ActionEvent e) {
            OutputStream out = null;
            try {
                int choice = fc.showSaveDialog(ce);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    out = new FileOutputStream(fc.getSelectedFile());
                    CircuitStore.save(out, ce);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ce, "Save Failed: "+ex.getMessage());
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
        private JFileChooser fc;
        
        public LoadCircuitAction() {
            super("Load Circuit...");
            fc = new JFileChooser();
            fc.setDialogTitle("Open Circuit Description File");
        }
        
        public void actionPerformed(ActionEvent e) {
            InputStream in = null;
            try {
                int choice = fc.showOpenDialog(ce);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    in = new FileInputStream(f);
                    ce.removeAllGates();
                    CircuitStore.load(in, ce, robot);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ce, "Load Failed: "+ex.getMessage());
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
        
        private JFileChooser fc;
        
        private GameLoop gameLoop;

        private Playfield playfield;
        
        public LoadGhostAction() {
            super("Load Circuit Into New Ghost...");
            fc = new JFileChooser();
            fc.setDialogTitle("Open Circuit Description File For Ghost");
        }
        
        public void actionPerformed(ActionEvent e) {
            InputStream in = null;
            try {
                int choice = fc.showOpenDialog(playfieldFrame);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    in = new FileInputStream(f);
                    Robot ghost = new Robot(playfield.getModel(), robotIcons);
                    CircuitEditor ghostCE = new CircuitEditor(ghost.getOutputs(), ghost.getInputsGate(), sm);
                    CircuitStore.load(in, ghostCE, ghost);
                    gameLoop.addRobot(ghost, ghostCE);
                    playfield.addRobot(ghost, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    JFrame ghostFrame = new JFrame("Ghost from "+f.getName());
                    ghostFrame.addWindowListener(new BuffyTheGhostKiller(ghost));
                    ghostFrame.setContentPane(ghostCE);
                    ghostFrame.pack();
                    ghostFrame.setLocationRelativeTo(editorFrame);
                    ghostFrame.setVisible(true);
                    windowsToClose.add(ghostFrame);
                }
            } catch (IOException ex) {
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

        private class BuffyTheGhostKiller extends WindowAdapter {
            
            private Robot ghostToKill;
            
            public BuffyTheGhostKiller(Robot ghostToKill) {
                this.ghostToKill = ghostToKill;
            }

            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Killing ghost!");
                playfield.removeRobot(ghostToKill);
                gameLoop.removeRobot(ghostToKill);
                e.getWindow().dispose();
            }
        }
    }

    private class LoadLevelsAction extends AbstractAction {
        
        JFileChooser fc;
        
        public LoadLevelsAction() {
            super("Load Levels...");
            fc = new JFileChooser();
            fc.setDialogTitle("Choose a Robot Levels File");
        }
        
        public void actionPerformed(ActionEvent e) {
            int choice = fc.showOpenDialog(null);
            if (choice == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try {
                    levels = LevelStore.loadLevels(new FileInputStream(f));
                    setLevel(0);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Could not find file '"+f.getPath()+"'");
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
        }
        
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
    
    private Playfield playfield;
    
    private List<PlayfieldModel> levels;
    
    private ImageIcon goalIcon;
    private ImageIcon[] robotIcons;
    private ImageIcon blackIcon;
    private ImageIcon whiteIcon;
    private ImageIcon redIcon;
    private ImageIcon greenIcon;
    private ImageIcon blueIcon;

    private SaveCircuitAction saveCircuitAction = new SaveCircuitAction();
    private LoadCircuitAction loadCircuitAction = new LoadCircuitAction();
    private LoadLevelsAction loadLevelsAction = new LoadLevelsAction();
    private LoadGhostAction loadGhostAction = new LoadGhostAction();
    private QuitAction quitAction = new QuitAction();
    
    private JFrame editorFrame;

    private JFrame playfieldFrame;

    /**
     * A list of windows that should get closed before moving to the next level.
     */
    private List<Window> windowsToClose = new ArrayList<Window>();
    
    private int level;

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
        editorFrame = new JFrame("Circuit Editor");
        editorFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        playfieldFrame = new JFrame("Grod - The Cake Assimilator!");
        playfieldFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar mb;
        JMenu menu;
        playfieldFrame.setJMenuBar(mb = new JMenuBar());
        mb.add(menu = new JMenu("File"));
        menu.add(new JMenuItem(loadLevelsAction));
        menu.add(new JMenuItem(loadCircuitAction));
        menu.add(new JMenuItem(saveCircuitAction));
        menu.add(new JMenuItem(loadGhostAction));
        menu.add(new JMenuItem(quitAction));

        try {
            URL levelMapURL = ClassLoader.getSystemResource(DEFAULT_MAP_RESOURCE_PATH);
            if (levelMapURL == null) {
                throw new RuntimeException(
                        "Could not find default map at resource path " +
                        DEFAULT_MAP_RESOURCE_PATH);
            }
            URLConnection levelMapURLConnection = levelMapURL.openConnection();
            levels = LevelStore.loadLevels(levelMapURLConnection.getInputStream());
            
            goalIcon = new ImageIcon(ClassLoader.getSystemResource("ROBO-INF/images/cake.png"));
            robotIcons = new ImageIcon[ROBOT_ICON_COUNT];
            for (int i = 0; i < ROBOT_ICON_COUNT; i++) {
                String resname = String.format("ROBO-INF/images/robot_%02d.png", i);
                URL imgurl = ClassLoader.getSystemResource(resname);
                if (imgurl == null) throw new RuntimeException("Couldn't load resource "+resname);
                robotIcons[i] = new ImageIcon(imgurl);
            }
            blackIcon = new ImageIcon(ClassLoader.getSystemResource("ROBO-INF/images/blacktile.png"));
            whiteIcon = new ImageIcon(ClassLoader.getSystemResource("ROBO-INF/images/whitetile.png"));
            redIcon = new ImageIcon(ClassLoader.getSystemResource("ROBO-INF/images/redtile.png"));
            greenIcon = new ImageIcon(ClassLoader.getSystemResource("ROBO-INF/images/greentile.png"));
            blueIcon = new ImageIcon(ClassLoader.getSystemResource("ROBO-INF/images/bluetile.png"));
            
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

    void setLevel(int levelNum) {
        for (Window w : windowsToClose) {
            w.dispose();
        }
        
        level = levelNum;
        final PlayfieldModel pfModel = levels.get(levelNum);
        final Robot robot = new Robot(pfModel, robotIcons);
        playfield = new Playfield(pfModel, robot);
        final CircuitEditor ce = new CircuitEditor(robot.getOutputs(), robot.getInputsGate(), sm);
        final GameLoop gameLoop = new GameLoop(robot, playfield, ce);

        saveCircuitAction.setCircuitEditor(ce);
        loadCircuitAction.setCircuitEditor(ce);
        loadCircuitAction.setRobot(robot);
        loadGhostAction.setGameLoop(gameLoop);
        loadGhostAction.setPlayfield(playfield);
        
        playfield.setGoalIcon(goalIcon);
        playfield.setBlackIcon(blackIcon);
        playfield.setWhiteIcon(whiteIcon);
        playfield.setRedIcon(redIcon);
        playfield.setGreenIcon(greenIcon);
        playfield.setBlueIcon(blueIcon);

        System.out.println("Starting level "+pfModel.getName());
        
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
                gameLoop, startButton, resetButton, ce, stepButton);

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
        levelSpinner.setValue(new Integer(level));
        levelSpinner.addChangeListener(new ChangeListener() {
           public void stateChanged(ChangeEvent evt) {
               int newLevel = (Integer) levelSpinner.getValue();
               if (newLevel < 0) {
                   Toolkit.getDefaultToolkit().beep();
                   System.out.println("Silly person tried to go to level "+newLevel);
               } else if (newLevel < levels.size()) {
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
        pffcp.add(new JLabel(pfModel.getName(), JLabel.CENTER), BorderLayout.NORTH);
        pffcp.add(playfield, BorderLayout.CENTER);
        pffcp.add(buttonPanel, BorderLayout.SOUTH);
        
        playfieldFrame.setTitle("CakeBots: Level "+levelNum);
        playfieldFrame.setContentPane(pffcp);
        playfieldFrame.pack();
        playfieldFrame.setVisible(true);

        JPanel efcp = new JPanel(new BorderLayout());
        efcp.add(ce, BorderLayout.CENTER);
        editorFrame.setContentPane(efcp);
        editorFrame.pack();
        editorFrame.setLocation(
                playfieldFrame.getX() + playfieldFrame.getWidth() + 5,
                playfieldFrame.getY());
        editorFrame.setVisible(true);
    }
    
    public List<PlayfieldModel> getLevels() {
        return levels;
    }

}
