package net.bluecow.robot.gate;

public class OrGate extends AbstractOrGate {
	
	/**
	 * Creates a 2-input OR gate.
	 */
	public OrGate() {
		this(2);
	}
	
	/**
	 * Creates a new OR gate with the specified number of inputs.
	 * 
	 * @param ninputs The number of inputs.
	 */
	public OrGate(int ninputs) {
		super(null);
		inputs = new DefaultInput[ninputs];
		for (int i = 0; i < ninputs; i++) {
			inputs[i] = new DefaultInput();
		}
	}
	
	public void evaluateInput() {
		boolean state = false;
		Gate.Input[] inputs = getInputs();
		for (int i = 0; i < inputs.length; i++) {
			state |= inputs[i].getState();
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
