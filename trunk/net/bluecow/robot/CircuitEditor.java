package net.bluecow.robot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;

import net.bluecow.robot.gate.AndGate;
import net.bluecow.robot.gate.Gate;
import net.bluecow.robot.gate.OrGate;
import net.bluecow.robot.gate.Gate.Input;

public class CircuitEditor extends JPanel {

    /**
     * The CircuitEditorComponentListener shifts the inputs and outputs along the edge as necessary.
     */
    public class CircuitEditorComponentListener implements ComponentListener {

        public void componentResized(ComponentEvent e) {
            double height = getHeight();
            double n = outputs.length;
            for (int i = 0; i < outputs.length; i++) {
                int y = (int) ( ((double) i) * (height / n) + (height / n / 2.0) - DEFAULT_GATE_HEIGHT/2.0 );
                gatePositions.put(outputs[i], new Rectangle(0, y, OUTPUT_STICK_LENGTH, DEFAULT_GATE_HEIGHT));
            }
            gatePositions.put(inputsGate, new Rectangle(getWidth() - INPUT_STICK_LENGTH, 0, INPUT_STICK_LENGTH, getHeight()));
        }

        public void componentMoved(ComponentEvent e) {
            // NOP
        }

        public void componentShown(ComponentEvent e) {
            // NOP
        }

        public void componentHidden(ComponentEvent e) {
            // NOP
        }
    }

    /**
	 * The AddGateAction adds a new instance of a gate to the enclosing circuit editor. 
	 *
	 * @author fuerth
	 * @version $Id$
	 */
	public class AddGateAction extends AbstractAction implements Action {

		/**
		 * The type of gate that will be created when this action is invoked.
		 */
		private Class gateClass;
		
		public AddGateAction(Class gateClass) {
			this.gateClass = gateClass;
		}
		
		public void actionPerformed(ActionEvent e) {
			try {
				Gate newGate = (Gate) gateClass.newInstance();
				Point p = new Point(10, 10);
				addGate(newGate, p);
				System.out.println("Added new "+newGate.getClass().getName()+" at "+p);
			} catch (InstantiationException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(CircuitEditor.this, "Couldn't create new Gate instance:\n"+e1.getMessage());
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(CircuitEditor.this, "Couldn't access new Gate instance:\n"+e1.getMessage());
			}
		}

	}
	private Gate[] outputs;

	private Gate inputsGate;

	/**
	 * Maps Gate instances to Rectangles that say where and how big to paint them.
	 */
	private Map gatePositions = new HashMap();
	
	/**
	 * The gate that should be highlighted.  This is the gate that currently 
	 * has keyboard and mouse focus.
	 */
	private Gate hilightGate;

	/**
	 * The Input that the sure wants to connect to an output.  This is set to 
	 * null when there is not a connect operation in progress.
	 */
	private Gate.Input connectionStartInput;
	
	private Gate movingGate;
	
	/**
	 * The colour to make a gate or connection which is active (in the TRUE/ON state). 
	 */
	private Color activeColor = Color.orange;
	
	/**
	 * The colour to make the highlight gate.
	 */
	private Color hilightColor = Color.BLUE;
	
	/**
	 * The font we use for labelling.
	 */
	private Font labelFont;

	private static final int DEFAULT_GATE_WIDTH = 50;

	private static final int DEFAULT_GATE_HEIGHT = 50;
	
	private static final int INPUT_STICK_LENGTH = 20;
	
	private static final int OUTPUT_STICK_LENGTH = 20;
	
	public CircuitEditor(Gate[] outputs, Gate inputs) {
		setupKeyActions();
		addComponentListener(new CircuitEditorComponentListener());
		setPreferredSize(new Dimension(400, 400));
		this.outputs = outputs;
		this.inputsGate = inputs;
		//gatePositions.put(inputsGate, new Rectangle(getWidth() - INPUT_STICK_LENGTH, 0, INPUT_STICK_LENGTH, getHeight()));
		MouseInput mouseListener = new MouseInput();
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}

	private void setupKeyActions() {
		getInputMap().put(KeyStroke.getKeyStroke('a'), "addGate(AND)");
		getInputMap().put(KeyStroke.getKeyStroke('o'), "addGate(OR)");
		
		getActionMap().put("addGate(AND)", new AddGateAction(AndGate.class));
		getActionMap().put("addGate(OR)", new AddGateAction(OrGate.class));
	}

	private void paintGate(Graphics2D g2, Gate gate, Rectangle r) {
		g2.translate(r.x, r.y);
		if (gate == hilightGate) g2.setColor(hilightColor);

		// for debugging the draw routine
		g2.drawRect(0, 0, r.width, r.height);
		
		// draw the inputs along the left edge of this gate
		Gate.Input[] inputs = gate.getInputs();
		Point inputLoc = new Point(INPUT_STICK_LENGTH, 0);
		for (int i = 0; inputs != null && i < inputs.length; i++) {
			if (inputs[i].getState() == true) {
				g2.setColor(getActiveColor());
			} else {
				g2.setColor(getForeground());
			}

			inputLoc.y = (int) ((0.5 + i) * (double) r.height / (double) inputs.length);
			paintInput(g2, inputLoc, inputs[i]);
		}

		// and the output
		paintOutput(g2, new Point(r.width - OUTPUT_STICK_LENGTH, r.height/2), gate.getLabel());
		
		// individual gate bodies (XXX: should probably farm this out to the gates themselves)
		if (gate instanceof OrGate) {
			g2.drawArc(0, 0, 20, r.height, 30, 30);
		} else if (gate instanceof Robot.RobotSensorOutput) {
		    // nothing to draw: this should be squished against the left side of the editor
		} else {
			g2.drawOval(0, 0, r.width, r.height);
			g2.drawString(gate.getClass().getName(), 5, r.height/2);
		}
		if (gate == hilightGate) g2.setColor(getForeground());
		g2.translate(-r.x, -r.y);
		
		// paint the connecting lines
		for (int i = 0; inputs != null && i < inputs.length; i++) {
			if (inputs[i].getState() == true) {
				g2.setColor(getActiveColor());
			} else {
				g2.setColor(getForeground());
			}

		    if (inputs[i].getConnectedGate() != null) {
		        Rectangle ir = (Rectangle) gatePositions.get(inputs[i].getConnectedGate());
		        if (ir != null) {
		            inputLoc = getInputLocation(inputs[i]);
		            g2.drawLine(inputLoc.x, inputLoc.y, ir.x + ir.width, ir.y + (ir.height / 2));
		        }
		    }
		}
	}

	private void paintOutput(Graphics2D g2, Point p, String label) {
	    int length = OUTPUT_STICK_LENGTH;
	    g2.drawLine(p.x, p.y, p.x + length, p.y);
		g2.drawLine(p.x + length, p.y, p.x + (int) (length*0.75), p.y - (int) (length*0.25));
		g2.drawLine(p.x + length, p.y, p.x + (int) (length*0.75), p.y + (int) (length*0.25));
		if (label != null) {
			g2.setFont(getLabelFont(g2));
			g2.drawString(label, p.x, p.y + 15);
		}
	}

	private void paintInput(Graphics2D g2, Point p, Gate.Input input) {
		g2.drawOval(p.x - INPUT_STICK_LENGTH, p.y - 5, 10, 10);
		g2.drawLine(p.x, p.y, p.x - INPUT_STICK_LENGTH + 10, p.y);
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setColor(getForeground());
		
		Iterator it = gatePositions.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry ent = (Map.Entry) it.next();
			Gate gate = (Gate) ent.getKey();
			Rectangle gr = (Rectangle) ent.getValue();
			paintGate(g2, gate, gr);
		}
	}
	
	public void addGate(Gate g, Point p) {
		gatePositions.put(g, new Rectangle(p.x, p.y, DEFAULT_GATE_WIDTH, DEFAULT_GATE_HEIGHT));
		repaint();
	}

	public Gate getGateAt(Point p) {
		Iterator it = gatePositions.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry ent = (Map.Entry) it.next();
			Rectangle r = (Rectangle) ent.getValue();
			if (r.contains(p)) return (Gate) ent.getKey();
		}
		return null;
	}
	
	public void connectGates(Gate source, Gate.Input target) {
	    try {
	        target.connect(source);
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(this, "Couldn't connect gates:\n"+e.getMessage());
	    }
        repaint();
	}
	
	// ---------------- Accessors and Mutators --------------------
	public void setActiveColor(Color v) {
		this.activeColor = v;
		repaint();
	}

	public Color getActiveColor() {
		return activeColor;
	}
	
	private Font getLabelFont(Graphics2D g) {
		if (labelFont == null) {
			labelFont = new Font("System", Font.PLAIN, 9);
		}
		return labelFont;
	}
	
	/**
	 * @param gate The gate that should be highlighted and given input focus.
	 */
	private void setHilightGate(Gate gate) {
	    if (gate != hilightGate) {
	        hilightGate = gate;
	        repaint();
	    }
	}
	
	private class MouseInput extends MouseInputAdapter {
	    public static final int MODE_IDLE = 1;
	    public static final int MODE_CONNECTING = 2;
	    public static final int MODE_MOVING = 3;
	    
	    private int mode = MODE_IDLE;
	    
		public void mouseDragged(MouseEvent e) {
		    if (mode == MODE_CONNECTING) {
		        repaint();
		    } else if (mode == MODE_MOVING) {
		        Point p = e.getPoint();
		        Rectangle r = (Rectangle) gatePositions.get(movingGate);
		        r.x = p.x;
		        r.y = p.y;
		        repaint();
		    }
		}

		public void mouseMoved(MouseEvent e) {
		    if (mode == MODE_IDLE) {
		        setHilightGate(getGateAt(e.getPoint()));
		    }
		}

        public void mousePressed(MouseEvent e) {
            // TODO: pop up a contextual menu for adding gates
            if (mode == MODE_IDLE) {
                Point p = e.getPoint();
                Gate g = getGateAt(e.getPoint());
                if (g != null) {
                    Gate.Input inp = getGateInput(g, p.x, p.y); 
                    if (inp != null) {
                        mode = MODE_CONNECTING;
                        connectionStartInput = inp;
                    } else {
                        mode = MODE_MOVING;
                        movingGate = g;
                    }
                }
            }
		}

        public void mouseReleased(MouseEvent e) {
            if (mode == MODE_CONNECTING) {
                Gate g = getGateAt(e.getPoint());
                if (g != null) {
                    connectGates(g, connectionStartInput);
                }
                connectionStartInput = null;
                mode = MODE_IDLE;
            } else if (mode == MODE_MOVING) {
                movingGate = null;
                mode = MODE_IDLE;
            }
        }
	}
	
	/**
	 * Returns the input of the gate which is at or near the given
	 * point (relative to the top left corner of the editor).  Currently,
	 * inputs are equally spaced off of the left-hand side of the gate's
	 * bounding rectangle, so this is just a simple calculation.  It
	 * may become more sophisticated as required.
	 * 
	 * @param g The gate.
	 * @param x The x location of the point of interest in the editor's coordinate system.
	 * @param y The y location of the point of interest in the editor's coordinate system.
	 * @return The nearest input, or null if there is no input nearby.
	 */
	private Input getGateInput(Gate g, int x, int y) {
	    final int ni = g.getInputs().length;  // number of inputs on this gate
	    Rectangle bb = (Rectangle) gatePositions.get(g); // the bounding box for this gate
	    x -= bb.x;
	    y -= bb.y;
	    if (x < 0 || x > INPUT_STICK_LENGTH) return null;
	    else return g.getInputs()[y / (bb.height / ni)];
	}
	
	private Point getInputLocation(Gate.Input inp) {
	    Gate enclosingGate = inp.getGate();
	    if (enclosingGate == null) {
	        // this doesn't happen under normal circumstances
	        return new Point(getWidth(), getHeight());
	    }
	    Rectangle r = (Rectangle) gatePositions.get(enclosingGate);
	    if (r == null) {
	        throw new IllegalStateException("Can't determine location of gate input "+inp
	                +" because its gate isn't in the gatePositions map.");
	    } else {
	        int inputNum;
	        Gate.Input[] siblings = enclosingGate.getInputs();
	        double spacing = ((double) r.height) / ((double) siblings.length);
	        for (inputNum = 0; inputNum < siblings.length && inp != siblings[inputNum]; inputNum++);
	        return new Point(r.x, r.y + (int) ( ((double) inputNum) * spacing + (spacing / 2.0)));
	    }
	}
}
