/*
 * Created on Apr 4, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.gate;


public class NorGate extends AbstractOrGate {

    /**
     * Creates a 2-input NOR gate.
     */
    public NorGate() {
        this(2);
    }

    /**
     * Creates a new NOR gate with the specified number of inputs.
     * 
     * @param ninputs The number of inputs.
     */
    public NorGate(int ninputs) {
        super(null);
        inputs = new DefaultInput[ninputs];
        for (int i = 0; i < ninputs; i++) {
            inputs[i] = new DefaultInput();
        }
    }

    public NorGate createDisconnectedCopy() {
        final NorGate newGate = new NorGate(getInputs().length);
        newGate.copyFrom(this);
        return newGate;
    }

    public void evaluateInput() {
        boolean orValue = false;
        for (Gate.Input inp : getInputs()) {
            orValue |= inp.getState();
        }
        nextOutputState = !orValue;
    }

    @Override
    protected boolean isInputInverted() {
        return true;
    }

    @Override
    protected boolean isOutputInverted() {
        return false;
    }

}
