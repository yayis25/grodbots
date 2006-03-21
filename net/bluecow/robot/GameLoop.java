/*
 * Created on Aug 20, 2005
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * The GameLoop represents the main loop of the game while it is in operation.
 *
 * @author fuerth
 * @version $Id$
 */
public class GameLoop implements Runnable {

    private Robot robot;
    private Playfield playfield;
    private CircuitEditor circuitEditor;
    
    /**
     * Set this to true to abort the current game.
     */
    private boolean stopRequested;

    /**
     * This value starts off false, then becomes true when the
     * run() method is invoked.
     */
    private boolean running;

    /**
     * Counts how many loops the game has gone through since it was started
     * (by calling the run() method).
     */
    private int loopCount;
    
    /**
     * Gets set to true if and when the robot reaches the goal.
     */
    private boolean goalReached;
    private int frameDelay;
    
    /**
     * @param robot
     * @param playfield
     * @param ce
     */
    public GameLoop(Robot robot, Playfield playfield, CircuitEditor circuitEditor) {
        this.robot = robot;
        this.playfield = playfield;
        this.circuitEditor = circuitEditor;
    }
    
    public void run() {
        synchronized (this) {
            if (running) {
                throw new IllegalStateException("Already running!");
            } else {
                running = true;
            }
        }
        
        pcs.firePropertyChange("running", false, true);
        
        try {
            while (running) {
                
                singleStep();
                
                try {
                    Thread.sleep(frameDelay);
                } catch (InterruptedException ex) {
                    System.out.println("GameLoop was Interrupted while sleeping.");
                }
            }
        } finally {
            halt();
        }
    }

    public void singleStep() {
        synchronized (this) {
            if (stopRequested || goalReached) halt();
            loopCount++;
        }

        System.out.println("Starting loop "+loopCount);
        
        robot.updateSensors();
        circuitEditor.evaluateOnce();
        robot.move();
        playfield.repaint();
        
        if (playfield.getSquareAt(robot.getPosition()).isGoal()) {
            setGoalReached(true);
        }
    }

    private void halt() {
        boolean wasRunning;
        synchronized (this) {
            wasRunning = running;
            running = false;
            stopRequested = false;
        }
        if (wasRunning) {
            pcs.firePropertyChange("running", true, false);
        }
    }
    
    /**
     * Tells whether or not the game loop is currently running.  This is a bound
     * property; to recieve change notifications, register a property change listener
     * for the "running" property.
     */
    public synchronized boolean isRunning() {
        return running;
    }
    
    /**
     * Calling this method will cause the game loop to halt during the next loop
     * iteration.  If you need to be notified when the loop has halted, you can
     * sign up for the "running" property change event.
     */
    public synchronized void requestStop() {
        stopRequested = true;
    }

    /**
     * Returns the number of loops this game loop has executed so far.
     */
    public synchronized int getLoopCount() {
        return loopCount;
    }
    
    /**
     * This becomes true when the robot has reached its goal. When the goal has
     * been reached, the game loop stops itself. This flag can be reset by
     * calling resetState(), which you will have to do before the game loop can
     * be restarted.
     */
    public synchronized boolean isGoalReached() {
        return goalReached;
    }
    
    private synchronized void setGoalReached(boolean v) {
        if (goalReached != v) {
            goalReached = v;
            pcs.firePropertyChange("goalReached", !goalReached, goalReached);
        }
    }

    /**
     * Sets the amount of time that the loop will sleep between frames.
     *
     * @param delayInMS The amount of time to sleep, in milliseconds.
     */
    public void setFrameDelay(int delayInMS) {
        frameDelay = delayInMS;
    }
    
    /**
     * Resets this game loop, its robot, and the circuit editor to their initial states.
     * 
     * @throws IllegalStateException if you call this method when the game loop
     * is running
     */
    public void resetState() {
        if (isRunning()) {
            throw new IllegalStateException("You can't reset the loop when it's running.");
        }
        setGoalReached(false);
        loopCount = 0;
        robot.setPosition(playfield.getModel().getStartPosition());
        circuitEditor.resetState();
    }

    
    // PROPERTY CHANGE STUFF (for notifying of game wins)
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }
}
