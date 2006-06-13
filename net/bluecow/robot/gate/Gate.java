package net.bluecow.robot.gate;

import java.awt.Graphics2D;
import java.awt.Rectangle;

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

    // ----------- UI Crap ------------

    /**
     * Renders a visual depiction of this gate's body to the given graphics.
     *
     * @param g2 The graphics to draw this gate with.
     * @param r The bounds of the entire gate, including input and output sitcks.
     * @param inputStickLength The length of the input sticks.
     * @param outputStickLength The length of the output sticks.
     */
    public void drawBody(Graphics2D g2, Rectangle r, int inputStickLength, int outputStickLength);

    public void drawInputs(Graphics2D g2, Rectangle r, int inputStickLength, int outputStickLength, Input hilightInput);

    public void drawOutput(Graphics2D g2, Rectangle r, boolean highlight, int outputStickLength);

    /**
     * Sets the size for the little inversion bubbles on the input and output sticks.
     */
    //public void setCircleSize(int i);

}
