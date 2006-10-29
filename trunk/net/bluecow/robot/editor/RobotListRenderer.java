/*
 * Created on Oct 13, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import net.bluecow.robot.Robot;
import net.bluecow.robot.sprite.SpriteIcon;

public class RobotListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        Robot robot = (Robot) value;
        setIcon(new SpriteIcon(robot.getSprite()));
        setText(robot.getId() + " (" + robot.getLabel() + ")");
        return this;
    }
}
