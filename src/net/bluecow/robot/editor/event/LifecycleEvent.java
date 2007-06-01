/*
 * Created on May 10, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor.event;

import java.util.EventObject;

/**
 * The LifecycleEvent is passed to LifecycleListeners when certain objects
 * reach milestones in their lifecycles.
 * <p>
 * Exactly which milestone has been reached is
 * not a property of this event object; it is inferred based on which LifecycleListener
 * method this object is passed to by the event's source.
 */
public class LifecycleEvent extends EventObject {

    public LifecycleEvent(Object source) {
        super(source);
    }
    
}
