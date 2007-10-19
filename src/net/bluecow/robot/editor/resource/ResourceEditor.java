/*
 * Copyright (c) 2007, Jonathan Fuerth
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Jonathan Fuerth nor the names of other
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.bluecow.robot.resource.CompoundResourceManager;
import net.bluecow.robot.resource.JarResourceManager;
import net.bluecow.robot.resource.ListableResourceLoader;
import net.bluecow.robot.resource.PreListedResourceLoader;
import net.bluecow.robot.resource.PrefixResourceLoader;
import net.bluecow.robot.resource.ResourceLoader;
import net.bluecow.robot.resource.ResourceManager;
import net.bluecow.robot.resource.SystemResourceLoader;

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
    
    /**
     * The action that can add a new resource via a dialog.
     */
    private final CreateResourceAction createResourceAction;

    /**
     * The action that can add a new resource via a dialog.
     */
    private final CreateDirectoryAction createDirectoryAction;
    
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
        createDirectoryAction = new CreateDirectoryAction(resourceManager, resourceTree);
        
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
        JMenuItem mi;
        
        mi = new JMenuItem(createResourceAction);
        mi.setActionCommand(getSelectedDirectory());
        m.add(mi);
        
        mi = new JMenuItem(createDirectoryAction);
        mi.setActionCommand(getSelectedDirectory());
        m.add(mi);

        m.show(resourceTree, p.x, p.y);
    }
    
    /**
     * Returns the currently-selected resource directory. If the current selection
     * is a resource (not a directory), then the resource's parent directory is
     * returned. Finally, if there is no tree selection, this method returns null.
     * 
     * @return The current directory selection, or null if nothing is selected.
     */
    public String getSelectedDirectory() {
        final TreePath selectionPath = resourceTree.getSelectionPath();
        if (selectionPath == null) return null;
        String path = (String) selectionPath.getLastPathComponent();
        if (!path.endsWith("/")) {
            path = path.substring(0, path.lastIndexOf('/'));
        }
        return path;
    }

    /**
     * Returns the resource manager this editor was created for.
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame f = new JFrame("Test resource editor");
                    
                    // this only exists if you have run the default_resoruces_jar build target
                    // (and if using eclipse, make sure to refresh the project) 
                    ResourceManager defaultResources = new JarResourceManager(
                            new File("build/net/bluecow/robot/default_resources.jar"));
                    ResourceLoader prefixLoader = new PrefixResourceLoader(new SystemResourceLoader(), "builtin_resources/");
                    ListableResourceLoader builtinResources = new PreListedResourceLoader(prefixLoader, "resources.list");
                    System.out.println("Root: " + builtinResources.list(""));
                    System.out.println("doc: " + builtinResources.list("ROBO-INF/doc/"));
                    ResourceManager rm = new CompoundResourceManager(defaultResources, builtinResources);
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
