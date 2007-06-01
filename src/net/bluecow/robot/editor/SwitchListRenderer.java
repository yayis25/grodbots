/*
 * Created on Oct 13, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import net.bluecow.robot.LevelConfig;
import net.bluecow.robot.LevelConfig.Switch;
import net.bluecow.robot.sprite.SpriteIcon;

public class SwitchListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        LevelConfig.Switch sw = (Switch) value;
        setText(sw.getId() + " (" + sw.getLabel() + ")");
        setIcon(new SpriteIcon(sw.getSprite()));
        return this;
    }
}
