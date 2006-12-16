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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.bluecow.robot.Direction;
import net.bluecow.robot.FileFormatException;
import net.bluecow.robot.GameConfig;
import net.bluecow.robot.LevelConfig;
import net.bluecow.robot.Playfield;
import net.bluecow.robot.Robot;
import net.bluecow.robot.RobotUtils;
import net.bluecow.robot.GameConfig.GateConfig;
import net.bluecow.robot.GameConfig.SensorConfig;
import net.bluecow.robot.GameConfig.SquareConfig;
import net.bluecow.robot.LevelConfig.Switch;
import net.bluecow.robot.gate.Gate;
import net.bluecow.robot.sprite.Sprite;
import net.bluecow.robot.sprite.SpriteFileFilter;
import net.bluecow.robot.sprite.SpriteManager;

public class EditorMain {

    public static final Dimension DEFAULT_LEVEL_SIZE = new Dimension(15,10);
    
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
            Project proj = promptUserForProject();
            if (proj != null) {
                closeProject();
                new EditorMain(proj);
            }
        }
    }
    
    private class SaveLevelPackAction extends AbstractAction {
        
        public SaveLevelPackAction() {
            super("Export Level Pack...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
        }
        
        public void actionPerformed(ActionEvent e) {
            Preferences recentFiles = RobotUtils.getPrefs().node("recentGameFiles");
            Writer out = null;
            try {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save Level Pack");
                fc.setCurrentDirectory(new File(recentFiles.get("0", System.getProperty("user.home"))));
                int choice = fc.showSaveDialog(frame);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    project.saveLevelPack(fc.getSelectedFile());
                    RobotUtils.updateRecentFiles(recentFiles, fc.getSelectedFile());
                }
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

    /**
     * Just saves the project resources in place.
     */
    private class SaveAction extends AbstractAction {
        
        public SaveAction() {
            super("Save");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
                project.save();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Save Failed: "+ex.getMessage());
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

    private Action addLevelAction = new AbstractAction("Add Level") {
        public void actionPerformed(ActionEvent e) {
            final LevelConfig currentLevel = (LevelConfig) levelChooser.getSelectedValue();
            final LevelConfig newLevel = new LevelConfig();
            newLevel.setName("New Level " + (1 + levelChooser.getModel().getSize()));
            if (currentLevel != null) {
                newLevel.setSize(currentLevel.getWidth(), currentLevel.getHeight());
            } else {
                newLevel.setSize(DEFAULT_LEVEL_SIZE.width, DEFAULT_LEVEL_SIZE.height);
            }
            project.getGameConfig().addLevel(newLevel);
            levelChooser.setSelectedValue(newLevel, true);
        }
    };

    private Action removeLevelAction = new AbstractAction("Remove Level") {
        public void actionPerformed(ActionEvent e) {
            final LevelConfig currentLevel = (LevelConfig) levelChooser.getSelectedValue();
            final int oldIndex = levelChooser.getSelectedIndex();
            project.getGameConfig().removeLevel(currentLevel);
            final int newIndex = Math.min(oldIndex, levelChooser.getModel().getSize()-1);
            levelChooser.clearSelection();
            levelChooser.setSelectedIndex(newIndex);
        }
    };

    private Action copyLevelAction = new AbstractAction("Copy Level") {
        public void actionPerformed(ActionEvent e) {
            final LevelConfig currentLevel = (LevelConfig) levelChooser.getSelectedValue();
            if (currentLevel == null) {
                JOptionPane.showMessageDialog(frame, "You have to select a level to copy it");
                return;
            }
            final LevelConfig newLevel = new LevelConfig(currentLevel);
            newLevel.setName(currentLevel.getName()+" Copy");
            project.getGameConfig().addLevel(newLevel);
            levelChooser.clearSelection();
            levelChooser.setSelectedIndex(levelChooser.getModel().getSize()-1);
        }
    };

    private Action addRobotAction = new AbstractAction("Add Robot") {
        public void actionPerformed(ActionEvent e) {
            final LevelConfig level = (LevelConfig) levelChooser.getSelectedValue();
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
            final LevelConfig level = (LevelConfig) levelChooser.getSelectedValue();
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
            final LevelConfig level = (LevelConfig) levelChooser.getSelectedValue();
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
            final LevelConfig level = (LevelConfig) levelChooser.getSelectedValue();
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
    private JPanel levelEditPanel;
    private LevelEditor editor;
    private JList levelChooser;
    private LevelChooserListModel levelChooserListModel;
    private JList sensorTypesList;
    private SensorTypeListModel sensorTypeListModel;
    private JList squareList;
    private SquareChooserListModel squareChooserListModel;
    private JList robotChooser;
    private JList switchChooser;
    
    private LoadProjectAction loadProjectAction = new LoadProjectAction();
    private SaveAction saveProjectAction = new SaveAction();
    private SaveLevelPackAction saveLevelPackAction = new SaveLevelPackAction();
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
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Choose a Robot Project Directory");
        File recentProject = new File(recentProjects.get("0", null));
        if (recentProject == null) {
            recentProject = new File(System.getProperty("user.home"));
        } else if (recentProject.isDirectory()) {
            // for project directories, we want to default the dialog to the parent dir
            recentProject = recentProject.getParentFile();
        }
        fc.setCurrentDirectory(recentProject);
        int choice = fc.showOpenDialog(null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                Project proj = Project.load(f);
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
        
        final JComboBox spritePathField = new JComboBox(new ResourcesComboBoxModel(project, new SpriteFileFilter()));
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
     * presses and robot's properties have been updated.  This action will
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
        
        final JDialog d = new JDialog(parent, "Switch Properties");

        final JTextField idField = new JTextField();
        final JTextField labelField = new JTextField();
        final JCheckBox labelEnabledBox = new JCheckBox("Label Enabled");
        final JComboBox labelDirectionBox = new JComboBox(Direction.values());
        final JSpinner xPosition = new JSpinner(new SpinnerNumberModel(0.0, 0.0, level.getWidth(), robot.getStepSize()));
        final JSpinner yPosition = new JSpinner(new SpinnerNumberModel(0.0, 0.0, level.getHeight(), robot.getStepSize()));
        final JComboBox spritePathField = new JComboBox(new ResourcesComboBoxModel(project, new SpriteFileFilter()));
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

        final JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    robot.setId(idField.getText());
                    robot.setLabel(labelField.getText());
                    robot.setLabelDirection((Direction) labelDirectionBox.getSelectedItem());
                    robot.setLabelEnabled(labelEnabledBox.isSelected());
                    Point2D pos = new Point2D.Double(
                            (Double) xPosition.getValue(),
                            (Double) yPosition.getValue());
                    robot.setPosition(pos);
                    
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
                        System.out.println("Performing OK action for robot props dialog");
                        okAction.actionPerformed(e);
                    }

                } catch (Exception ex) {
                    showException(parent, "Couldn't update Robot properties", ex);
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
        final JComboBox spritePathField = new JComboBox(new ResourcesComboBoxModel(project, new SpriteFileFilter()));
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
        
        frame = new JFrame("Robot Level Editor");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit();
            }
        });
        
        final GameConfig myGameConfig = project.getGameConfig();
        
        frame.getContentPane().setLayout(new BorderLayout(8, 8));
        ((JComponent) frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        
        JPanel levelChooserPanel = new JPanel(new BorderLayout());
        levelChooserListModel = new LevelChooserListModel(myGameConfig);
        levelChooser = new JList(levelChooserListModel);
        levelChooser.setCellRenderer(new LevelChooserListRenderer());
        levelChooser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        levelChooser.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                LevelConfig level = (LevelConfig) levelChooser.getSelectedValue();
                setLevelToEdit(level);
            }
        });
        levelChooserPanel.add(new JLabel("Levels"), BorderLayout.NORTH);
        levelChooserPanel.add(new JScrollPane(levelChooser), BorderLayout.CENTER);
        JPanel buttonPanel = makeButtonPanel(addLevelAction, removeLevelAction, copyLevelAction);
        levelChooserPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        frame.add(levelChooserPanel, BorderLayout.WEST);
        
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
                System.out.println("Click");
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    GameConfig.SensorConfig sc = (SensorConfig) sensorTypesList.getSelectedValue();
                    System.out.println("Double Click (selectedValue="+sc+")");
                    if (sc != null) {
                        makeSensorPropsDialog(frame, getProject().getGameConfig(), sc).setVisible(true);
                    }
                }
            }
        });
        sensorTypesPanel.add(
                makeButtonPanel(addSensorTypeAction, removeSensorTypeAction),
                BorderLayout.SOUTH);
        
        squareChooserListModel = new SquareChooserListModel(myGameConfig);
        squareList = new JList(squareChooserListModel);
        squareList.setCellRenderer(new SquareChooserListRenderer());
        squareList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                System.out.println("Square List selection changed. squares="+myGameConfig.getSquareTypes());
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
                System.out.println("Click");
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    GameConfig.SquareConfig sc = (SquareConfig) squareList.getSelectedValue();
                    System.out.println("Double Click (selectedValue="+sc+")");
                    if (sc != null) {
                        makeSquarePropsDialog(frame, getProject(), sc).setVisible(true);
                    }
                }
            }
        });
        squareListPanel.add(
                makeButtonPanel(addSquareTypeAction, removeSquareTypeAction),
                BorderLayout.SOUTH);
        JSplitPane eastPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        eastPanel.setTopComponent(sensorTypesPanel);
        eastPanel.setBottomComponent(squareListPanel);
        
        frame.add(eastPanel, BorderLayout.EAST);
        
        setupMenu();
        
        levelChooser.setSelectedIndex(0);
        
        frame.pack();
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
     * Sets up all the menu bar crap and adds it to the frame.
     */
    private void setupMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu m;
        mb.add (m = new JMenu("File"));
        m.add(new JMenuItem(loadProjectAction));
        m.add(new JMenuItem(saveProjectAction));
        m.add(new JMenuItem(saveLevelPackAction));
        m.add(new JMenuItem(closeProjectAction));
        m.add(new JMenuItem(exitAction));
        
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
            System.exit(0);
        }
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
            levelEditPanel = new JPanel(new BorderLayout(8, 8));
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

            levelEditPanel.add(makeLevelPropsPanel(level), BorderLayout.CENTER);
            levelEditPanel.add(editorPanel, BorderLayout.SOUTH);
            
            editor.setPaintingSquareType((SquareConfig) squareList.getSelectedValue());
        }        
        frame.add(levelEditPanel, BorderLayout.CENTER);
        frame.validate();
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
        p.add(new JLabel("Description (HTML):"), gbc);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.5;
        p.add(new JScrollPane(descriptionArea), gbc);
        
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        p.add(new JLabel("Robots"), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        p.add(new JLabel("Switches"), gbc);
        
        robotChooser = new JList(new RobotListModel(level));
        switchChooser = new JList(new SwitchListModel(level));

        gbc.gridwidth = 2;
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
                System.out.println("robot chooser selection changed: "+e);
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
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JPanel addRemovePanel = makeButtonPanel(addRobotAction, removeRobotAction);
        p.add(addRemovePanel, gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        JPanel buttonPanel = makeButtonPanel(addSwitchAction, removeSwitchAction);
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
            try {
                Project project = Project.load(mostRecentProjectLocation);
                new EditorMain(project);
                return true;
            } catch (Exception ex) {
                System.err.println("autoloadMostRecentProject():");
                System.err.println("  Exception while opening most recent project from '"+
                        mostRecentProjectLocation.getPath()+"'. Giving up.");
                ex.printStackTrace();
            }
        } else {
            System.err.println("autoloadMostRecentProject():");
            System.err.println("  Most recent project location '"+
                        mostRecentProjectLocation.getPath()+"' isn't a directory. Giving up.");
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
            System.out.println("Choice: "+choice);

            if (choice == 0) {
                System.exit(0);
            } else if (choice == 1) {
                // open existing
                proj = promptUserForProject();
            } else if (choice == 2) {
                // create new
                String projName = JOptionPane.showInputDialog(
                        "What will your project be called?");
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setDialogTitle("Where do you want to save your project?");
                int fcChoice = fc.showSaveDialog(null);
                File projDir = new File(fc.getSelectedFile(), projName);
                if (fcChoice == JFileChooser.APPROVE_OPTION) {
                    proj = Project.createNewProject(projDir);
                }
                JOptionPane.showMessageDialog(null, 
                        "Ok, your project has been created!\n" +
                        "If you want to add to, remove from, or modify its resources\n" +
                        "(images and sounds), you can find them in the project directory:\n\n" +
                        new File(projDir, "ROBO-INF").getAbsolutePath());
            }
        } catch (Exception ex) {
            showException(null, "Couldn't load project!", ex);
        }
        
        new EditorMain(proj);
    }

}
