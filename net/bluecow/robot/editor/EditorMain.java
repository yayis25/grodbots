/*
 * Created on Aug 25, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.bluecow.robot.CircuitEditor;
import net.bluecow.robot.Direction;
import net.bluecow.robot.FileFormatException;
import net.bluecow.robot.GameConfig;
import net.bluecow.robot.GameLoop;
import net.bluecow.robot.GameState;
import net.bluecow.robot.GameStateHandler;
import net.bluecow.robot.LevelConfig;
import net.bluecow.robot.Playfield;
import net.bluecow.robot.Robot;
import net.bluecow.robot.RobotUtils;
import net.bluecow.robot.SoundManager;
import net.bluecow.robot.GameConfig.GateConfig;
import net.bluecow.robot.GameConfig.SensorConfig;
import net.bluecow.robot.GameConfig.SquareConfig;
import net.bluecow.robot.LevelConfig.Switch;
import net.bluecow.robot.editor.resource.ResourcesComboBoxModel;
import net.bluecow.robot.gate.Gate;
import net.bluecow.robot.sprite.Sprite;
import net.bluecow.robot.sprite.SpriteFileFilter;
import net.bluecow.robot.sprite.SpriteManager;

public class EditorMain {

    public static final Dimension DEFAULT_LEVEL_SIZE = new Dimension(15,10);

    private static final String BSH_ID_REGEX = "[A-Za-z_][A-Za-z0-9_]*";
    
    private class CloseProjectAction extends AbstractAction {

        public CloseProjectAction() {
            super("Close Project");
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
        }
        
        public void actionPerformed(ActionEvent e) {
            closeProject();
            presentWelcomeMenu();
        }
    }
    
    private class LoadProjectAction extends AbstractAction {

        public LoadProjectAction() {
            super("Open Project...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }
        
        public void actionPerformed(ActionEvent e) {
            if (closeProject()) {
                Project proj = promptUserForProject();
                if (proj != null) {
                    new EditorMain(proj);
                } else {
                    presentWelcomeMenu();
                }
            }
        }
    }
    
    /**
     * Handles both the "save" and "save as" actions for the project.
     */
    private class SaveAction extends AbstractAction {
        
        private final boolean saveAs;
        
        public SaveAction(boolean saveAs) {
            super(saveAs ? "Save As..." : "Save");
            if (saveAs) {
                putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            } else {
                putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            }
            this.saveAs = saveAs;
        }
        
        public void actionPerformed(ActionEvent e) {
            Preferences recentFiles = RobotUtils.getPrefs().node("recentGameFiles");
            Writer out = null;
            if (saveAs || project.getFileLocation() == null) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save Project");
                fc.setCurrentDirectory(new File(recentFiles.get("0", System.getProperty("user.home"))));
                int choice = fc.showSaveDialog(frame);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    project.setFileLocation(fc.getSelectedFile());
                } else {
                    return;
                }
            }
            try {
                project.saveLevelPack(null);
                setFrameTitle(project.getFileLocation().getName());
                RobotUtils.updateRecentFiles(recentFiles, project.getFileLocation());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Save Failed: "+ex.getMessage());
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
    }

    private Action addSquareTypeAction = new AbstractAction("Add Square Type") {
        public void actionPerformed(ActionEvent e) {
            SquareConfig squareConfig = new GameConfig.SquareConfig();
            JDialog d = makeSquarePropsDialog(frame, project, squareConfig);
            d.setModal(true);
            d.setVisible(true);
            project.getGameConfig().addSquareType(squareConfig);
        }
    };

    private Action removeSquareTypeAction = new AbstractAction("Remove Square Type") {
        public void actionPerformed(ActionEvent e) {
            final SquareConfig squareConfig = (SquareConfig) squareList.getSelectedValue();
            final int oldIndex = squareList.getSelectedIndex();
            project.getGameConfig().removeSquareType(squareConfig);
            squareList.setSelectedIndex(
                    Math.min(squareList.getModel().getSize() - 1, oldIndex));
        }
    };
    
    private Action addSensorTypeAction = new AbstractAction("Add Sensor Type") {
        public void actionPerformed(ActionEvent e) {
            SensorConfig sensorConfig = new GameConfig.SensorConfig("");
            JDialog d = makeSensorPropsDialog(frame, project.getGameConfig(), sensorConfig);
            d.setModal(true);
            d.setVisible(true);
            project.getGameConfig().addSensorType(sensorConfig);
        }
    };

    private Action removeSensorTypeAction = new AbstractAction("Remove Sensor Type") {
        public void actionPerformed(ActionEvent e) {
            final SensorConfig sensorConfig = (SensorConfig) sensorTypesList.getSelectedValue();
            final int oldIndex = sensorTypesList.getSelectedIndex();
            project.getGameConfig().removeSensorType(sensorConfig);
            sensorTypesList.setSelectedIndex(
                    Math.min(sensorTypesList.getModel().getSize() - 1, oldIndex));
        }
    };

    private Action addLevelAction = new AbstractAction("Add") {
        public void actionPerformed(ActionEvent e) {
            final LevelConfig currentLevel = (LevelConfig) levelChooser.getSelectedItem();
            final LevelConfig newLevel = new LevelConfig();
            newLevel.setName("New Level " + (1 + levelChooser.getModel().getSize()));
            if (currentLevel != null) {
                newLevel.setSize(currentLevel.getWidth(), currentLevel.getHeight());
            } else {
                newLevel.setSize(DEFAULT_LEVEL_SIZE.width, DEFAULT_LEVEL_SIZE.height);
            }
            project.getGameConfig().addLevel(newLevel);
            levelChooser.setSelectedItem(newLevel);
        }
    };

    private Action removeLevelAction = new AbstractAction("Remove") {
        public void actionPerformed(ActionEvent e) {
            final LevelConfig currentLevel = (LevelConfig) levelChooser.getSelectedItem();
            final int oldIndex = levelChooser.getSelectedIndex();
            project.getGameConfig().removeLevel(currentLevel);
            final int newIndex = Math.min(oldIndex, levelChooser.getModel().getSize()-1);
            levelChooser.setSelectedItem(null);
            levelChooser.setSelectedIndex(newIndex);
        }
    };

    private Action copyLevelAction = new AbstractAction("Duplicate") {
        public void actionPerformed(ActionEvent e) {
            final LevelConfig currentLevel = (LevelConfig) levelChooser.getSelectedItem();
            if (currentLevel == null) {
                JOptionPane.showMessageDialog(frame, "You have to select a level to copy it");
                return;
            }
            final LevelConfig newLevel = new LevelConfig(currentLevel);
            newLevel.setName(currentLevel.getName()+" Copy");
            project.getGameConfig().addLevel(newLevel);
            levelChooser.setSelectedItem(null);
            levelChooser.setSelectedIndex(levelChooser.getModel().getSize()-1);
        }
    };

    /**
     * An action that pops up a dialog for reordering the sequence of the levels.
     */
    private final OrganizeLevelsAction organizeLevelsAction;
    
    private Action playLevelAction = new AbstractAction("Play Test") {
        public void actionPerformed(ActionEvent e) {
            
            LevelConfig realLevel = (LevelConfig) levelChooser.getSelectedItem();
            if (realLevel == null) {
                JOptionPane.showMessageDialog(frame, "Can't play test: No level selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            final LevelConfig level = new LevelConfig(realLevel);
            final JFrame playtestFrame = new JFrame("Playtest");
            final Playfield playfield = new Playfield(project.getGameConfig(), level);
            final List<Window> windowsToDispose = new ArrayList<Window>();
            final SoundManager soundManager = new SoundManager(project.getResourceManager());
            
            // This makes the reset feature of GameStateHandler work properly.
            level.snapshotState();
            
            windowsToDispose.add(playtestFrame);

            List<Robot> robots = level.getRobots();
            
            GameLoop gameLoop = new GameLoop(robots, level, playfield);
            Map<Robot, CircuitEditor> editors = new HashMap<Robot, CircuitEditor>();
            for (Robot r : robots) {
                CircuitEditor ce = new CircuitEditor(
                        r.getCircuit(),
                        soundManager);
                editors.put(r, ce);
                JDialog d = new JDialog(playtestFrame, "Circuit for "+r.getLabel());
                d.add(ce);
                d.pack();
                d.setVisible(true);
                windowsToDispose.add(d);
            }

            final GameStateHandler gsh = new GameStateHandler(gameLoop, soundManager, editors);
            JButton quitPlaytestButton = new JButton("Quit Playtest");

            JPanel tb = new JPanel(new FlowLayout());
            tb.add(gsh.getStartButton());
            tb.add(gsh.getStepButton());
            tb.add(gsh.getResetButton());
            tb.add(quitPlaytestButton);
            
            playtestFrame.add(playfield, BorderLayout.CENTER);
            playtestFrame.add(tb, BorderLayout.SOUTH);
            
            playtestFrame.pack();
            playtestFrame.setVisible(true);
            
            frame.setVisible(false);
            
            RobotUtils.tileWindows(windowsToDispose);
            
            quitPlaytestButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    for (Window w : windowsToDispose) {
                        w.dispose();
                    }
                    gsh.setState(GameState.RESET);
                    frame.setVisible(true);
                }
            });
        }
    };
    
    private Action addRobotAction = new AbstractAction("Add Robot") {
        public void actionPerformed(ActionEvent e) {
            final LevelConfig level = (LevelConfig) levelChooser.getSelectedItem();
            final Robot robot = project.createRobot(level);
            ActionListener addRobot = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    level.addRobot(robot);
                    editor.addRobot(robot);
                    robotChooser.setSelectedValue(robot, true);
                }
            };
            JDialog d = makeRobotPropsDialog(frame, project, level, robot, addRobot);
            d.setModal(true);
            d.setVisible(true);
        }
    };
    
    private Action removeRobotAction = new AbstractAction("Remove Robot") {
        public void actionPerformed(ActionEvent e) {
            final LevelConfig level = (LevelConfig) levelChooser.getSelectedItem();
            final Robot robot = (Robot) robotChooser.getSelectedValue();
            final int oldIndex = robotChooser.getSelectedIndex();
            level.removeRobot(robot);
            editor.removeRobot(robot); // XXX Playfield doesn't do this automatically

            /*
             * this is necessary to make the spotlight position update
             * because the selection index doesn't necessarily change when we
             * delete a robot, and if it doesn't change, setSelectedIndex()
             * doesn't fire an event.
             */ 
            robotChooser.clearSelection();

            robotChooser.setSelectedIndex(
                    Math.min(robotChooser.getModel().getSize() - 1, oldIndex));
        }
    };

    private Action addSwitchAction = new AbstractAction("Add Switch") {
        public void actionPerformed(ActionEvent e) {
            final Switch sw = project.createSwitch();
            final LevelConfig level = (LevelConfig) levelChooser.getSelectedItem();
            ActionListener addSwitch = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    level.addSwitch(sw);
                    switchChooser.setSelectedValue(sw, true);
                }
            };
            JDialog d = makeSwitchPropsDialog(frame, project, level, sw, addSwitch);
            d.setModal(true);
            d.setVisible(true);
        }
    };
    
    private Action removeSwitchAction = new AbstractAction("Remove Switch") {
        public void actionPerformed(ActionEvent e) {
            final LevelConfig level = (LevelConfig) levelChooser.getSelectedItem();
            final Switch sw = (Switch) switchChooser.getSelectedValue();
            final int oldIndex = switchChooser.getSelectedIndex();
            level.removeSwitch(sw);

            /*
             * this is necessary to make the spotlight position update
             * because the selection index doesn't necessarily change when we
             * delete a switch, and if it doesn't change, setSelectedIndex()
             * doesn't fire an event.
             */ 
            switchChooser.clearSelection();

            switchChooser.setSelectedIndex(
                    Math.min(switchChooser.getModel().getSize() - 1, oldIndex));
        }
    };

    /**
     * The preferences node that stores a list of most recently saved
     * and opened project locations.
     */
    private static Preferences recentProjects = RobotUtils.getPrefs().node("recentProjects");

    /**
     * The project this editor is currently editing.
     */
    private Project project;

    private JFrame frame;
    private JComponent levelEditPanel;
    private LevelEditor editor;
    private JComboBox levelChooser;
    private LevelChooserListModel levelChooserListModel;
    private JList sensorTypesList;
    private SensorTypeListModel sensorTypeListModel;
    private JList squareList;
    private SquareChooserListModel squareChooserListModel;
    private JList robotChooser;
    private JList switchChooser;
    
    private Icon addItemIcon = new AddRemoveIcon(AddRemoveIcon.Type.ADD);
    private Icon removeItemIcon = new AddRemoveIcon(AddRemoveIcon.Type.REMOVE);
    
    private LoadProjectAction loadProjectAction = new LoadProjectAction();
    private SaveAction saveAction = new SaveAction(false);
    private SaveAction saveAsAction = new SaveAction(true);
    private CloseProjectAction closeProjectAction = new CloseProjectAction();

    
    private static JDialog makeSensorPropsDialog(final JFrame parent, GameConfig gc, final SensorConfig sc) {
        final JDialog d = new JDialog(parent, "Sensor Type Properties");
        final JTextField nameField = new JTextField(sc.getId() == null ? "" : sc.getId(), 20);
        final JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sc.setId(nameField.getText());
                    d.dispose();
                } catch (Exception ex) {
                    showException(parent, "Couldn't apply sensor config", ex);
                }
            }
        });

        // set up the form
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel cp = new JPanel(new GridBagLayout());
        gbc.weighty = 0.0;
        gbc.insets = new Insets(4, 4, 4, 4);
        
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Sensor Type Name:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(nameField, gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        cp.add(okButton, gbc);

        cp.getActionMap().put("cancel", new DialogCancelAction(d));
        cp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        cp.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        d.setContentPane(cp);
        d.getRootPane().setDefaultButton(okButton);
        d.pack();
        return d;
    }
    
    public static Project promptUserForProject() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose a Robot Project File");
        File recentProject = new File(recentProjects.get("0", System.getProperty("user.home")));
        if (recentProject.isDirectory()) {
            // for project directories, we want to default the dialog to the parent dir
            recentProject = recentProject.getParentFile();
        }
        fc.setCurrentDirectory(recentProject);
        int choice = fc.showOpenDialog(null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            Project proj = null;
            try {
                proj = Project.load(f);
                RobotUtils.updateRecentFiles(recentProjects, fc.getSelectedFile());
                recentProjects.put("autoLoadOk", "true");
                return proj;
            } catch (BackingStoreException ex) {
                System.out.println("Couldn't update user prefs");
                ex.printStackTrace();
            } catch (FileFormatException ex) {
                RobotUtils.showFileFormatException(ex);
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
            
            // load failed, but we still have to ensure proper cleanup!
            if (proj != null) {
                proj.close();
            }
        }

        // either load failed, or user cancelled
        return null;
    }

    private static JDialog makeSquarePropsDialog(final JFrame parent, final Project project, final SquareConfig sc) {
        final GameConfig gc = project.getGameConfig();
        final JDialog d = new JDialog(parent, "Square Type Properties");
        final JTextField nameField = new JTextField(sc.getName() == null ? "" : sc.getName(), 20);
        final JTextField mapCharField = new JTextField(String.valueOf(sc.getMapChar()));
        final JCheckBox occupiableBox = new JCheckBox("Occupiable", sc.isOccupiable());
        final JList sensorTypesList = new JList(gc.getSensorTypes().toArray());
        sensorTypesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // have to jump through hoops to make an array of selection indices
        List<Integer> sensorsToSelect = new ArrayList<Integer>();
        for (GameConfig.SensorConfig sensor : sc.getSensorTypes()) {
            sensorsToSelect.add(gc.getSensorTypes().indexOf(sensor));
        }
        int[] selectionIndices = new int[sensorsToSelect.size()];
        for (int i = 0; i < sensorsToSelect.size(); i++) {
            selectionIndices[i] = sensorsToSelect.get(i);
        }
        sensorTypesList.setSelectedIndices(selectionIndices);
        
        final JComboBox spritePathField = new JComboBox(
                new ResourcesComboBoxModel(project.getResourceManager(), new SpriteFileFilter()));
        if (sc.getSprite() != null) {
            spritePathField.setSelectedItem(sc.getSprite().getAttributes().get(Sprite.KEY_HREF));
        }
        final JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sc.setName(nameField.getText());
                    sc.setMapChar(mapCharField.getText().charAt(0));
                    sc.setOccupiable(occupiableBox.isSelected());
                    List<SensorConfig> sensorTypes = new ArrayList<SensorConfig>();
                    Object[] selectedItems = sensorTypesList.getSelectedValues();
                    for (Object sensor : selectedItems) {
                        sensorTypes.add((SensorConfig) sensor);
                    }
                    sc.setSensorTypes(sensorTypes);
                    sc.setSprite(SpriteManager.load(
                                            gc.getResourceLoader(),
                                            (String) spritePathField.getSelectedItem()));
                    d.dispose();
                } catch (Exception ex) {
                    showException(parent, "Couldn't apply square config", ex);
                }
            }
        });

        // set up the form
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel cp = new JPanel(new GridBagLayout());
        gbc.weighty = 0.0;
        gbc.insets = new Insets(4, 4, 4, 4);
        
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Square Type Name:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(nameField, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Map Character:"), gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(mapCharField, gbc);
        
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel(""), gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.LINE_START;
        cp.add(occupiableBox, gbc);

        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Sensor Types Activated:"), gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        cp.add(new JScrollPane(sensorTypesList), gbc);

        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Sprite File Location in Project:"), gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(spritePathField, gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        cp.add(okButton, gbc);

        cp.getActionMap().put("cancel", new DialogCancelAction(d));
        cp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        cp.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        d.setContentPane(cp);
        d.getRootPane().setDefaultButton(okButton);
        d.pack();
        return d;
    }
    
    /**
     * Creates a JDialog with a GUI for editing the properties of the given
     * robot.  The GUI will default to describing the current state of the robot.
     * The dialog has an OK button which, when pressed, will update the robot's
     * properties to reflect the new values in the GUI.
     * 
     * @param parent The frame that owns this dialog
     * @param project The project that the robot ultimately belongs to
     * @param robot The robot to edit
     * @param okAction An action to perform after the OK button has been
     * pressed and robot's properties have been updated.  This action will
     * only be invoked if the user OK's the dialog; it will not be invoked
     * (and the robot properties will not be modified) if the user cancels
     * the dialog.  Also, you can safely pass in <tt>null</tt> for this
     * action if you don't need to do anything when the user hits OK. 
     * @return A non-modal JDialog which has been pack()ed, but not set visible.
     * You are free to setModal(true) on the dialog before displaying it if
     * you want it to be modal. 
     */
    private static JDialog makeRobotPropsDialog(final JFrame parent,
            final Project project,
            final LevelConfig level, final Robot robot,
            final ActionListener okAction) {
        
        final GameConfig gameConfig = project.getGameConfig();
        
        final JDialog d = new JDialog(parent, "Robot Properties");

        final JTextField idField = new JTextField();
        final JTextField labelField = new JTextField();
        final JCheckBox labelEnabledBox = new JCheckBox("Label Enabled");
        final JComboBox labelDirectionBox = new JComboBox(Direction.values());
        final JSpinner xPosition = new JSpinner(new SpinnerNumberModel(0.0, 0.0, level.getWidth(), robot.getStepSize()));
        final JSpinner yPosition = new JSpinner(new SpinnerNumberModel(0.0, 0.0, level.getHeight(), robot.getStepSize()));
        final JComboBox spritePathField = new JComboBox(
                new ResourcesComboBoxModel(project.getResourceManager(), new SpriteFileFilter()));
        final JSpinner spriteScaleSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1000.0, 0.01));
        final JSpinner evalsPerStep = new JSpinner(new SpinnerNumberModel(1, 1, null, 1));
        final JSpinner stepSize = new JSpinner(new SpinnerNumberModel(0.1, 0.01, null, 0.01));
        
        final Map<GateConfig, JSpinner> gateAllowanceSpinnerList =
            new LinkedHashMap<GateConfig, JSpinner>();
        for (GateConfig gateConfig : gameConfig.getGateTypes()) {
            gateAllowanceSpinnerList.put(
                    gateConfig,
                    new JSpinner(new SpinnerNumberModel(0, 0, 100, 1)));
        }
        
        final Runnable uiUpdater = new Runnable() {
            public void run() {
                idField.setText(robot.getId());
                labelField.setText(robot.getLabel());
                labelEnabledBox.setSelected(robot.isLabelEnabled());
                labelDirectionBox.setSelectedItem(robot.getLabelDirection());
                xPosition.setValue(robot.getX());
                yPosition.setValue(robot.getY());
                if (robot.getSprite() != null) {
                    spritePathField.setSelectedItem(robot.getSprite().getAttributes().get(Sprite.KEY_HREF));
                    spriteScaleSpinner.setValue(robot.getSprite().getScale());
                }
                evalsPerStep.setValue(robot.getEvalsPerStep());
                stepSize.setValue(new Double(robot.getStepSize()));

                Map<Class<? extends Gate>, Integer> gateAllowances = robot.getCircuit().getGateAllowances();
                for (Map.Entry<GateConfig, JSpinner> entry : gateAllowanceSpinnerList.entrySet()) {
                    GateConfig gateConfig = entry.getKey();
                    JSpinner spinner = entry.getValue();
                    final Integer currentAllowance = gateAllowances.get(gateConfig.getGateClass());
                    spinner.setValue(currentAllowance == null ? 0 : currentAllowance.intValue());
                }
            }
        };
        uiUpdater.run();
        
        final JButton copyRobotButton = new JButton("Copy existing >");
        copyRobotButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPopupMenu menu = makeRobotChooserPopup(project.getGameConfig(), robot, uiUpdater);
                menu.show(copyRobotButton, 10, 10);
            }
        });

        final JButton cancelButton = new JButton(new DialogCancelAction(d));
        final JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Point2D pos = new Point2D.Double(
                        (Double) xPosition.getValue(),
                        (Double) yPosition.getValue());

                List<String> msgs = new ArrayList<String>();
                if (!Pattern.matches(BSH_ID_REGEX, idField.getText())) {
                    msgs.add("Robot ID must be a valid Java identifier " +
                            "(letter or underscore followed by zero or more " +
                            "letters, numbers, or underscores)");
                }
                if (pos.getX() < 0.0 || pos.getX() > level.getWidth()
                        || pos.getY() < 0.0 || pos.getY() > level.getHeight()) {
                    msgs.add("Position has to be within the current map dimensions" +
                            " (the map is currently "+level.getWidth()+"x"+level.getHeight()+")");
                }
                if (spritePathField.getSelectedItem() == null) {
                    msgs.add("You have to choose a sprite for your robot");
                }
                if (!msgs.isEmpty()) {
                    StringBuilder message = new StringBuilder();
                    message.append("Your input has errors.  Fix them.");
                    for (String msg : msgs) {
                        message.append("\n -").append(msg);
                    }
                    message.append("\n");
                    JOptionPane.showMessageDialog(d, message, "I'm sorry, Dave...", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    robot.setId(idField.getText());
                    robot.setLabel(labelField.getText());
                    robot.setLabelDirection((Direction) labelDirectionBox.getSelectedItem());
                    robot.setLabelEnabled(labelEnabledBox.isSelected());
                    robot.setPosition(pos);
                    robot.setStartPosition(pos);
                    
                    Sprite sprite = SpriteManager.load(
                            gameConfig.getResourceLoader(),
                            (String) spritePathField.getSelectedItem());
                    sprite.setScale((Double) spriteScaleSpinner.getValue());
                    robot.setSprite(sprite);
                    robot.setEvalsPerStep((Integer) evalsPerStep.getValue());
                    robot.setStepSize(((Double) stepSize.getValue()).floatValue());
                    
                    for (Map.Entry<GateConfig, JSpinner> entry : gateAllowanceSpinnerList.entrySet()) {
                        final GateConfig gateConfig = entry.getKey();
                        final JSpinner spinner = entry.getValue();
                        int allowance = ((Integer) spinner.getValue()).intValue();
                        robot.getCircuit().addGateAllowance(gateConfig.getGateClass(), allowance);
                    }
                    
                    if (okAction != null) {
                        okAction.actionPerformed(e);
                    }

                } catch (Exception ex) {
                    String message;
                    if (ex.getMessage() != null) {
                        message = ex.getMessage();
                    } else {
                        message = "Couldn't update Robot properties";
                    }
                    showException(parent, message, ex);
                    return;
                }
                
                d.setVisible(false);
                d.dispose();
            }
        });
        
        // set up the form
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel cp = new JPanel(new GridBagLayout());
        gbc.weighty = 0.0;
        gbc.insets = new Insets(4, 4, 4, 4);
        
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Robot ID:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(idField, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Position:"), gbc);

        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(xPosition, gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(yPosition, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Label:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(labelField, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel(""), gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(labelEnabledBox, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Label Direction:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(labelDirectionBox, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Sprite Path:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(spritePathField, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Sprite Scale:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(spriteScaleSpinner, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Evals per step:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(evalsPerStep, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Step size (squares):"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(stepSize, gbc);

        for (Map.Entry<GateConfig, JSpinner> entry : gateAllowanceSpinnerList.entrySet()) {
            final GateConfig gateConfig = entry.getKey();
            final JSpinner spinner = entry.getValue();
            
            gbc.weightx = 0.0;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.LINE_END;
            cp.add(new JLabel("Number of " + gateConfig.getName() + " gates:"), gbc);

            gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            cp.add(spinner, gbc);
        }
        
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START;
        cp.add(copyRobotButton, gbc);

        // XXX not ideal layout
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(cancelButton, gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        cp.add(okButton, gbc);

        cp.getActionMap().put("cancel", new DialogCancelAction(d));
        cp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        cp.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        d.setContentPane(cp);
        d.getRootPane().setDefaultButton(okButton);
        d.pack();
        return d;
    }
    
    /**
     * Works very much like {@link #makeRobotPropsDialog(JFrame, Project, Robot, ActionListener)},
     * but is for editing switch properties.
     */
    private static JDialog makeSwitchPropsDialog(final JFrame parent,
            final Project project, final LevelConfig level, final Switch sw,
            final ActionListener okAction) {
        final JDialog d = new JDialog(parent, "Switch Properties");

        final JTextField idField = new JTextField();
        final JCheckBox switchEnabledBox = new JCheckBox("Start enabled");
        final JSpinner xPosition = new JSpinner();
        final JSpinner yPosition = new JSpinner();
        final JTextField labelField = new JTextField();
        final JCheckBox labelEnabledBox = new JCheckBox("Label Enabled");
        final JComboBox labelDirectionBox = new JComboBox(Direction.values());
        final JComboBox spritePathField = new JComboBox(
                new ResourcesComboBoxModel(project.getResourceManager(), new SpriteFileFilter()));
        final JSpinner spriteScaleSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1000.0, 0.01));
        final JTextArea onEnterArea = new JTextArea(6, 15);

        // a simple Runnable that updates all the swing components to the current switch properties
        final Runnable uiUpdater = new Runnable() {
            public void run() {
                idField.setText(sw.getId());
                switchEnabledBox.setSelected(sw.isEnabled());
                xPosition.setModel(new SpinnerNumberModel(sw.getX(), 0, level.getWidth(), 1));
                yPosition.setModel(new SpinnerNumberModel(sw.getY(), 0, level.getHeight(), 1));
                labelField.setText(sw.getLabel());
                labelEnabledBox.setSelected(sw.isLabelEnabled());
                labelDirectionBox.setSelectedItem(sw.getLabelDirection());
                if (sw.getSprite() != null) {
                    spritePathField.setSelectedItem(sw.getSprite().getAttributes().get(Sprite.KEY_HREF));
                    spriteScaleSpinner.setValue(sw.getSprite().getScale());
                } else {
                    spritePathField.setSelectedItem(null);
                    spriteScaleSpinner.setValue(new Double(1.0));
                }
                onEnterArea.setText(sw.getOnEnter());
            }
        };
        uiUpdater.run();  // fill in the initial values from the switch
        
        final JButton copySwitchButton = new JButton("Copy existing >");
        copySwitchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPopupMenu menu = makeSwitchChooserPopup(project.getGameConfig(), sw, uiUpdater);
                menu.show(copySwitchButton, 10, 10);
            }
        });
        final JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sw.setId(idField.getText());
                    sw.setEnabled(switchEnabledBox.isSelected());
                    Point pos = new Point();
                    pos.x = (Integer) xPosition.getValue();
                    pos.y = (Integer) yPosition.getValue();
                    sw.setPosition(pos);
                    sw.setLabel(labelField.getText());
                    sw.setLabelDirection((Direction) labelDirectionBox.getSelectedItem());
                    sw.setLabelEnabled(labelEnabledBox.isSelected());
                    Sprite sprite = SpriteManager.load(
                            project.getGameConfig().getResourceLoader(),
                            (String) spritePathField.getSelectedItem());
                    sprite.setScale((Double) spriteScaleSpinner.getValue());
                    sw.setSprite(sprite);
                    sw.setOnEnter(onEnterArea.getText());
                    
                    if (okAction != null) {
                        okAction.actionPerformed(e);
                    }

                } catch (Exception ex) {
                    showException(parent, "Couldn't update Switch properties", ex);
                }
                d.setVisible(false);
                d.dispose();
            }
        });
        
        // set up the form
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel cp = new JPanel(new GridBagLayout());
        gbc.weighty = 0.0;
        gbc.insets = new Insets(4, 4, 4, 4);
        
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Switch ID:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(idField, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel(""), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(switchEnabledBox, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Position:"), gbc);

        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(xPosition, gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(yPosition, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Label:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(labelField, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel(""), gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(labelEnabledBox, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Label Direction:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(labelDirectionBox, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Sprite:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(spritePathField, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("Sprite Scale:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(spriteScaleSpinner, gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        cp.add(new JLabel("On Enter Script:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(new JScrollPane(onEnterArea), gbc);

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START;
        cp.add(copySwitchButton, gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        cp.add(okButton, gbc);
        
        cp.getActionMap().put("cancel", new DialogCancelAction(d));
        cp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        cp.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        d.setContentPane(cp);
        d.getRootPane().setDefaultButton(okButton);
        d.pack();
        return d;
    }
    
    private static JPopupMenu makeRobotChooserPopup(
            GameConfig game,
            final Robot robot,
            final Runnable runAfterSelection) {
        JPopupMenu menu = new JPopupMenu();
        for (LevelConfig level : game.getLevels()) {
            JMenu item = new JMenu(level.getName());
            menu.add(item);
            for (Robot levelRobot : level.getRobots()) {
                final Robot flr = levelRobot;
                Action a = new AbstractAction(flr.getId()+" ("+flr.getLabel()+")") {
                    public void actionPerformed(ActionEvent e) {
                        robot.copyFrom(flr, robot.getLevel());
                        if (runAfterSelection != null) {
                            runAfterSelection.run();
                        }
                    }
                };
                item.add(new JMenuItem(a));
            }
        }
        return menu;
    }

    private static JPopupMenu makeSwitchChooserPopup(
            GameConfig game,
            final Switch sw,
            final Runnable runAfterSelection) {
        JPopupMenu menu = new JPopupMenu();
        for (LevelConfig level : game.getLevels()) {
            JMenu item = new JMenu(level.getName());
            menu.add(item);
            for (Switch levelSwitch : level.getSwitches()) {
                final Switch fls = levelSwitch;
                Action a = new AbstractAction(fls.getId()+" ("+fls.getLabel()+")") {
                    public void actionPerformed(ActionEvent e) {
                        sw.copyFrom(fls);
                        if (runAfterSelection != null) {
                            runAfterSelection.run();
                        }
                    }
                };
                item.add(new JMenuItem(a));
            }
        }
        return menu;
    }

    public EditorMain(Project project) {
        this.project = project;
        
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit();
            }
        });
        
        final GameConfig myGameConfig = project.getGameConfig();
        
        organizeLevelsAction = new OrganizeLevelsAction(frame, myGameConfig);
        
        frame.getContentPane().setLayout(new BorderLayout(8, 8));
        ((JComponent) frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        
        JPanel levelChooserPanel = new JPanel(new FlowLayout());
        levelChooserListModel = new LevelChooserListModel(myGameConfig);
        levelChooser = new JComboBox(levelChooserListModel);
        levelChooser.setRenderer(new LevelChooserListRenderer());
        levelChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LevelConfig level = (LevelConfig) levelChooser.getSelectedItem();
                setLevelToEdit(level);
            }
        });
        levelChooserPanel.add(new JLabel("Level:"));
        levelChooserPanel.add(levelChooser);
        JPanel buttonPanel = makeButtonPanel(addLevelAction, removeLevelAction, copyLevelAction, playLevelAction);
        levelChooserPanel.add(buttonPanel);
        
        frame.add(levelChooserPanel, BorderLayout.NORTH);
        
        levelEditPanel = new JPanel();
        levelEditPanel.add(new JLabel("To edit a level, select it from the list on the left-hand side."));
        frame.add(levelEditPanel, BorderLayout.CENTER);
        
        JPanel sensorTypesPanel = new JPanel(new BorderLayout());
        sensorTypeListModel = new SensorTypeListModel(myGameConfig);
        sensorTypesList = new JList(sensorTypeListModel);
        sensorTypesPanel.add(new JLabel("Sensor Types"), BorderLayout.NORTH);
        sensorTypesPanel.add(new JScrollPane(
                    sensorTypesList,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
                BorderLayout.CENTER);
        sensorTypesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    GameConfig.SensorConfig sc = (SensorConfig) sensorTypesList.getSelectedValue();
                    if (sc != null) {
                        makeSensorPropsDialog(frame, getProject().getGameConfig(), sc).setVisible(true);
                    }
                }
            }
        });
        sensorTypesPanel.add(
                makeAddRemoveButtonPanel(addSensorTypeAction, removeSensorTypeAction),
                BorderLayout.SOUTH);
        
        squareChooserListModel = new SquareChooserListModel(myGameConfig);
        squareList = new JList(squareChooserListModel);
        squareList.setCellRenderer(new SquareChooserListRenderer());
        squareList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (editor != null) {
                    editor.setPaintingSquareType((SquareConfig) squareList.getSelectedValue());
                }
            }
        });
        JPanel squareListPanel = new JPanel(new BorderLayout());
        squareListPanel.add(new JLabel("Square Types"), BorderLayout.NORTH);
        squareListPanel.add(
                new JScrollPane(squareList,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
                BorderLayout.CENTER);
        squareList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    GameConfig.SquareConfig sc = (SquareConfig) squareList.getSelectedValue();
                    if (sc != null) {
                        makeSquarePropsDialog(frame, getProject(), sc).setVisible(true);
                    }
                }
            }
        });
        squareListPanel.add(
                makeAddRemoveButtonPanel(addSquareTypeAction, removeSquareTypeAction),
                BorderLayout.SOUTH);
        JSplitPane eastPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        eastPanel.setTopComponent(sensorTypesPanel);
        eastPanel.setBottomComponent(squareListPanel);
        
        frame.add(eastPanel, BorderLayout.EAST);
        
        setupMenu();

        if (project.getFileLocation() != null) {
            setFrameTitle(project.getFileLocation().getName());
        } else {
            setFrameTitle(null);
        }
        
        // this pack is required to realize the frame and get the layout going.
        // the alternative would be frame.setVisible(true), but that would cause
        // the following rearrangements to be visible to the user (yuck!)
        frame.pack();
        
        Dimension frameSize = Toolkit.getDefaultToolkit().getScreenSize();
        frameSize.width = Math.min(800, frameSize.width);
        frameSize.height = Math.min(800, frameSize.height);
        frame.setSize(frameSize);
        frame.setLocationRelativeTo(null);
        
        // must come after frame is realized and sized because the split pane
        // divider location is calculated relative to its height
        levelChooser.setSelectedIndex(0);
        
        frame.setVisible(true);
    }

    /**
     * Creates a panel with any number of buttons in a centered FlowLayout. The
     * first action will be in a button on the left, and subsequent actions will
     * be to the right of it.
     */
    private JPanel makeButtonPanel(Action ... actions) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        for (Action action : actions) {
            buttonPanel.add(new JButton(action));
        }
        return buttonPanel;
    }
    
    /**
     * Makes an iTunes-style add/remove button set.
     * 
     * @param addAction The action to perform when the add button is pressed
     * @param removeAction The action to perform when the remove button is pressed
     * @return A panel with two fixed-size buttons in it, left aligned with
     * no gap between them.  The add button has the addItemIcon on it (a "+" symbol
     * by default); the remove button has the remvoeItemIcon on it (a "-" symbol by
     * default). 
     */
    private JPanel makeAddRemoveButtonPanel(Action addAction, Action removeAction) {
        Dimension buttonSize = new Dimension(22,22);  // largest size for square-cornered OS X buttons
        
        JButton add = new JButton(addAction);
        add.setPreferredSize(buttonSize);
        add.setText(null);
        add.setIcon(addItemIcon);
        
        JButton remove = new JButton(removeAction);
        remove.setPreferredSize(buttonSize);
        remove.setText(null);
        remove.setIcon(removeItemIcon);
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.add(add);
        p.add(remove);
        
        return p;
    }
    /**
     * Sets up all the menu bar crap and adds it to the frame.
     */
    private void setupMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu m;
        mb.add (m = new JMenu("File"));
        m.add(new JMenuItem(loadProjectAction));
        m.add(new JMenuItem(saveAction));
        m.add(new JMenuItem(saveAsAction));
        m.add(new JMenuItem(closeProjectAction));
        m.add(new JMenuItem(exitAction));
        
        mb.add(m = new JMenu("Level"));
        m.add(new JMenuItem(addLevelAction));
        m.add(new JMenuItem(copyLevelAction));
        m.add(new JMenuItem(removeLevelAction));
        m.addSeparator();
        m.add(new JMenuItem(organizeLevelsAction));
        
        frame.setJMenuBar(mb);
    }
    
    /**
     * Invokes the confirmExit() method.
     */
    private Action exitAction = new AbstractAction("Exit") {
        public void actionPerformed(ActionEvent e) {
            confirmExit();
        }
    };

    /**
     * Returns the project that this editor is working with.
     */
    private Project getProject() {
        return project;
    }

    /**
     * Presents an "are you sure?" dialog and exits the application if the user
     * responds affirmitavely.
     * <p>
     * This method differs from {@link #closeProject()} in the following ways:
     * <ul>
     *  <li>It does not disable auto-loading the current project next time the editor starts
     *  <li>The message in the dialog is more appropriate to quitting the whole application
     *  <li>This method can terminate the JVM; closeProject() will redisplay the welcome menu
     * </ul>
     */
    public void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(frame, "Do you really want to quit?", "Quit the level editor", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (project != null) {
                project.close();
            }
            System.exit(0);
        }
    }
    
    /**
     * Changes the title of the frame to include the given string.  This method insures
     * that the title will always contain the phrase "Robot Level Editor".
     * 
     * @param title The string to display in the editor frame's title, in addition
     * to the set phrase "Robot Level Editor".
     */
    public void setFrameTitle(String title) {
        StringBuilder newTitle = new StringBuilder();
        newTitle.append("Robot Level Editor");
        if (title != null) {
            newTitle.append(" - ").append(title);
        }
        frame.setTitle(newTitle.toString());
    }
    
    /**
     * After prompting the user to confirm they really want to close the 
     * project, shuts down this editor with respect to the current project.
     * When it returns true, this method will have released any and all system
     * resources associated with the current editor instance, including the
     * Window object.  This method does not terminate the JVM.
     * <p>
     * See also {@link #confirmExit()}.
     * 
     * @return true if the user wants to close the project (and therefore the
     *         frame has been disposed); false otherwise.
     */
    private boolean closeProject() {
        int choice = JOptionPane.showOptionDialog(
                frame,
                "Really close the current project?\n" +
                "You will lose any unsaved changes.",
                "Close project", 0, JOptionPane.QUESTION_MESSAGE, null,
                new String[] {"Close", "Keep Working"}, "Close");
        if (choice != 0) return false;
        
        project.close();
        frame.dispose();
        recentProjects.put("autoLoadOk", "false");
        return true;
    }
    
    /**
     * Uninstalls the old levelEditPanel and level editor, and replaces them either
     * with new ones that are set up for editing the given level, or a simple message
     * panel if given <tt>null</tt> instead of a level.
     * 
     * @param level The level to set up an editor GUI for, or null to uninstall
     * the current editor GUI and repalce it with a placeholder panel.
     */
    private void setLevelToEdit(LevelConfig level) {
        if (levelEditPanel != null) {
            frame.remove(levelEditPanel);
        }
        if (level == null) {
            editor = null;
            levelEditPanel = new JPanel(new GridLayout(1,1));
            levelEditPanel.add(
                    new JLabel(
                            "Please select a level from the list, " +
                            "or create a new one!"));
        } else {
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setContinuousLayout(true);
            levelEditPanel = splitPane;
            editor = new LevelEditor(project.getGameConfig(), level);

            final JCheckBox showDescriptionBox = new JCheckBox("Show Level Description", editor.isDescriptionOn());
            showDescriptionBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editor.setDescriptionOn(showDescriptionBox.isSelected());
                }
            });
            final JCheckBox showLabelsBox = new JCheckBox("Show Labels", editor.isLabellingOn());
            showLabelsBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editor.setLabellingOn(showLabelsBox.isSelected());
                }
            });

            JPanel floaterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            floaterPanel.add(editor);

            Box editorOptionsPanel = new Box(BoxLayout.Y_AXIS);
            editorOptionsPanel.add(Box.createGlue());
            editorOptionsPanel.add(showDescriptionBox);
            editorOptionsPanel.add(showLabelsBox);
            editorOptionsPanel.add(Box.createGlue());

            JPanel editorPanel = new JPanel(new BorderLayout(10, 10));
            editorPanel.add(floaterPanel, BorderLayout.CENTER);
            editorPanel.add(editorOptionsPanel, BorderLayout.EAST);

            splitPane.setTopComponent(makeLevelPropsPanel(level));
            splitPane.setBottomComponent(editorPanel);
            
            editor.setPaintingSquareType((SquareConfig) squareList.getSelectedValue());
        }
        
        frame.add(levelEditPanel, BorderLayout.CENTER);
        frame.validate();
        
        // have to do this after validate to ensure the split pane has been positioned by the layout manager 
        if (levelEditPanel instanceof JSplitPane) {
            final JSplitPane splitPane = ((JSplitPane) levelEditPanel);
            int newDividerLoc = splitPane.getHeight() - splitPane.getBottomComponent().getPreferredSize().height;
            System.out.println("split pane height: "+splitPane.getHeight());
            System.out.println("editor panel pref size: "+splitPane.getBottomComponent().getPreferredSize());
            splitPane.setDividerLocation(newDividerLoc);
        }
    }
    
    /**
     * Creates a panel for editing non-nested level properties (this
     * is only name and description at the moment).
     * 
     * @param level The level to create a property editor for.  Must not
     * be <tt>null</tt>.
     */
    private JPanel makeLevelPropsPanel(final LevelConfig level) {
        
        final JTextArea descriptionArea = new JTextArea(level.getDescription(), 8, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        final JTextField levelNameField = new JTextField(level.getName());
        final DocumentListener updateListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { update(); }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            void update() {
                level.setName(levelNameField.getText());
                level.setDescription(descriptionArea.getText());
            }
        };
        levelNameField.getDocument().addDocumentListener(updateListener);
        descriptionArea.getDocument().addDocumentListener(updateListener);

        final JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(level.getWidth(), 1, 100, 1));
        final JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(level.getHeight(), 1, 100, 1));
        final ChangeListener spinnerListener = new ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int width = ((Integer) widthSpinner.getValue()).intValue();
                int height = ((Integer) heightSpinner.getValue()).intValue();
                level.setSize(width, height);
            }
        };
        widthSpinner.addChangeListener(spinnerListener);
        heightSpinner.addChangeListener(spinnerListener);
        
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel p = new JPanel(new GridBagLayout());
        
        // all components in the layout will have 4px of space around them
        gbc.insets = new Insets(4, 4, 4, 4);
        
        gbc.weighty = 0.0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        p.add(new JLabel("Level Name:"), gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        p.add(levelNameField, gbc);
        
        gbc.weighty = 0.0;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        p.add(new JLabel("Size (width, height):"), gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        gbc.weightx = 0.5;
        p.add(widthSpinner, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weighty = 0.0;
        gbc.weightx = 0.5;
        p.add(heightSpinner, gbc);

        gbc.weighty = 0.5;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        p.add(new JLabel("Description (HTML):"), gbc);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.5;
        p.add(new JScrollPane(descriptionArea), gbc);
        
        // filler (next 3 rows don't have left-hand labels)
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        p.add(new JPanel(), gbc);

        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        p.add(new JLabel("Robots"), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        p.add(new JLabel("Switches"), gbc);
        
        robotChooser = new JList(new RobotListModel(level));
        switchChooser = new JList(new SwitchListModel(level));
        
        gbc.gridwidth = 1;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        robotChooser.setCellRenderer(new RobotListRenderer());
        robotChooser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    Robot robot = (Robot) robotChooser.getSelectedValue();
                    if (robot != null) {
                        JDialog d = makeRobotPropsDialog(frame, project, level, robot, null);
                        d.setVisible(true);
                    }
                }
            }
        });
        robotChooser.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                Robot r = (Robot) robotChooser.getSelectedValue();
                if (r == null) {
                    editor.setSpotlightLocation(null);
                    ((Playfield) editor).setSpotlightRadius(0.0);
                } else {
                    switchChooser.clearSelection();
                    editor.setSpotlightLocation(r.getPosition());
                    editor.setSpotlightRadius(1.0);
                }
            }
        });
        p.add(new JScrollPane(robotChooser), gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        switchChooser.setCellRenderer(new SwitchListRenderer());
        switchChooser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    Switch sw = (Switch) switchChooser.getSelectedValue();
                    if (sw != null) {
                        JDialog d = makeSwitchPropsDialog(frame, project, level, sw, null);
                        d.setVisible(true);
                    }
                }
            }
        });
        switchChooser.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                Switch s = (Switch) switchChooser.getSelectedValue();
                if (s == null) {
                    editor.setSpotlightLocation(null);
                    ((Playfield) editor).setSpotlightRadius(0.0);
                } else {
                    robotChooser.clearSelection();
                    Point p = s.getPosition();
                    editor.setSpotlightLocation(new Point2D.Double(p.x+0.5, p.y+0.5));
                    editor.setSpotlightRadius(1.0);
                }
            }
        });
        p.add(new JScrollPane(switchChooser), gbc);

        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel addRemovePanel = makeAddRemoveButtonPanel(addRobotAction, removeRobotAction);
        p.add(addRemovePanel, gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        JPanel buttonPanel = makeAddRemoveButtonPanel(addSwitchAction, removeSwitchAction);
        p.add(buttonPanel, gbc);

        return p;
    }

    /**
     * Shows the given message and the exception's message and stack trace
     * in a modal dialog.
     */
    public static void showException(Component owner, String message, Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        pw.flush();
        pw.close();
        
        JTextArea ta = new JTextArea(message+"\n\n"+sw.getBuffer(), 15, 60);
        JOptionPane.showMessageDialog(owner, new JScrollPane(ta), "Error Report", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Starts the level editor application.
     * 
     * @param args The command-line arguments are ignored
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    if (!autoloadMostRecentProject()) {
                        presentWelcomeMenu();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Fatal error:\n\n"+e
                            +"\n\nMore information is available on the system or Java console.");
                    System.exit(0);
                }
            }
        });
    }

    /**
     * Checks for a "most recently opened project" file in the Java preferences
     * node associated with this program.  If there is a most-recent project,
     * and autoload isn't disabled (also controlled by a preference), then this
     * method attempts to load it into a new editor instance.
     * <p>
     * This method catches all possible exceptions thrown by the attempt to load
     * the most recent project and start the editor.  Although they are logged to
     * the console (System.err), they are not propagated to the caller.  This design
     * decision was made because the typical (only?) use case for this method is
     * simply to make a best effort to autoload, and fall back on prompting the
     * user if the autoload fails.
     * 
     * @return true if loading the project and launching the editor was successful;
     * false if the project couldn't be loaded for any reason.
     */
    private static boolean autoloadMostRecentProject() {
        if (recentProjects.get("0", null) == null) return false;
        if (recentProjects.get("autoLoadOk", "false").equals("false")) return false;
        File mostRecentProjectLocation = new File(recentProjects.get("0", null));
        if (mostRecentProjectLocation.isDirectory()) {
            System.err.println("autoloadMostRecentProject():");
            System.err.println("  Most recent project location '"+
                        mostRecentProjectLocation.getPath()+"' is a directory." +
                        " This probably means it's an old-style project which needs" +
                        " to be jarred up.");
            JOptionPane.showMessageDialog(null,
                    "Your most recent project location is a directory." +
                    "\nThere is now no difference between the level pack" +
                    "\nfile and the project file.  Export a level pack of" +
                    "\nyour project using an old version of the editor," +
                    "\nthen load that into this version of the editor and" +
                    "\nyou'll be good to go!");
        } else {
            Project project = null;
            try {
                project = Project.load(mostRecentProjectLocation);
                new EditorMain(project);
                return true;
            } catch (Exception ex) {
                System.err.println("autoloadMostRecentProject():");
                System.err.println("  Exception while opening most recent project from '"+
                        mostRecentProjectLocation.getPath()+"'. Giving up.");
                ex.printStackTrace();
                
                // clean up even if creating editor failed
                if (project != null) {
                    project.close();
                }
            }
        }
        return false;
    }

    /**
     * Presents a dialog which gives the user a choice: create a new project,
     * open an existing one, or quit.  This would be a good way to launch
     * the application from another Java program, except for one caveat.
     * <p>
     * Caveat: The quit option currently calls System.exit().  You might want
     * to change that before calling this method from within your program.
     */
    protected static void presentWelcomeMenu() {
        Project proj = null;

        while (proj == null) try {
            int choice = JOptionPane.showOptionDialog(
                    null, 
                    "Welcome to the Robot Editor.\n" +
                    "Do you want to open an existing project\n" +
                    "or start a new one?", "Robot Editor",
                    JOptionPane.YES_NO_CANCEL_OPTION, 
                    JOptionPane.PLAIN_MESSAGE, null, 
                    new String[] {"Quit", "Open Existing", "Create new"},
            "Create new");

            if (choice == 0) {
                System.exit(0);
            } else if (choice == 1) {
                // open existing
                proj = promptUserForProject();
            } else if (choice == 2) {
                // create new
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Where do you want to save your project?");
                int fcChoice = fc.showSaveDialog(null);
                if (fcChoice == JFileChooser.APPROVE_OPTION) {
                    File projFile = fc.getSelectedFile();
                    proj = Project.createNewProject(projFile);
                }
            }
        } catch (Exception ex) {
            showException(null, "Couldn't load project!", ex);
        }
        
        new EditorMain(proj);
    }

}
