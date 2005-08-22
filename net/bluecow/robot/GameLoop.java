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
        int loopCounter = 0;
        
        robot.updateSensors();
        
        while (!stopRequested) {

            System.out.println("Starting loop "+loopCounter);

            circuitEditor.evaluateOnce();
            robot.move();
            playfield.repaint();
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                System.out.println("GameLoop was Interrupted while sleeping.");
            }
            
            loopCounter++;
        }
    }
}
