/*
 * Created on Apr 20, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import java.awt.Image;
import java.net.URL;
import java.util.Map;

import javax.swing.ImageIcon;

public class IconSprite extends AbstractSprite {
    private ImageIcon icon;
    
    public IconSprite(URL imageURL, Map<String, String> attribs) {
        super(attribs);
        icon = new ImageIcon(imageURL);
    }
    
    @Override
    public Image getImage() {
        return icon.getImage();
    }

    public int getWidth() {
        return (int) (icon.getIconWidth() * getScale());
    }
    
    public int getHeight() {
        return (int) (icon.getIconHeight() * getScale());
    }
}
