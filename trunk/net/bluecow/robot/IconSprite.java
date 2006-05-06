/*
 * Created on Apr 20, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.awt.Graphics2D;
import java.net.URL;

import javax.swing.ImageIcon;

public class IconSprite implements Sprite {
    private ImageIcon icon;
    
    public IconSprite(URL imageURL) {
        icon = new ImageIcon(imageURL);
    }
    
    public ImageIcon getIcon() {
        return icon;
    }

    public void paint(Graphics2D g2, int x, int y) {
        icon.paintIcon(null, g2, x, y);
    }

    public int getWidth() {
        return icon.getIconWidth();
    }

    public int getHeight() {
        return icon.getIconHeight();
    }
}
