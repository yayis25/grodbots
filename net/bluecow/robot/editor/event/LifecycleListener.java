/*
 * Created on May 10, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor.event;

public interface LifecycleListener {
    
    /**
     * Indicates that an object's life has come to an end.  The source
     * of the LifecycleEvent is the object which has reached the end of
     * its life.
     */
    void lifecycleEnding(LifecycleEvent evt);
    
}
