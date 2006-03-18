/*
 * Created on Mar 16, 2006
 *
 * This code belongs to Jonathan Fuerth.
 */
package net.bluecow.robot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Main {
    
    private static final String DEFAULT_MAP_RESOURCE_PATH = "ROBO-INF/default.map";

    private class SaveCircuitAction extends AbstractAction {
        
        private CircuitEditor ce;
        
        public SaveCircuitAction(CircuitEditor ce) {
            super("Save Circuit");
            this.ce = ce;
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                CircuitStore.save(buf, ce);
                buf.close();
                JTextArea ta = new JTextArea(buf.toString(), 24, 80);
                JOptionPane.showMessageDialog(ce, new JScrollPane(ta));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ce, "Save Failed: "+ex.getMessage());
            }
        }
    }

    private class LoadCircuitAction extends AbstractAction {
        
        private CircuitEditor ce;
        private Robot robot;
        
        public LoadCircuitAction(CircuitEditor ce, Robot robot) {
            super("Load Circuit");
            this.ce = ce;
            this.robot = robot;
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
                JTextArea ta = new JTextArea("Paste Circuit Here", 24, 80);
                JOptionPane.showMessageDialog(ce, new JScrollPane(ta));
                ByteArrayInputStream buf = new ByteArrayInputStream(ta.getText().getBytes());
                CircuitStore.load(buf, ce, robot);
                buf.close();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ce, "Load Failed: "+ex.getMessage());
            }
        }
    }

    
    private Playfield playfield;
    
    private List<PlayfieldModel> levels;
    
    private ImageIcon goalIcon;
    private ImageIcon robotIcon;
    private ImageIcon blackIcon;
    private ImageIcon whiteIcon;
    private ImageIcon redIcon;
    private ImageIcon greenIcon;
    private ImageIcon blueIcon;

    private SaveCircuitAction saveCircuitAction;
    private LoadCircuitAction loadCircuitAction;

    private JFrame editorFrame;

    private JFrame playfieldFrame;

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
        playfieldFrame = new JFrame("CakeBots!");

        try {
            URL levelMapURL = ClassLoader.getSystemResource(DEFAULT_MAP_RESOURCE_PATH);
            if (levelMapURL == null) {
                throw new RuntimeException(
                        "Could not find default map at resource path " +
                        DEFAULT_MAP_RESOURCE_PATH);
            }
            URLConnection levelMapURLConnection = levelMapURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    levelMapURLConnection.getInputStream()));
            
            Pattern magicPattern = Pattern.compile("^ROCKY ([0-9]+)\\.([0-9]+)$");
            String magic = in.readLine();
            Matcher magicMatcher = magicPattern.matcher(magic);
            if (!magicMatcher.matches()) {
                throw new IOException("Bad magic! This is not a robot applet map file!");
            }
            int major = Integer.parseInt(magicMatcher.group(1));
            int minor = Integer.parseInt(magicMatcher.group(2));
            if (major != 2) {
                throw new IOException(
                        "Map file major version "+major+
                        " is not supported (only version 2 is supported)");
            }
            System.out.println("Reading map file version "+major+"."+minor);
            
            levels = new ArrayList<PlayfieldModel>();
            String line;
            while ((line = in.readLine()) != null) {
                String levelName = line;
                System.out.print("Found level \""+levelName+"\"");
                
                int xSize = Integer.parseInt(in.readLine());
                int ySize = Integer.parseInt(in.readLine());
                System.out.println(" ("+xSize+"x"+ySize+")");
                
                float initialX = Float.parseFloat(in.readLine());
                float initialY = Float.parseFloat(in.readLine());
                Point2D.Float initialPosition = new Point2D.Float(initialX, initialY);
                
                float roboStepSize = Float.parseFloat(in.readLine());
                
                Square[][] map = new Square[xSize][ySize];
                
                // read the level map (short lines are padded with spaces)
                int lineNum = 0;
                for (;;) {
                    line = in.readLine();
                    if (line == null) break;
                    if (line.equals("*")) break;
                    System.out.println(line);
                    for (int i = 0; i < xSize; i++) {
                        if (i < line.length()) {
                            map[i][lineNum] = new Square(line.charAt(i));
                        } else {
                            map[i][lineNum] = new Square(Square.EMPTY);
                        }
                    }
                    lineNum += 1;
                }
                
                // pad out unspecified lines with spaces
                for (; lineNum < ySize; lineNum++) {
                    for (int i = 0; i < xSize; i++) {
                        map[i][lineNum] = new Square(Square.EMPTY);
                    }
                }
                
                PlayfieldModel pf = new PlayfieldModel(
                        map, levelName, initialPosition, roboStepSize);
                levels.add(pf);
            }
            
            goalIcon = new ImageIcon(ClassLoader.getSystemResource("ROBO-INF/images/cake.png"));
            robotIcon = new ImageIcon(ClassLoader.getSystemResource("ROBO-INF/images/robot.png"));
            
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
        level = levelNum;
        final PlayfieldModel pfModel = levels.get(levelNum);
        final Robot robot = new Robot(pfModel, robotIcon);
        playfield = new Playfield(pfModel, robot);
        final CircuitEditor ce = new CircuitEditor(robot.getOutputs(), robot.getInputsGate(), sm);
        
        saveCircuitAction = new SaveCircuitAction(ce);
        loadCircuitAction = new LoadCircuitAction(ce, robot);
        
        playfield.setGoalIcon(goalIcon);
        playfield.setBlackIcon(blackIcon);
        playfield.setWhiteIcon(whiteIcon);
        playfield.setRedIcon(redIcon);
        playfield.setGreenIcon(greenIcon);
        playfield.setBlueIcon(blueIcon);

        System.out.println("Starting level "+pfModel.getName());
        
        final GameLoop gameLoop = new GameLoop(robot, playfield, ce);

        final JSpinner frameDelaySpinner = new JSpinner();
        frameDelaySpinner.setValue(new Integer(50));

        final JButton startButton = new JButton("Start");
        final JButton pauseButton = new JButton("Pause");
        final JButton resetButton = new JButton("Reset");
        final JButton stepButton = new JButton("Step");
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        stepButton.setEnabled(false);
        resetButton.setEnabled(false);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!gameLoop.isRunning()) {
                    gameLoop.setFrameDelay(((Integer) frameDelaySpinner.getValue()).intValue());
                    new Thread(gameLoop).start();
                    pauseButton.setEnabled(true);
                    startButton.setEnabled(false);
                    stepButton.setEnabled(false);
                    resetButton.setEnabled(false);
                }
            }
        });
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameLoop.requestStop();
                startButton.setEnabled(true);
                pauseButton.setEnabled(false);
                stepButton.setEnabled(true);
                resetButton.setEnabled(true);
            }
        });
        stepButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameLoop.singleStep();
            }
        });
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gameLoop.resetState();
                playfield.repaint();
                ce.repaint();
                startButton.setEnabled(true);
                pauseButton.setEnabled(false);
                stepButton.setEnabled(false);
                resetButton.setEnabled(false);
            }
        });
        final JButton saveButton = new JButton();
        saveButton.setAction(saveCircuitAction);

        final JButton loadButton = new JButton();
        loadButton.setAction(loadCircuitAction);

        gameLoop.addPropertyChangeListener("running", new PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (!gameLoop.isRunning()) {
                            if (gameLoop.isGoalReached()) {
                                Graphics g = playfield.getGraphics();
                                g.setFont(g.getFont().deriveFont(50f));
                                g.setColor(Color.BLACK);
                                g.drawString("CAKE! You Win?", 20, playfield.getHeight()/2);
                                g.setColor(Color.RED);
                                g.drawString("CAKE! You Win!", 15, playfield.getHeight()/2-5);
                                sm.play("win");
                                startButton.setEnabled(false);
                                pauseButton.setEnabled(false);
                                stepButton.setEnabled(false);
                                resetButton.setEnabled(true);
                            }
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
        topButtonPanel.add(pauseButton);
        topButtonPanel.add(stepButton);
        topButtonPanel.add(resetButton);
        topButtonPanel.add(loadButton);
        topButtonPanel.add(saveButton);
        
        JPanel bottomButtonPanel = new JPanel(new FlowLayout());
        bottomButtonPanel.add(new JLabel("Delay between frames (ms):"));
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
