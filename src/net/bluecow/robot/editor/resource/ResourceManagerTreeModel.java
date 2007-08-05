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
 * Created on Apr 11, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.bluecow.robot.resource.RegexResourceNameFilter;
import net.bluecow.robot.resource.ResourceManager;
import net.bluecow.robot.resource.ResourceNameFilter;
import net.bluecow.robot.resource.event.ResourceManagerEvent;
import net.bluecow.robot.resource.event.ResourceManagerListener;

/**
 * The ResourceManagerTreeModel makes a ResourceManager available
 * as a Swing TreeModel.  The individual Node objects of the tree
 * model are Strings, each one holding the full resource path name
 * from the root to the resource file or directory itself.
 * <p>
 * File resource names never end with a slash (/) character;
 * directory resource names always end with a slash.  The Root
 * element returned by {@link #getRoot()} is always the constant
 * String "/".
 * 
 * @author fuerth
 * @version $Id:$
 */
public class ResourceManagerTreeModel implements TreeModel {

    /**
     * Controls the debugging features of this class.
     */
    private static final boolean debugOn = true;
    
    /**
     * Prints the given message to System.out if debugOn is true.
     */
    private static void debug(String msg) {
        if (debugOn) System.out.println(msg);
    }

    /**
     * Prints the given printf-formatted message, followed by a newline,
     * to the console if debugOn == true.
     */
    private static void debugf(String fmt, Object ... args) {
        if (debugOn) debug(String.format(fmt, args));
    }

    private List<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();
    
    /**
     * The resource manager whose resources this tree model reflects.
     */
    private ResourceManager resourceManager;
    
    /**
     * A filter for ignoring resources we don't care about, such as the
     * /META-INF/ directory.
     */
    private ResourceNameFilter filter;
    
    /**
     * Listens for changes in the resource manager and refires them
     * as tree model events.
     */
    private final ResourceManagerListener eventAdapter = new ResourceManagerListener() {

        public void resourceAdded(ResourceManagerEvent event) {
            fireChildrenAdded(event.getParentPath(), event.getChildName());
        }

        public void resourceChanged(ResourceManagerEvent event) {
            // TODO Auto-generated method stub
            
        }

        public void resourceRemoved(ResourceManagerEvent event) {
            // TODO Auto-generated method stub
            
        }
        
    };
    
    public ResourceManagerTreeModel(ResourceManager resourceManager) throws IOException {
        this.resourceManager = resourceManager;
        filter = new RegexResourceNameFilter("/?META-INF/.*", true);
        resourceManager.addResourceManagerListener(eventAdapter);
    }
    
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    public Object getChild(Object parent, int index) {
        try {
            debug("RMTM: getIndexOfChild(" + parent + ", " + index + ")");
            String path = (String) parent;
            return resourceManager.list(path, filter).get(index);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int getChildCount(Object parent) {
        try {
            debug("RMTM: getChildCount(" + parent + ")");
            String path = (String) parent;
            return resourceManager.list(path, filter).size();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int getIndexOfChild(Object parent, Object child) {
        try {
            debug("RMTM: getIndexOfChild(" + parent + ", " + child + ")");
            String path = (String) parent;
            List<String> children = resourceManager.list(path, filter);
            return children.indexOf(child);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Object getRoot() {
        debug("RMTM: getRoot()");
        return "/";
    }

    public boolean isLeaf(Object node) {
        debug("RMTM: isLeaf(" + node + ")");
        String path = (String) node;
        return !(path.equals("") || path.endsWith("/"));
    }

    /**
     * Converts the given resource path string into the equivalent TreePath.
     * 
     * @param resourcePath The resource manager path to convert
     * @return A TreePath that is compatible with this tree model and describes
     * the given resource path.
     */
    public static TreePath resourcePathToTreePath(String resourcePath) {
        boolean pathIsDir = resourcePath.endsWith("/");
        List<String> treePath = new ArrayList<String>();
        String[] pathElements = resourcePath.split("/");
        String currentPath = "/";
        treePath.add(currentPath);
        for (int i = 0; i < pathElements.length; i++) {
            if ( (i == pathElements.length - 1) && !pathIsDir ) {
                currentPath += pathElements[i];
            } else {
                currentPath += pathElements[i] + "/";
            }
            treePath.add(currentPath);
        }
        debugf("resourcePathToTreePath: %s -> %s", resourcePath, treePath);
        return new TreePath(treePath.toArray());
    }
    
    // ------------- Events! --------------
    
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.remove(l);
    }

    /**
     * Fires a treeNodesInserted event for the given child.  Determines the
     * child index by searching for it in the resource manager.
     */
    private void fireChildrenAdded(String parentPath, String childName) {
        List<String> rmChildren;
        try {
            rmChildren = resourceManager.list(parentPath, filter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String fullPath = parentPath + childName;
        int pos = rmChildren.indexOf(fullPath);
        TreePath parentTreePath = resourcePathToTreePath(parentPath);
        TreeModelEvent tme = new TreeModelEvent(this, parentTreePath, new int[] { pos }, new String[] { fullPath });
        debug("Firing treeNodesInserted event to "+treeModelListeners.size()+" listeners: " + tme);
        for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
            treeModelListeners.get(i).treeNodesInserted(tme);
        }
    }
}
