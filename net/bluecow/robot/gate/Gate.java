package net.bluecow.robot.gate;

public interface Gate {
    
    /**
     * Gets the label text that is associated with this gate.
     * 
     * @return The text associated with this gate, or null if the gate has no label.
     */
    public String getLabel();
    
    /**
     * Returns the current output state of this gate.
     */
    public boolean getOutputState();
    
    /**
     * Re-evaluates this gate's future state based on its current input states.
     */
    public void evaluateInput();
    
    /**
     * Latches output state to whatever was previously determined by evaluateInput().
     */
    public void latchOutput();
    
    public Gate.Input[] getInputs();

    /**
     * Resets this gate to its default output state, regardless of the
     * current input values.
     */
    public void reset();

    /**
     * The input interface represents an input to a gate.  A gate will
     * have 0 or more inputs.
     */
    public interface Input {
        
        /**
         * Connects this input to the given gate's output.
         */
        public void connect(Gate g);
        
        /**
         * Returns the current state of this input.
         */
        public boolean getState();
        
        /**
         * Returns the gate that this input is connected to.  The value is null
         * if this input is not connected.
         */
        public Gate getConnectedGate();
        
        /**
         * Returns the gate that this is an input for.
         */
        public Gate getGate();
        
        /**
         * Returns the label to be painted near this input.
         * 
         * @return The label, or null for no label.
         */
        public String getLabel();
    }
}
