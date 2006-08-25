/*
 * Created on Aug 22, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

/**
 * A simple class that you can inherit from or delegate to when
 * implementing the Labelable interface. All getters and setters do the
 * obvious straightforward things.
 *
 * @author fuerth
 * @version $Id$
 */
public class LabelableSupport implements Labelable {

    private String label;
    private boolean enabled;
    private Direction direction;
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isLabelEnabled() {
        return enabled;
    }

    public void setLabelEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Direction getLabelDirection() {
        return direction;
    }

    public void setLabelDirection(Direction direction) {
        this.direction = direction;
    }

}
