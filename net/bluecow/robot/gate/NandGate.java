/*
 * Created on Apr 4, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.gate;


public class NandGate extends AbstractAndGate {

    /**
     * Creates a 2-input NAND gate.
     */
    public NandGate() {
        this(2);
    }

    /**
     * Creates a new NAND gate with the specified number of inputs.
     * 
     * @param ninputs The number of inputs.
     */
    public NandGate(int ninputs) {
        super(null);
        inputs = new DefaultInput[ninputs];
        for (int i = 0; i < ninputs; i++) {
            inputs[i] = new DefaultInput();
        }
    }

    public NandGate createDisconnectedCopy() {
        final NandGate newGate = new NandGate(getInputs().length);
        newGate.copyFrom(this);
        return newGate;
    }

    public void evaluateInput() {
        boolean andValue = true;
        for (Gate.Input inp : getInputs()) {
            andValue &= inp.getState();
        }
        nextOutputState = !andValue;
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
