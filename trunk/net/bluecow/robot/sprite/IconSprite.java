/*
 * Created on Apr 20, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import java.awt.Image;
import java.io.IOException;
import java.util.Map;

import javax.swing.ImageIcon;

import net.bluecow.robot.resource.ResourceLoader;

public class IconSprite extends AbstractSprite {
    private ImageIcon icon;
    
    public IconSprite(
            ResourceLoader resourceLoader, String imagePath,
            Map<String, String> attribs) throws IOException {
        super(resourceLoader, attribs);
        icon = new ImageIcon(resourceLoader.getResourceBytes(imagePath));
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
