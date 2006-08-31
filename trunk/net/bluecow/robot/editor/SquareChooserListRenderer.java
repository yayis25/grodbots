/*
 * Created on Aug 28, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import net.bluecow.robot.GameConfig.SquareConfig;

public class SquareChooserListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
        SquareConfig sc = (SquareConfig) value;
        Image spriteImage = new BufferedImage(sc.getSprite().getWidth(), sc.getSprite().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) spriteImage.getGraphics();
        sc.getSprite().paint(g2, 0, 0);
        g2.dispose();
        setIcon(new ImageIcon(spriteImage));
        setText(sc.getName());
        return this;
    }
}
