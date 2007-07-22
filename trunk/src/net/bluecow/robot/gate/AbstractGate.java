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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * A generic gate implementation that can do everything except evaluate its
 * output state (because it's not declared to be AND, OR, NOT, XOR, NAND, etc).
 */
public abstract class AbstractGate implements Gate {

    /**
     * Controls whether or not the debugging features of this class are enabled.
     */
    private static final boolean debugOn = false;
    
    /**
     * Prints the given printf-formatted message, followed by a newline,
     * to the console if debugOn == true.
     */
    private void debugf(String fmt, Object ... args) {
        if (debugOn) debug(String.format(fmt, args));
    }

    /**
     * Prints the given string followed by a newline to the console if debugOn==true.
     */
    private void debug(String msg) {
        if (debugOn) System.out.println(msg);
    }
    
    /**
	 * The label text for this gate.
	 */
	private String label;

    /**
     * The current output state that this gate reports.
     */
	private boolean outputState;
    
	/**
	 * The most-recently calculated output state of this gate.  It will normally
     * be delayed by one evaluate() cycle before it's reported as the
     * current outputState.
	 */
    protected boolean nextOutputState;
    
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
     * Copies all the properties from the given source gate to
     * this one, except the inputs array.  This gate's inputs
     * will remain untouched.
     */
    public void copyFrom(Gate src) {
        setActiveColor(src.getActiveColor());
        setBounds(src.getBounds());
        setCircleSize(src.getCircleSize());
        setDrawingTerminations(src.isDrawingTerminations());
        setHilightColor(src.getHilightColor());
        setInputStickLength(src.getInputStickLength());
        setNormalColor(src.getNormalColor());
        setOutputStickLength(src.getOutputStickLength());
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
        
        // ----------- UI Crap ------------
        
        public Point getPosition() {
            return calcInputPosition(this);
        }
	}

    /**
     * A utility method for calculating the position of a particular input of a gate.
     * This was factored out from DefaultInput.getPosition so that other Gate.Input
     * implementations can use it.
     * 
     * @param g The gate in question
     * @param i Which input of <tt>g</tt> to get the position of
     */
    public static Point calcInputPosition(Input i) {
        Gate g = i.getGate();
        Input[] siblings = g.getInputs();
        Rectangle r = g.getBounds();
        int inputNum;
        double spacing = ((double) r.height) / ((double) siblings.length);
        for (inputNum = 0; inputNum < siblings.length && i != siblings[inputNum]; inputNum++);
        return new Point(r.x, r.y + (int) ( ((double) inputNum) * spacing + (spacing / 2.0)));
    }
	/**
	 * Returns the list of inputs.
	 */
	public Gate.Input[] getInputs() {
		return inputs;
	}

    /**
     * Implements gate delay by copying the nextOutputState to
     * the current outputState variable.
     */
    public final void latchOutput() {
        outputState = nextOutputState;
    }
    
    public final void reset() {
        outputState = false;
        nextOutputState = false;
        if (outputState || nextOutputState) {
            System.out.println("Gate.reset(): warning: output="+getOutputState()+"; next="+nextOutputState+" after reset (both should be false!)");
        }
    }
    
	// -------------- ACCESSORS and MUTATORS ------------------
	
	public String getLabel() {
		return label;
	}
	
	public final boolean getOutputState() {
	    return outputState;
	}
    
    /**
     * Returns true because this is almost always the only reasonable answer.
     */
    public boolean isOutputConnectable() {
        return true;
    }

    // -------------- UI Crap ----------------
    
    private Rectangle bounds;
    
    /**
     * The colour of a highlighted gate.
     * FIXME: this is also defined in CircuitEditor!
     */
    private Color hilightColor = Color.BLUE;
    
    /**
     * The colour of a normal (not highlighted and inactive) gate.
     * FIXME: this is also defined in CircuitEditor!
     */
    private Color normalColor = Color.WHITE;
    
    /**
     * The colour of a highlighted gate.
     * FIXME: this is also defined in CircuitEditor!
     */
    private Color activeColor = Color.ORANGE;

    /**
     * The diameter of circles that indicate signal inversion (in pixels).
     */
    private int circleSize = 6;
    
    /**
     * Controls whether or not the boxes and arrows are drawn on the ends of the
     * inputs and outputs.
     */
    private boolean drawingTerminations = true;
    
    private Font labelFont;
    
    private int inputStickLength = 22;
    
    private int outputStickLength = 20;
    
    /**
     * Draws a crappy placeholder graphic: an oval with the class name in it.  You
     * should implement your own gate bydy design and not rely on this implementation.
     */
    public void drawBody(Graphics2D g2) {
        Rectangle r = getBounds();
        g2.drawOval(0, 0, r.width, r.height);
        g2.drawString(getClass().getName(), 5, r.height/2);
    }
    
    public void drawInputs(Graphics2D g2, Input hilightInput) {
        // draw the inputs along the left edge of this gate
        Gate.Input[] inputs = getInputs();
        for (int i = 0; inputs != null && i < inputs.length; i++) {
            if (inputs[i] == hilightInput) {
                g2.setColor(getHilightColor());
            } else if (inputs[i].getState() == true) {
                g2.setColor(getActiveColor());
            } else {
                g2.setColor(getNormalColor());
            }
            
            drawInput(g2, inputs[i]);
        }

    }
    
    public void drawOutput(Graphics2D g2, boolean highlight) {
        Rectangle r = getBounds();
        Point p = new Point(r.width - outputStickLength, r.height/2);
        int length = outputStickLength;
        if (highlight) {
            g2.setColor(getHilightColor());
        } else if (getOutputState()) {
            g2.setColor(getActiveColor());
        } else {
            g2.setColor(getNormalColor());
        }
        
        if (isOutputInverted()) {
            g2.drawOval(p.x, p.y - circleSize/2, circleSize, circleSize);
            g2.drawLine(p.x + circleSize, p.y, p.x + length, p.y);
        } else {
            g2.drawLine(p.x, p.y, p.x + length, p.y);
        }
        
        if (drawingTerminations) {
            g2.drawLine(p.x + length, p.y, p.x + (int) (length*0.75), p.y - (int) (length*0.25));
            g2.drawLine(p.x + length, p.y, p.x + (int) (length*0.75), p.y + (int) (length*0.25));
        }
        
        if (label != null) {
            g2.setFont(getLabelFont());
            g2.drawString(label, p.x, p.y + 15);
        }
    }

    private void drawInput(Graphics2D g2, Gate.Input input) {
        Rectangle r = getBounds();
        Point p = input.getPosition();
        
        // adjust the point because the graphics is already translated to the gate's position
        p.x = p.x - r.x + getInputStickLength();
        p.y -= r.y;

        int connectorWidth;
        if (drawingTerminations) {
            connectorWidth = 6;
        } else {
            connectorWidth = 0;
        }
        if (isInputInverted()) {
            // the - 1 off circlesize is because the circle outline has thickness and runs into the gate's back without the adjustment
            g2.drawOval(p.x - circleSize - 1, p.y - circleSize/2, circleSize, circleSize);
            g2.drawLine(p.x - circleSize - 1, p.y, p.x - inputStickLength + connectorWidth, p.y);
        } else {
            g2.drawLine(p.x, p.y, p.x - inputStickLength + connectorWidth, p.y);
        }
        g2.drawRect(p.x - inputStickLength, p.y - (connectorWidth / 2), connectorWidth, connectorWidth);
        if (input.getLabel() != null) {
            g2.setFont(getLabelFont());
            int labelLength = g2.getFontMetrics().stringWidth(input.getLabel());
            int ascent = g2.getFontMetrics().getAscent();
            g2.drawString(input.getLabel(), p.x - labelLength, p.y + ascent + connectorWidth);
        }
    }
    
    public Input getInputAt(int x, int y) {
        final int ni = getInputs().length;  // number of inputs on this gate
        if (ni == 0) return null;
        Rectangle bb = getBounds();
        x -= bb.x;
        y -= bb.y;
        if (x < 0 || x > inputStickLength) {
            return null;
        } else {
            int index = (int) ( (float) y / ( (float) bb.height / ni));
            debugf("bb=[%d,%d,%d,%d] x=%d y=%d ni=%d index=%d",
                    bb.x,bb.y,bb.width,bb.height,
                    x,y,ni,index);
            return getInputs()[index];
        }
    }
    
    public boolean isOutput(int x, int y) {
        Rectangle bb = getBounds();
        x -= bb.x;
        y -= bb.y;
        if (x > bb.width || x < bb.width - outputStickLength) return false;
        else return true;
    }

    public Point getOutputPosition() {
        Rectangle bb = getBounds(); // the bounding box for this gate
        return new Point(bb.x + bb.width, bb.y + (bb.height / 2));
    }

    private Font getLabelFont() {
        if (labelFont == null) {
            labelFont = new Font("System", Font.PLAIN, 9);
        }
        return labelFont;
    }

    public void setActiveColor(Color activeColor) {
        this.activeColor = activeColor;
    }
    
    public Color getActiveColor() {
        return activeColor;
    }
    
    public void setHilightColor(Color hilightColor) {
        this.hilightColor = hilightColor;
    }
    
    public Color getHilightColor() {
        return hilightColor;
    }
    
    public void setNormalColor(Color normalColor) {
        this.normalColor = normalColor;
    }
    
    public Color getNormalColor() {
        return normalColor;
    }
    
    public void setCircleSize(int i) {
        circleSize = i;
    }
    
    public int getCircleSize() {
        return circleSize;
    }
    
    public void setDrawingTerminations(boolean v) {
        drawingTerminations = v;
    }
    
    public boolean isDrawingTerminations() {
        return drawingTerminations;
    }
    
    /**
     * Tells whether or not the inputs on this gate should be painted with an inversion bubble.
     */
    protected abstract boolean isInputInverted();

    /**
     * Tells whether or not the outputs on this gate should be painted with an inversion bubble.
     */
    protected abstract boolean isOutputInverted();
    
    public Rectangle getBounds() {
        if (bounds == null) {
            throw new IllegalStateException(
                    "null bounds for "+this+
                    " ("+getClass().getName()+"@"+System.identityHashCode(this)+")");
        }
        return new Rectangle(bounds);
    }
    
    public void setBounds(Rectangle v) {
        bounds = new Rectangle(v);
    }
    
    public int getInputStickLength() {
        return inputStickLength;
    }
    
    public void setInputStickLength(int inputStickLength) {
        this.inputStickLength = inputStickLength;
    }
    
    public int getOutputStickLength() {
        return outputStickLength;
    }
    
    public void setOutputStickLength(int outputStickLength) {
        this.outputStickLength = outputStickLength;
    }
}
