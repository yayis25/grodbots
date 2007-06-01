/*
 * Created on Oct 10, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.robot.editor;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import net.bluecow.robot.LevelConfig;

public class LevelChooserListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        LevelConfig level = (LevelConfig) value;
        String label;
        if (level != null) {
            label = level.getName();
        } else {
            label = "(This game no levels!)";
        }
        super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
        return this;
    }
}
