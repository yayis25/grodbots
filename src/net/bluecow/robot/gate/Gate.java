/*
 * Copyright (c) 2007, Jonathan Fuerth
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Jonathan Fuerth nor the names of other
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.bluecow.robot.gate;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public interface Gate {
    
    /**
     * Returns the type of gate this is, for instance "AND" "OR" "NOT" "NAND" and
     * so on.
     */
    public String getType();
    
    /**
     * Gets the label text that is associated with this gate, for instance the robot
     * outputs are labeled "Red" "Green" "Blue" etc.
     * 
     * @return The text associated with this gate, or null if the gate has no label.
     */
    public String getLabel();
    
    /**
     * Returns true if the output on this gate is allowed to be connected to another
     * gate's input; false otherwise.  This flag is amost always true; see the
     * {@link net.bluecow.robot.Robot.RobotInputsGate} for an example of why you might want to set it false.
     */
    public boolean isOutputConnectable();
    
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
     * Creates a copy of this gate with the same behaviour and number of inputs,
     * but no input or output connections.
     */
    public Gate createDisconnectedCopy();

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
        
        /**
         * Returns this input's position (the leftmost point on its stem in the
         * GUI; where a connecting wire would attach).
         */
        public Point getPosition();
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
    public void drawBody(Graphics2D g2);

    public void drawInputs(Graphics2D g2, Input hilightInput);

    public void drawOutput(Graphics2D g2, boolean highlight);

    /**
     * Sets the size for the little inversion bubbles on the input and output sticks.
     */
    public void setCircleSize(int i);

    public void setDrawingTerminations(boolean v);

    /**
     * Sets the position and size of this gate's visual manifestation.
     */
    public void setBounds(Rectangle rectangle);
    
    /**
     * Returns a copy of this gate's bounding box.  To alter a gate's position,
     * call {@link #setBounds()}.
     */
    public Rectangle getBounds();

    /**
     * Returns the position where this gate's output is (the rightmost point on its
     * output stem in the GUI; where a connecting wire would attach).
     */
    public Point getOutputPosition();
    
    /**
     * Returns the input of this gate which is at or near the given point, in
     * the same coordinate space as this gate's bounding box (that is, relative
     * to the top left corner of the editor). In this implementation, inputs are
     * equally spaced off of the left-hand side of the gate's bounding
     * rectangle, so this is just a simple calculation. Subclasses may become
     * provide a more sophisticated implementation as required.
     * 
     * @param x
     *            The x position of the point of interest in the editor's
     *            coordinate system.
     * @param y
     *            The y position of the point of interest in the editor's
     *            coordinate system.
     * @return The nearest input, or null if there is no input nearby.
     */
    public Input getInputAt(int x, int y);

    /**
     * Returns true if the given coordinates are reasonably close to this gate's
     * output stick.
     */
    public boolean isOutput(int x, int y);

    public int getInputStickLength();
    public void setInputStickLength(int v);
    public int getOutputStickLength();
    public void setOutputStickLength(int v);
    public Color getActiveColor();
    public int getCircleSize();
    public boolean isDrawingTerminations();
    public Color getHilightColor();
    public Color getNormalColor();
}
