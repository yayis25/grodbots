package net.bluecow.robot.gate;

public class OrGate extends AbstractGate {
    
    public OrGate(int ninputs) {
	inputs = new DefaultInput[ninputs];
	for (int i = 0; i < ninputs; i++) {
	    inputs[i] = new DefaultInput();
	}
    }

    public boolean getOutputState() {
	boolean state = false;
	Gate.Input[] inputs = getInputs();
	for (int i = 0; i < inputs.length; i++) {
	    state |= inputs[i].getState();
	}
	return state;
    }
}
