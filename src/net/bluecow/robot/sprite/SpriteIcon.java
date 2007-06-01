/*
 * Created on Oct 13, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

/**
 * The SpriteIcon is an adapter that makes it possible to use a Sprite
 * where an Icon is required.
 * 
 * <p>Note: the Sprite and Icon interfaces are very similar.  It might be a
 * better idea to just have Sprite extend Icon.
 *
 * @author fuerth
 * @version $Id$
 */
public class SpriteIcon implements Icon {

    private Sprite sprite;
    
    public SpriteIcon(Sprite sprite) {
        this.sprite = sprite;
    }
    
    public int getIconHeight() {
        return sprite.getHeight();
    }

    public int getIconWidth() {
        return sprite.getWidth();
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        sprite.paint((Graphics2D) g, x, y);
    }

}
