/*
 * Created on Apr 20, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor.resource;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.bluecow.robot.RobotUtils;
import net.bluecow.robot.resource.ResourceManager;
import net.bluecow.robot.resource.ResourceUtils;

public class CreateResourceAction extends AbstractAction {

    /**
     * The component whos containing window will own any dialogs created by this action.
     */
    private final JComponent owningComponent;
    
    /**
     * The resource manager that this action adds resources to.
     */
    private final ResourceManager resourceManager;
    
    public CreateResourceAction(ResourceManager resourceManager, JComponent owningComponent) {
        super("Create Resource...");
        this.resourceManager = resourceManager;
        this.owningComponent = owningComponent;
    }
    
    public void actionPerformed(ActionEvent e) {
        
        // need to get target path for resource
        JComboBox cb = new JComboBox(new ResourcesComboBoxModel(resourceManager, ResourceUtils.directoryOnlyFilter()));
        int choice = JOptionPane.showOptionDialog(
                owningComponent, cb, "Choose a target directory in the project",
                JOptionPane.OK_CANCEL_OPTION, -1, null, null, 0);
        if (choice != JOptionPane.OK_OPTION) return;
        String targetdir = (String) cb.getSelectedItem();
        
        Preferences prefs = RobotUtils.getPrefs().node("recentResourceFiles");
        String mostRecentResource = prefs.get("0", null);
        
        JFileChooser fc = new JFileChooser();
        if (mostRecentResource != null) {
            File f = new File(mostRecentResource);
            if (f.exists()) {
                fc.setCurrentDirectory(f);
            }
        }
        
        choice = fc.showOpenDialog(owningComponent);
        if (choice != JFileChooser.APPROVE_OPTION) return;
        File inFile = fc.getSelectedFile();
        InputStream in = null;
        OutputStream out = null;
        
        try {
            in = new BufferedInputStream(new FileInputStream(inFile));
            out = resourceManager.openForWrite(targetdir + inFile.getName(), true);

            byte[] buf = new byte[8192];
            int count;
            while ( (count = in.read(buf)) > 0 ) {
                out.write(buf, 0, count);
            }
        } catch (IOException ex) {
            RobotUtils.showException("Couldn't create resource", ex);
        } finally {
            try { in.close(); } catch (IOException ex) { ex.printStackTrace(); }
            try { out.flush(); } catch (IOException ex) { ex.printStackTrace(); }
            try { out.close(); } catch (IOException ex) { ex.printStackTrace(); }
        }
    }

}
