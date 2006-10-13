/*
 * Created on Aug 25, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.bluecow.robot.FileFormatException;
import net.bluecow.robot.GameConfig;
import net.bluecow.robot.LevelConfig;
import net.bluecow.robot.RobotUtils;
import net.bluecow.robot.GameConfig.SensorConfig;
import net.bluecow.robot.GameConfig.SquareConfig;
import net.bluecow.robot.sprite.Sprite;
import net.bluecow.robot.sprite.SpriteManager;

public class EditorMain {

    private class LoadProjectAction extends AbstractAction {
        
        
        public LoadProjectAction() {
            super("Open Project...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }
        
        public void actionPerformed(ActionEvent e) {
            Project proj = promptUserForProject();
            if (proj != null) {
                setProject(proj);
            }
        }
    }
    
    private class SaveLevelPackAction extends AbstractAction {
        
        public SaveLevelPackAction() {
            super("Save Level Pack...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
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

    private Action addSquareTypeAction = new AbstractAction("Add Square Type") {
        public void actionPerformed(ActionEvent e) {
            SquareConfig squareConfig = new GameConfig.SquareConfig();
            JDialog d = makeSquarePropsDialog(frame, project, squareConfig);
            d.setModal(true);
            d.setVisible(true);
            project.getGameConfig().addSquareType(squareConfig);
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

    private Action addLevelAction = new AbstractAction("Add Level") {
        public void actionPerformed(ActionEvent e) {
            LevelConfig level = new LevelConfig();
            level.setName("New Level");
            project.getGameConfig().addLevel(level);
            levelChooser.setSelectedValue(level, true);
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
    private SensorTypeListModel sensorTypeListModel;
    private SquareChooserListModel squareChooserListModel;
    
    private LoadProjectAction loadProjectAction = new LoadProjectAction();
    private SaveLevelPackAction saveLevelPackAction = new SaveLevelPackAction();

    
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
        cp.add(new JLabel("Square Type Name:"), gbc);

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cp.add(nameField, gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        cp.add(okButton, gbc);

        d.setContentPane(cp);
        d.pack();
        return d;
    }
    
    public static Project promptUserForProject() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Choose a Robot Project Directory");
        fc.setCurrentDirectory(new File(recentProjects.get("0", System.getProperty("user.home"))));
        int choice = fc.showOpenDialog(null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                Project proj = Project.load(f);
                RobotUtils.updateRecentFiles(recentProjects, fc.getSelectedFile());
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
        
        final JComboBox spritePathField = new JComboBox(new ResourcesComboBoxModel(project));
        spritePathField.setSelectedItem(sc.getSprite().getAttributes().get(Sprite.KEY_HREF));
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

        d.setContentPane(cp);
        d.pack();
        return d;
    }
    
    public EditorMain(Project project) {
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
        JButton addLevelButton = new JButton(addLevelAction);
        levelChooserPanel.add(addLevelButton, BorderLayout.SOUTH);
        
        frame.add(levelChooserPanel, BorderLayout.WEST);
        
        levelEditPanel = new JPanel();
        levelEditPanel.add(new JLabel("To edit a level, select it from the list on the left-hand side."));
        frame.add(levelEditPanel, BorderLayout.CENTER);
        
        JPanel sensorTypesPanel = new JPanel(new BorderLayout());
        sensorTypeListModel = new SensorTypeListModel(myGameConfig);
        final JList sensorTypesList = new JList(sensorTypeListModel);
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
        sensorTypesPanel.add(new JButton(addSensorTypeAction), BorderLayout.SOUTH);
        
        squareChooserListModel = new SquareChooserListModel(myGameConfig);
        final JList squareList = new JList(squareChooserListModel);
        squareList.setCellRenderer(new SquareChooserListRenderer());
        squareList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                System.out.println("Square List selection changed. squares="+myGameConfig.getSquareTypes());
                editor.setPaintingSquareType((SquareConfig) squareList.getSelectedValue());
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
        squareListPanel.add(new JButton(addSquareTypeAction), BorderLayout.SOUTH);
        
        JSplitPane eastPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        eastPanel.setTopComponent(sensorTypesPanel);
        eastPanel.setBottomComponent(squareListPanel);
        
        frame.add(eastPanel, BorderLayout.EAST);
        
        setupMenu();
        
        setProject(project);
        
        frame.pack();
        frame.setVisible(true);
    }
    
    /**
     * Sets up all the menu bar crap and adds it to the frame.
     */
    private void setupMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu m;
        mb.add (m = new JMenu("File"));
        m.add(new JMenuItem(loadProjectAction));
        m.add(new JMenuItem(saveLevelPackAction));
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
     * Presents an "are you sure?" dialog and exits the application if the user
     * responds affirmitavely.
     *
     */
    public void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(frame, "Do you really want to quit?", "Quit the level editor", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    
    private void setProject(Project proj) {
        this.project = proj;
        sensorTypeListModel.setGame(proj.getGameConfig());
        squareChooserListModel.setGame(proj.getGameConfig());
        levelChooserListModel.setGame(proj.getGameConfig());
        levelChooser.setSelectedIndex(0);
    }
    
    private Project getProject() {
        return project;
    }

    private void setLevelToEdit(LevelConfig level) {
        if (levelEditPanel != null) {
            frame.remove(levelEditPanel);
        }
        levelEditPanel = new JPanel(new BorderLayout(8, 8));
        editor = new LevelEditor(project.getGameConfig(), level);
        levelEditPanel.add(editor, BorderLayout.CENTER);
        
        levelEditPanel.add(makeLevelPropsPanel(level), BorderLayout.NORTH);
        
        frame.add(levelEditPanel, BorderLayout.CENTER);
        frame.validate();
    }
    
    private JPanel makeLevelPropsPanel(LevelConfig level) {
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel p = new JPanel(new GridBagLayout());
        
        gbc.weighty = 0.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        p.add(new JLabel("Level Name:"), gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        p.add(new JTextField(level.getName()), gbc);
        
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        p.add(new JLabel("Robots"), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        p.add(new JLabel("Switches"), gbc);
        
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        final JList robotChooser = new JList(new RobotListModel(level));
        p.add(new JScrollPane(robotChooser), gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        final JList switchChooser = new JList(new SwitchListModel(level));
        p.add(new JScrollPane(switchChooser), gbc);

        return p;
    }

    /**
     * Shows the given message and the exception's message and stack trace
     * in a modal dialog.
     */
    public static void showException(JFrame owner, String message, Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        pw.flush();
        pw.close();
        
        JTextArea ta = new JTextArea(message+"\n\n"+sw.getBuffer(), 15, 60);
        JOptionPane.showMessageDialog(owner, new JScrollPane(ta), "Error Report", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * @param args
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

    private static boolean autoloadMostRecentProject() {
        if (recentProjects.get("0", null) == null) {
            return false;
        }
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
            System.out.println("autoloadMostRecentProject():");
            System.out.println("  Most recent project location '"+
                        mostRecentProjectLocation.getPath()+"' isn't a directory. Giving up.");
        }
        return false;
    }

    protected static void presentWelcomeMenu() throws IOException {
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
        
        Project proj = null;
        if (choice == 1) {
            // open existing
            proj = promptUserForProject();
        } else if (choice == 2) {
            // create new
            String projName = JOptionPane.showInputDialog("What will your project be called?");
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setDialogTitle("Where do you want to save your project?");
            int fcChoice = fc.showSaveDialog(null);
            if (fcChoice == JFileChooser.APPROVE_OPTION) {
                proj = Project.createNewProject(new File(fc.getSelectedFile(), projName));
            }
        }
        
        if (proj != null) {
            new EditorMain(proj);
        }
    }

}
