/*
 * Created on Feb 18, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.fx;

import java.awt.Graphics2D;
import java.awt.Point;

import net.bluecow.robot.sprite.Sprite;

/**
 * Effect is a sprite which keeps track of its own position, and has a limited
 * life span. It is actually closer to the traditional definition of a sprite
 * than the Sprite interface represents.
 * 
 * @author fuerth
 * @version $Id$
 */
public interface Effect extends Sprite {
    
    /**
     * The position where the top left-hand corner of this effect will
     * paint.
     */
    public Point getPosition();
    
    /**
     * This effect's X position.
     */
    public int getX();
    
    /**
     * This effect's Y position.
     */
    public int getY();
    
    /**
     * Paints this effect at its current position.
     */
    public void paint(Graphics2D g2);
    
    /**
     * Returns true if this effect has completed, and should no longer be
     * painted.
     */
    public boolean isFinished();
    
}
