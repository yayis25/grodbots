/*
 * Created on Apr 18, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import java.awt.Graphics2D;
import java.util.Map;

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
     * Returns the attribute map that was passed to the SpriteManager in
     * order to create this sprite object.
     */
    Map<String, String> getAttributes();
    
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
    
    /**
     * Tells this sprite that a frame of the overall gamee has passed, and it
     * should paint the next frame in its sequence next time it is asked to paint.
     */
    void nextFrame();
}
