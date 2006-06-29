/*
 * Created on Jun 27, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

public abstract class AbstractSprite implements Sprite {
    private double scale = 1.0;

    /**
     * Paints this sprite at the given co-ordinates at the current scale
     * setting.  The image to paint is obtained by a call to getImage(),
     * and the scale comes from a call to getScale().  Each of getImage()
     * and getScale() are called exactly once.  The Graphics2D argument's
     * transform is modified in the course of this method's invocation, but
     * it is guaranteed to be restored to its initial setting before this
     * method returns.
     */
    public void paint(Graphics2D g2, int x, int y) {
        AffineTransform backupXform = g2.getTransform();
        try {
            double scale = getScale();
            g2.translate(x, y);
            g2.drawImage(getImage(), AffineTransform.getScaleInstance(scale, scale), null);
        } finally {
            g2.setTransform(backupXform);
        }
    }

    /**
     * Returns the image that this sprite will draw when paint() is called.
     * Subclasses have to implement this method in order to take advantage of
     * the AbstractSprite's generic implementation of paint().
     */
    public abstract Image getImage();
    
    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * This implementation does nothing.  If your subclass supports multiple frames
     * of animation, override this method.
     */
    public void nextFrame() {
        // nothing to do
    }
}
