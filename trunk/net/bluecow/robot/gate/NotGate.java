/*
 * Created on Aug 19, 2005
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.gate;

/**
 * The NotGate class represents a single input logical NOT gate.
 *
 * @author fuerth
 * @version $Id$
 */
public class NotGate extends AbstractGate {

    /**
     * Creates a 1-input 1-output NOT gate.
     */
    public NotGate() {
        super(null);
        inputs = new DefaultInput[1];
        inputs[0] = new DefaultInput();
    }

    /**
     * Calculates the logical complement of the current input state.
     */
    public void evaluateInput() {
        nextOutputState = ! inputs[0].getState();
    }   
}
