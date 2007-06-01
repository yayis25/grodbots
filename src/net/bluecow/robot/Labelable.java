/*
 * Created on Aug 21, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

/**
 * The Labelable interface provides all the methods required for labeling
 * an object in the robot game.
 *
 * @author fuerth
 * @version $Id$
 */
public interface Labelable {
    
    /**
     * Returns the text of this object's label.
     * @return The label for this object.  If this object should not have a
     * label, the return value is null.
     */
    String getLabel();
    
    /**
     * Sets the text of this object's label.
     * @param label The label text for this object.  Null is allowed, and means
     * that this object has no label.
     */
    void setLabel(String label);
    
    /**
     * Returns true if the label should be painted; false otherwise.
     */
    boolean isLabelEnabled();
    
    /**
     * Enables or disables painting of the label for this object.
     * 
     * @param enabled true for enabled; false for disabled.
     */
    void setLabelEnabled(boolean enabled);
    
    /**
     * Returns the direction, away from this Labelable object, that the label
     * should be positioned.
     */
    Direction getLabelDirection();
    
    
    /**
     * Sets the direction, away from this Labelable object, that the label
     * should be positioned.
     */
    void setLabelDirection(Direction direction);
}
