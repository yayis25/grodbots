/*
 * Created on Apr 11, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.bluecow.robot.resource.ResourceManager;

/**
 * The ResourceManagerTreeModel makes a ResourceManager available
 * as a Swing TreeModel.
 * 
 * <p>FIXME: still need to implement change events on the resource
 * manager, and relay them through this class as TreeModelEvents.
 *
 * @author fuerth
 * @version $Id$
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
    
    private List<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();
    private ResourceManager resourceManager;
    
    public ResourceManagerTreeModel(ResourceManager resourceManager) throws IOException {
        this.resourceManager = resourceManager;
    }
    
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.remove(l);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    public Object getChild(Object parent, int index) {
        debug("RMTM: getIndexOfChild(" + parent + ", " + index + ")");
        String path = (String) parent;
        return resourceManager.list(path).get(index);
    }

    public int getChildCount(Object parent) {
        debug("RMTM: getChildCount(" + parent + ")");
        String path = (String) parent;
        return resourceManager.list(path).size();
    }

    public int getIndexOfChild(Object parent, Object child) {
        debug("RMTM: getIndexOfChild(" + parent + ", " + child + ")");
        String path = (String) parent;
        List<String> children = resourceManager.list(path);
        return children.indexOf(child);
    }

    public Object getRoot() {
        debug("RMTM: getRoot()");
        return "";
    }

    public boolean isLeaf(Object node) {
        debug("RMTM: isLeaf(" + node + ")");
        String path = (String) node;
        return !(path.equals("") || path.endsWith("/"));
    }

}
