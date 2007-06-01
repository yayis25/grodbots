/*
 * Created on Feb 18, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.fx;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.Map;

public abstract class AbstractEffect implements Effect {

    /**
     * This effect's current position and size.
     */
    protected Rectangle bounds = new Rectangle();
    
    /**
     * This effect's scaling factor. Scales the size that this effect paints
     * itself at, but not its position. The implementation of paint() should
     * respect this value.
     */
    protected double scale = 1.0;
    
    /**
     * This effect's transform.  This abstract effect class does not attempt to do
     * anything with the transform beyond remembering its value for the get and
     * set methods.  Subclasses are responsible for applying this effect in whatever
     * way is appropriate.
     */
    protected AffineTransform transform;
    
    protected AbstractEffect() {
        // nothing to do
    }

    /**
     * Returns an empty map.
     */
    public Map<String, String> getAttributes() {
        return Collections.emptyMap();
    }

    public int getX() {
        return bounds.x;
    }
    
    public int getY() {
        return bounds.y;
    }
    
    public int getHeight() {
        return bounds.height;
    }

    public int getWidth() {
        return bounds.width;
    }
    
    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }
    
    public AffineTransform getTransform() {
        return transform;
    }

    public void setTransform(AffineTransform transform) {
        this.transform = transform;
    }

    public abstract void nextFrame();
    
    /**
     * Left for implementations to implement.
     */
    public abstract void paint(Graphics2D g2, int x, int y);

    /**
     * Paints this effect at its current position by calling the
     * {@link #paint(Graphics2D, int, int)} method.
     */
    public void paint(Graphics2D g2) {
        paint(g2, bounds.x, bounds.y);
    }

    /**
     * Returns a new Point object of the current bounds rectangle's
     * position.
     */
    public Point getPosition() {
        return new Point(bounds.x, bounds.y);
    }

    /**
     * Not implemented, because it is up to each effect to know when its
     * time is up.
     */
    public abstract boolean isFinished();
    
    /**
     * Not currently implemented.
     */
    @Override
    public Effect clone() {
        throw new RuntimeException("There isn't currently a generic clone for effects.");
    }
}
