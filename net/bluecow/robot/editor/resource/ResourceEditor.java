/*
 * Created on Mar 20, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor.resource;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
     * This object manages a preview panel which displays the currently
     * selected resource(s).
     */
    private ResourcePreview resourcePreview;
    
    private CreateResourceAction createResourceAction;
    
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
        resourceTree.expandRow(0); /* this will be the ROBO-INF directory */
        
        resourcePreview = new ResourcePreview(resourceManager);
        resourceTree.addTreeSelectionListener(resourcePreview);
        
        createResourceAction = new CreateResourceAction(resourceManager, resourceTree);
        
        resourceTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isPopupTrigger()) showPopupMenu(e.getPoint());
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showPopupMenu(e.getPoint());
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showPopupMenu(e.getPoint());
            }
        });
    }
    
    /**
     * Causes a popup menu with various resource tree actions to
     * display over the resource tree at the given point.
     */
    public void showPopupMenu(Point p) {
        JPopupMenu m = new JPopupMenu();
        m.add(new JMenuItem(createResourceAction));
        m.show(resourceTree, p.x, p.y);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame f = new JFrame("Test resource editor");
                    ResourceManager rm = new JarResourceManager(
                            new File("net/bluecow/robot/default_resources.jar"));
                    ResourceEditor re = new ResourceEditor(rm);
                    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                    f.add(sp);
                    sp.setTopComponent(new JScrollPane(re.resourceTree));
                    sp.setBottomComponent(re.resourcePreview);
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
