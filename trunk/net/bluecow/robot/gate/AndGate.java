/*
 * Created on Jun 23, 2005
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.gate;

/**
 * The AndGate represents an AND gate with any number of inputs.
 *
 * @author fuerth
 * @version $Id$
 */
public class AndGate extends AbstractAndGate {

    /**
	 * Creates a 2-input AND gate.
	 */
	public AndGate() {
		this(2);
	}

	/**
	 * Creates a new AND gate with the specified number of inputs.
	 * 
	 * @param ninputs The number of inputs.
	 */
	public AndGate(int ninputs) {
		super(null);
		inputs = new DefaultInput[ninputs];
		for (int i = 0; i < ninputs; i++) {
			inputs[i] = new DefaultInput();
		}
	}
	
    public AndGate createDisconnectedCopy() {
        final AndGate gate = new AndGate(getInputs().length);
        gate.copyFrom(this);
        return gate;
    }

	public void evaluateInput() {
		boolean state = true;
		Gate.Input[] inputs = getInputs();
		for (int i = 0; i < inputs.length; i++) {
			state &= inputs[i].getState();
		}
		nextOutputState = state;
	}

    @Override
    protected boolean isInputInverted() {
        return false;
    }

    @Override
    protected boolean isOutputInverted() {
        return false;
    }
}
