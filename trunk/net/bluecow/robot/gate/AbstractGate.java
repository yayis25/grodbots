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
    }
    
	// -------------- ACCESSORS and MUTATORS ------------------
	
	public String getLabel() {
		return label;
	}
	
	public boolean getOutputState() {
	    return outputState;
	}
    
    
    // -------------- UI Crap ----------------
    private Color hilightColor;
    
    private Color normalColor;
    
    private Color activeColor;

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
    
    /**
     * Draws a crappy placeholder graphic: an oval with the class name in it.  You
     * should implement your own gate bydy design and not rely on this implementation.
     */
    public void drawBody(Graphics2D g2, Rectangle r, int inputStickLength, int outputStickLength) {
        g2.drawOval(0, 0, r.width, r.height);
        g2.drawString(getClass().getName(), 5, r.height/2);
    }
    
    public void drawInputs(Graphics2D g2, Rectangle r, int inputStickLength, int outputStickLength, Input hilightInput) {
        // draw the inputs along the left edge of this gate
        Gate.Input[] inputs = getInputs();
        Point inputLoc = new Point(inputStickLength, 0);
        for (int i = 0; inputs != null && i < inputs.length; i++) {
            if (inputs[i] == hilightInput) {
                g2.setColor(getHilightColor());
            } else if (inputs[i].getState() == true) {
                g2.setColor(getActiveColor());
            } else {
                g2.setColor(getNormalColor());
            }
            
            inputLoc.y = (int) ((0.5 + i) * (double) r.height / (double) inputs.length);
            drawInput(g2, inputLoc, inputs[i], isInputInverted(), inputStickLength, outputStickLength);
        }

    }
    
    public void drawOutput(Graphics2D g2, Rectangle r, boolean highlight, int outputStickLength) {
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

    private void drawInput(Graphics2D g2, Point p, Gate.Input input, boolean invert, int inputStickLength, int outputStickLength) {
        int connectorWidth;
        if (drawingTerminations) {
            connectorWidth = 6;
        } else {
            connectorWidth = 0;
        }
        if (invert) {
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
    
    public void setDrawingTerminations(boolean v) {
        drawingTerminations = v;
    }
    
    /**
     * Tells whether or not the inputs on this gate should be painted with an inversion bubble.
     */
    protected abstract boolean isInputInverted();

    /**
     * Tells whether or not the outputs on this gate should be painted with an inversion bubble.
     */
    protected abstract boolean isOutputInverted();
    
}
