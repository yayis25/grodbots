/*
 * Created on Aug 20, 2005
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

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
        
        robot.updateSensors();
        
        loopCount = 0;
        
        for (;;) {
            synchronized (this) {
                if (stopRequested || goalReached) break;
                loopCount++;
            }
            
            System.out.println("Starting loop "+loopCount);

            circuitEditor.evaluateOnce();
            robot.move();
            playfield.repaint();
            
            if (playfield.getSquareAt(robot.getPosition()).isGoal()) {
                synchronized (this) {
                    goalReached = true;
                }
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                System.out.println("GameLoop was Interrupted while sleeping.");
            }
        }
        
        synchronized (this) {
            running = false;
            stopRequested = false;
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }
    
    public synchronized void requestStop() {
        stopRequested = true;
    }
    
    public synchronized int getLoopCount() {
        return loopCount;
    }
    
    public synchronized boolean isGoalReached() {
        return goalReached;
    }
}
