package net.bluecow.robot.gate;

/**
 * A generic gate implementation that can do everything except evaluate its
 * output state (because it's not declared to be AND, OR, NOT, XOR, NAND, etc).
 */
public abstract class AbstractGate implements Gate {

	/**
	 * The label text for this gate.
	 */
	private String label;
	
	/**
	 * The most-recently calculated output state of this gate.
	 */
	protected boolean outputState;
	
	/**
	 * These are the inputs to this gate. Subclass constructors should
	 * initialise this array to the correct length and types.
	 */
	protected Gate.Input inputs[];

	/**
	 * Creates a new gate with the given label.
	 * 
	 * @param label The label associated with this gate.
	 */
	protected AbstractGate(String label) {
		this.label = label;
	}
	
	/**
	 * The Input class represents a single input to its enclosing gate instance.
	 * A gate can have 0 or more of these in its inputs list.
	 */
	public class DefaultInput implements Gate.Input {

	    private String label;
	    
		/**
		 * This is the gate we monitor for changes, and whose output state we
		 * report as our input state.
		 */
		private Gate inputGate;

		/**
		 * Connects this input to the output of the given gate. If this input
		 * was already connected, the existing connection is broken.
		 */
		public void connect(Gate g) {
			inputGate = g;
		}

		/**
		 * Returns the current state of this input (which is the output state of
		 * the gate it is connected to). If this input is not connected, its
		 * state defaults to false.
		 */
		public boolean getState() {
			if (inputGate != null) {
				return inputGate.getOutputState();
			} else {
				return false;
			}
		}
		
		public Gate getConnectedGate() {
		    return inputGate;
		}
		
		public Gate getGate() {
		    return AbstractGate.this;
		}

		public String getLabel() {
		    return label;
		}
		
	    /**
         * See {@link #label}.
         */
        public void setLabel(String label) {
            this.label = label;
        }
	}

	/**
	 * Returns the list of inputs.
	 */
	public Gate.Input[] getInputs() {
		return inputs;
	}

	// -------------- ACCESSORS and MUTATORS ------------------
	
	public String getLabel() {
		return label;
	}
	
	public boolean getOutputState() {
	    return outputState;
	}
}
