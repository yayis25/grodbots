/*
 * Created on Apr 13, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor.resource;

import java.awt.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class ResourcePathTreeCellRenderer extends DefaultTreeCellRenderer {

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        String path = (String) value;
        
        Pattern p = Pattern.compile(".*/([^/]+)/?");
        Matcher m = p.matcher(path);
        if (m.matches()) {
            path = m.group(1);
        }        
        super.getTreeCellRendererComponent(tree, path, selected, expanded, leaf, row, hasFocus);
        return this;
    }

}
