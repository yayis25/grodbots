/*
 * Created on Mar 20, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor.resource;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;

import net.bluecow.robot.resource.JarResourceManager;
import net.bluecow.robot.resource.ResourceManager;

/**
 * The ResourceEditor is a GUI for manipulating and browsing a
 * project's set of resources.
 *
 * @author fuerth
 * @version $Id$
 */
public class ResourceEditor {
    
    /**
     * The resource manager that this editor deals with.
     */
    private ResourceManager resourceManager;
    
    /**
     * This tree displays the resources that exist in the project.
     */
    private JTree resourceTree;
    
    /**
     * This panel contains the editor's complete GUI.
     */
    private JPanel panel;
    
    public ResourceEditor(ResourceManager resourceManager) throws IOException {
        this.resourceManager = resourceManager;
        TreeModel treeModel = new ResourceManagerTreeModel(resourceManager);
        resourceTree = new JTree(treeModel);
        resourceTree.setRootVisible(false);
        resourceTree.setCellRenderer(new ResourcePathTreeCellRenderer());
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame f = new JFrame("Test resource editor");
                    ResourceManager rm = new JarResourceManager(
                            new File("net/bluecow/robot/default_resources.jar"));
                    ResourceEditor re = new ResourceEditor(rm);
                    f.add(new JScrollPane(re.resourceTree));
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.pack();
                    f.setVisible(true);
                } catch (Exception ex) {
                    System.err.println("Couldn't create resource editor");
                    ex.printStackTrace();
                }
            }
        });
    }
}
