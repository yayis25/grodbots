/*
 * Created on Aug 20, 2005
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import net.bluecow.robot.LevelConfig.Switch;
import bsh.EvalError;

/**
 * The GameLoop represents the main loop of the game while it is in operation.
 *
 * @author fuerth
 * @version $Id$
 */
public class GameLoop implements Runnable {

    private List<Robot> robots = new ArrayList<Robot>();
    
    private Playfield playfield;
    
    private LevelConfig level;
    
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
     * Tells whether or not outside code has intentionally started
     * the game loop.
     * 
     * <p>Justification for this extra state flag: The playfield sometimes
     * requires extra repaints in order to finish effects that are synchronized
     * with frame repaints (to reduce aliasing of the effect's framerate with
     * the repaint rate). If all the playfield effects were asynchronous, this
     * flag could be eliminated because you'd only need to keep repainting the
     * playfield when the game is running.
     */
    //private boolean started;

    /**
     * Counts how many loops the game has gone through since it was started
     * (by calling the run() method).
     */
    private int loopCount;
    
    /**
     * Gets set to true if and when the robot reaches the goal.
     */
    private boolean goalReached;
    
    /**
     * Time to sleep between loops (in milliseconds).
     */
    private int frameDelay = 50;
    
    /**
     * @param robot
     * @param playfield
     */
    public GameLoop(Collection<Robot> robots, LevelConfig level, Playfield playfield) {
        this.level = level;
        this.playfield = playfield;
        for (Robot r : robots) {
            addRobot(r);
        }
    }
    
    public final void addRobot(Robot robot) {
        robots.add(robot);
    }

    /**
     * Removes the given robot from this game loop.
     */
    public final void removeRobot(Robot robot) {
        robots.remove(robot);
    }

    public void run() {
        synchronized (this) {
            if (running) {
                throw new IllegalStateException("Already running!");
            } else {
                running = true;
            }
        }
        
        playfield.setAsyncRepaint(false);
        
        pcs.firePropertyChange("running", false, true);
        
        try {
            while (running) {
                
                if (running) {
                    singleStep();
                }
                
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
            if (stopRequested || goalReached) {
                halt();
                return;
            }
            loopCount++;
        }

        boolean allGoalsReached = true;
        for (Robot robot : robots) {
            boolean thisGoalReached = robot.isGoalReached();
            if (!thisGoalReached) {
                robot.updateSensors();
                for (int i = 0; i < robot.getEvalsPerStep(); i++) {
                    // should this loop be in the robot's circuit instead?
                    robot.getCircuit().evaluateOnce();
                }
                Point2D.Float oldPos = robot.getPosition();
                robot.move();
                if (!isSameSquare(oldPos, robot.getPosition())) {
                    Switch exitingSwitch = level.getSwitch(oldPos);
                    Switch enteringSwitch = level.getSwitch(robot.getPosition());
                    try {
                        if (exitingSwitch != null) exitingSwitch.onExit(robot);
                        if (enteringSwitch != null) enteringSwitch.onEnter(robot);
                    } catch (EvalError e) {
                        JOptionPane.showMessageDialog(null, "Error evaluating switch:\n"+e.getMessage());
                    }
                }
                // XXX: should we re-check if the goal is reached, or wait for the next loop?
            }
            allGoalsReached &= thisGoalReached; 
        }
        
        playfield.setFrameCount(loopCount);
        playfield.repaint();
        
        if (allGoalsReached) {
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
        playfield.setAsyncRepaint(true);
        if (wasRunning) {
            pcs.firePropertyChange("running", true, false);
        }
    }
    
    private static boolean isSameSquare(Point2D.Float p1, Point2D.Float p2) {
        return (Math.floor(p1.x) == Math.floor(p2.x)) &&
               (Math.floor(p1.y) == Math.floor(p2.y));
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
     * Calling this method with the parameter <tt>true</tt> will cause the
     * game loop to halt during the next loop iteration. If you need to be
     * notified when the loop has halted, you can sign up for the "running"
     * property change event.
     */
    public synchronized void setStopRequested(boolean v) {
        stopRequested = v;
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
    
    public int getFrameDelay() {
        return frameDelay;
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
        level.resetState();
        for (Robot robot : robots) {
            robot.resetState();
        }
        playfield.setLevel(level);
        playfield.setFrameCount(null);
        playfield.setAsyncRepaint(true);
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
