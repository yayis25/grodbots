/*
 * Created on Aug 19, 2005
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.gate;

import java.awt.Graphics2D;
import java.awt.Rectangle;

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

    @Override
    public void drawBody(Graphics2D g2) {
        Rectangle r = getBounds();
        int backX = getInputStickLength();
        g2.drawLine(backX, 0, backX, r.height);
        g2.drawLine(backX, 0, r.width - getOutputStickLength(), r.height/2);
        g2.drawLine(backX, r.height, r.width - getOutputStickLength(), r.height/2);
    }
    
    @Override
    protected boolean isInputInverted() {
        return false;
    }

    @Override
    protected boolean isOutputInverted() {
        return true;
    }

}
