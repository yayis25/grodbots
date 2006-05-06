/*
 * Created on Apr 20, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.net.URL;

import javax.swing.ImageIcon;

public class IconSprite implements Sprite {
    private ImageIcon icon;
    private double scale = 1.0;
    
    public IconSprite(URL imageURL) {
        icon = new ImageIcon(imageURL);
    }
    
    public ImageIcon getIcon() {
        return icon;
    }

    public void paint(Graphics2D g2, int x, int y) {
        AffineTransform backupXform = g2.getTransform();
        try {
            g2.translate(x, y);
            g2.drawImage(icon.getImage(), AffineTransform.getScaleInstance(scale, scale), null);
        } finally {
            g2.setTransform(backupXform);
        }
    }

    public int getWidth() {
        return (int) (icon.getIconWidth() * scale);
    }

    public int getHeight() {
        return (int) (icon.getIconHeight() * scale);
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }
}
