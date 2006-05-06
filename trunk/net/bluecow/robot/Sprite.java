/*
 * Created on Apr 18, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.robot;

import java.awt.Graphics2D;

/**
 * Sprite is an interface to any type of moving or static image resource
 * in the Robot game, possibly animated by having multiple frames (all of the
 * same size) with variable delays between them.
 *
 * @author fuerth
 * @version $Id$
 */
public interface Sprite {

    /**
     * Paints this sprite into the given graphics
     * at the given (x,y) coordinates.
     */
    void paint(Graphics2D g2, int x, int y);

    /**
     * Returns the width of a bounding box that contains all the pixels in
     * this sprite.
     */
    int getWidth();

    /**
     * Returns the height of a bounding box that contains all the pixels in
     * this sprite.
     */
    int getHeight();
    
    /**
     * Reports the scaling factor that will be applied to the source image data
     * before it is drawn (the scale factor also affects the reported width and
     * height reported by the sprite).
     */
    double getScale();
    
    /**
     * Sets the sprite's scale factor.
     */
    void setScale(double scale);
}
