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
public class AndGate extends AbstractGate {
	/**
	 * Creates a 2-input OR gate.
	 */
	public AndGate() {
		this(2);
	}
	
	/**
	 * Creates a new OR gate with the specified number of inputs.
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
	
	public boolean getOutputState() {
		boolean state = true;
		Gate.Input[] inputs = getInputs();
		for (int i = 0; i < inputs.length; i++) {
			state &= inputs[i].getState();
		}
		return state;
	}
}
