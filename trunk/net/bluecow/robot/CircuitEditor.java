package net.bluecow.robot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;

import net.bluecow.robot.gate.AndGate;
import net.bluecow.robot.gate.Gate;
import net.bluecow.robot.gate.NotGate;
import net.bluecow.robot.gate.OrGate;
import net.bluecow.robot.gate.Gate.Input;

public class CircuitEditor extends JPanel {

    /**
     * Removes the highlighted wire or gate.  If both a gate and a wire are
     * highlighted, removes only the wire.
     */
    private class RemoveAction extends AbstractAction {

        public RemoveAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            if (locked) return;
            if (hilightWireInput != null) {
                hilightWireInput.connect(null);
                hilightWireInput = null;
                repaint();
                playSound("delete_gate");  // XXX: should have a new sound!

            } else if (hilightGate != null) {
                
                // disconnect all the inputs the hilight gate outputs to
                for (Gate g : gatePositions.keySet()) {
                    for (int i = 0; i < g.getInputs().length; i++) {
                        if (g.getInputs()[i].getConnectedGate() == hilightGate) {
                            g.getInputs()[i].connect(null);
                        }
                    }
                }
                
                // disconnect the outputs from the hilighted gate's inputs
                for (int i = 0; i < hilightGate.getInputs().length; i++) {
                    hilightGate.getInputs()[i].connect(null);
                }
                
                if (permanentGates.contains(hilightGate)) {
                    // TODO: play a "you suck" type sound
                    return;
                }
                
                gatePositions.remove(hilightGate);
                repaint();
                playSound("delete_gate");
            }
        }

    }
    
    private class RemoveAllAction extends AbstractAction {
        public RemoveAllAction() {
            super("Clear");
        }
        
        public void actionPerformed(ActionEvent e) {
            if (locked) return;
            removeAllGates();
            // TODO: play a sound
        }
    }

    private static final class ConnectionLine {
        private Point fixedEnd;
        private Point cursorEnd;
        private boolean couldConnect;
        
        /**
         * Creates a new ConnectionLine with the given fixed end and cursor end.
         * 
         * @param fixedEnd the end of the cursor that stays in the same place throughout
         * the connection operation.
         * @param cursorEnd the end of the line that moves around according to the
         * user's whim.
         * @param couldConnect true iff the line currently represents a valid connection
         * in the circuit editor.
         */
        public ConnectionLine(Point fixedEnd, Point cursorEnd, boolean couldConnect) {
            this.fixedEnd = fixedEnd;
            this.cursorEnd = cursorEnd;
            this.couldConnect = couldConnect;
        }
        
        public ConnectionLine moveCursor(Point newCursorEnd, boolean couldConnect) {
            return new ConnectionLine(fixedEnd, newCursorEnd, couldConnect);
        }

        public Point getCursorEnd() {
            return new Point(cursorEnd);
        }

        public Point getFixedEnd() {
            return new Point(fixedEnd);
        }
        
        public boolean isConnectionPossible() {
            return couldConnect;
        }
    }

    /**
     * The CircuitEditorLayout shifts the inputs and outputs
     * along the edge as necessary.
     */
    public static class CircuitEditorLayout implements LayoutManager {

        public void addLayoutComponent(String name, Component comp) {
            // NOP
        }

        public void removeLayoutComponent(Component comp) {
            // NOP
        }

        public Dimension preferredLayoutSize(Container parent) {
            return parent.getPreferredSize();
        }

        public Dimension minimumLayoutSize(Container parent) {
            return parent.getPreferredSize();
        }

        public void layoutContainer(Container parent) {
            CircuitEditor ce = (CircuitEditor) parent;
            double height = ce.getHeight();
            double n = ce.outputs.size();
            for (int i = 0; i < ce.outputs.size(); i++) {
                int y = (int) ( ((double) i) * (height / n) + (height / n / 2.0) - DEFAULT_GATE_HEIGHT/2.0 );
                ce.gatePositions.put(ce.outputs.get(i), new Rectangle(0, y, OUTPUT_STICK_LENGTH, DEFAULT_GATE_HEIGHT));
            }
            ce.gatePositions.put(ce.inputsGate, new Rectangle(ce.getWidth() - INPUT_STICK_LENGTH, 0, INPUT_STICK_LENGTH + OUTPUT_STICK_LENGTH, ce.getHeight()));
        }
    }

    /**
	 * The AddGateAction adds a new instance of a gate to the enclosing circuit editor. 
	 */
    public class AddGateAction extends AbstractAction implements Action {
        
        /**
         * The type of gate that will be created when this action is invoked.
         */
        private Class gateClass;
        
        private String audioClipName;
        
        public AddGateAction(Class gateClass, String name, String audioClipName) {
            super(name);
            this.gateClass = gateClass;
            this.audioClipName = audioClipName;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (locked) return;
            try {
                Gate newGate = (Gate) gateClass.newInstance();
                Point p = new Point(newGatePosition);
                addGate(newGate, p);
                playSound(audioClipName);
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
	private List<Gate> outputs;

	private Gate inputsGate;

	/**
	 * Maps Gate instances to Rectangles that say where and how big to paint them.
	 */
	private Map<Gate,Rectangle> gatePositions = new HashMap<Gate,Rectangle>();
	
    /**
     * The gates in this set should not be deletable by the user.
     */
    private Set<Gate> permanentGates = new HashSet<Gate>();
    
	/**
	 * The gate that should be highlighted.  This is the gate that currently 
	 * has keyboard and mouse focus.
	 */
	private Gate hilightGate;

	/**
	 * The Input that the user wants to connect to an output.  This is set to 
	 * null when there is not a connect-from-input operation in progress.
	 */
	private Gate.Input connectionStartInput;
	
    /**
     * The Gate that the user wants to connect to an input.  This is set to 
     * null when there is not a connect-from-output operation in progress.
     */
    private Gate connectionStartOutput;

    /**
     * The line segment that the user sees while trying to connect a pair of gates together.
     * This is set to null when there is not a connection operation in progress.
     */
    private ConnectionLine pendingConnectionLine;

    /**
     * The position where AddGateAction should add a gate.  Gets updated by
     * the mouse listener.
     */
    private Point newGatePosition = new Point(10, 10);

    /**
     * The gate input that should be highlighted (because the cursor is over it).
     */
    private Gate.Input hilightInput;

    /**
     * The wire connected to this input should be highlighted (because the cursor is over it).
     */
    private Gate.Input hilightWireInput;

    /**
     * The gate whose output should be highlighted (because the cursor is over it).
     */
    private Gate hilightOutput;

    /**
     * The gate that's currently being dragged around.
     */
	private Gate movingGate;
	
	/**
	 * The colour to make a gate or connection which is active (in the TRUE/ON state). 
	 */
	private Color activeColor = Color.orange;
	
	/**
	 * The colour to make the highlighted gates or inputs.
	 */
	private Color hilightColor = Color.BLUE;
	
	/**
	 * The font we use for labelling.
	 */
	private Font labelFont;

    private AddGateAction addAndGateAction;

    private AddGateAction addOrGateAction;

    private AddGateAction addNotGateAction;

    private RemoveAction removeAction;
    
    private RemoveAllAction removeAllAction;
    
    private SoundManager sm;

    /**
     * When locked is true, this component should not allow the circuit to be
     * modified.
     */
    private boolean locked;
    
    private MouseInput mouseListener;
    
	private static final int DEFAULT_GATE_WIDTH = 85;

	private static final int DEFAULT_GATE_HEIGHT = 50;
	
	private static final int INPUT_STICK_LENGTH = 20;
	
	private static final int OUTPUT_STICK_LENGTH = 20;

	
	public CircuitEditor(Gate[] outputs, Gate inputs, SoundManager sm) {
	    setupKeyActions();
	    setPreferredSize(new Dimension(400, 400));
	    this.inputsGate = inputs;
	    this.outputs = new ArrayList<Gate>();
	    for (Gate g : outputs) this.outputs.add(g);
	    permanentGates.addAll(this.outputs);
	    permanentGates.add(inputs);
	    this.sm = sm;
	    mouseListener = new MouseInput();
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        setLocked(false);
        setLayout(new CircuitEditorLayout());
	}

	private void setupKeyActions() {
	    getInputMap().put(KeyStroke.getKeyStroke('a'), "addGate(AND)");
	    getInputMap().put(KeyStroke.getKeyStroke('o'), "addGate(OR)");
        getInputMap().put(KeyStroke.getKeyStroke('n'), "addGate(NOT)");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "remove");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "remove");
        getInputMap().put(KeyStroke.getKeyStroke('!'), "removeAll");
        
	    addAndGateAction = new AddGateAction(AndGate.class, "New AND Gate", "create-AND");
	    addOrGateAction = new AddGateAction(OrGate.class, "New OR Gate", "create-OR");
	    addNotGateAction = new AddGateAction(NotGate.class, "New NOT Gate", "create-NOT");
        removeAction = new RemoveAction("Remove");
        removeAllAction = new RemoveAllAction();
	    
	    getActionMap().put("addGate(AND)", addAndGateAction);
	    getActionMap().put("addGate(OR)", addOrGateAction);
	    getActionMap().put("addGate(NOT)", addNotGateAction);
        getActionMap().put("remove", removeAction);
        getActionMap().put("removeAll", removeAllAction);
	}

	private void paintGate(Graphics2D g2, Gate gate, Rectangle r) {
		g2.translate(r.x, r.y);

		// for debugging the draw routine
		//g2.drawRect(0, 0, r.width, r.height);
		
		// draw the inputs along the left edge of this gate
		Gate.Input[] inputs = gate.getInputs();
		Point inputLoc = new Point(INPUT_STICK_LENGTH, 0);
		for (int i = 0; inputs != null && i < inputs.length; i++) {
            if (inputs[i] == hilightInput) {
                g2.setColor(getHilightColor());
            } else if (inputs[i].getState() == true) {
                g2.setColor(getActiveColor());
            } else {
                g2.setColor(getForeground());
            }
            
            inputLoc.y = (int) ((0.5 + i) * (double) r.height / (double) inputs.length);
            paintInput(g2, inputLoc, inputs[i]);
		}

		// and the output
		paintOutput(g2, new Point(r.width - OUTPUT_STICK_LENGTH, r.height/2), gate.getLabel(),
                gate == hilightOutput, gate.getOutputState());
		
        if (gate == hilightGate) {
            g2.setColor(getHilightColor());
        } else if (gate.getOutputState() == true) {
            g2.setColor(activeColor);
        } else {
            g2.setColor(getForeground());
        }
		// individual gate bodies (XXX: should probably farm this out to the gates themselves)
		if (gate instanceof OrGate) {
		    int backX = OUTPUT_STICK_LENGTH;
		    double backDepth = r.height/6.0;
		    int pointyX = r.width-OUTPUT_STICK_LENGTH;
		    
		    // The back part
		    g2.draw(new QuadCurve2D.Double(backX, 0, backX + backDepth, r.height/2, backX, r.height));
		    
		    // Top curve
		    g2.draw(new QuadCurve2D.Double(backX, 0, backX + pointyX/2, 0, pointyX, r.height/2));
		    
		    // Bottom curve
		    g2.draw(new QuadCurve2D.Double(backX, r.height, backX + pointyX/2, r.height, pointyX, r.height/2));

		} else if (gate instanceof AndGate) {
		    int backX = OUTPUT_STICK_LENGTH;
		    int arcRadius = r.height/2;
		    int straightLength = r.width - arcRadius - INPUT_STICK_LENGTH - OUTPUT_STICK_LENGTH;
		    if (straightLength < 0) straightLength = 0;
		    g2.drawLine(backX, 0, backX, r.height);
		    g2.drawLine(backX, 0, backX+straightLength, 0);
		    g2.drawLine(backX, r.height, backX+straightLength, r.height);
		    g2.drawArc(backX + straightLength - arcRadius, 0, arcRadius*2, r.height, 270, 90);
		    g2.drawArc(backX + straightLength - arcRadius, 0, arcRadius*2, r.height, 0, 90);
		} else if (gate instanceof NotGate) {
		    int backX = OUTPUT_STICK_LENGTH;
		    int circleSize = 6;
		    g2.drawLine(backX, 0, backX, r.height);
		    g2.drawLine(backX, 0, r.width-OUTPUT_STICK_LENGTH-circleSize, r.height/2);
		    g2.drawLine(backX, r.height, r.width-OUTPUT_STICK_LENGTH-circleSize, r.height/2);
		    g2.drawOval(r.width-OUTPUT_STICK_LENGTH-circleSize, r.height/2 - circleSize/2, circleSize, circleSize);
		} else if (gate instanceof Robot.RobotSensorOutput) {
		    // nothing to draw: this should be squished against the left side of the editor
		} else if (gate instanceof Robot.RobotInputsGate) {
		    // Again, there's no visible gate body here
		} else {
			g2.drawOval(0, 0, r.width, r.height);
			g2.drawString(gate.getClass().getName(), 5, r.height/2);
		}
		g2.translate(-r.x, -r.y);
		
		// paint the connecting lines
		for (int i = 0; inputs != null && i < inputs.length; i++) {
			if (inputs[i].getState() == true) {
				g2.setColor(getActiveColor());
			} else {
				g2.setColor(getForeground());
			}

		    if (inputs[i].getConnectedGate() != null) {
		        Rectangle ir = gatePositions.get(inputs[i].getConnectedGate());
		        if (ir != null) {
                    if (inputs[i].getState() == true) {
                        g2.setColor(getActiveColor());
                    } else if (inputs[i] == hilightWireInput) {
                        g2.setColor(getHilightColor());
                    } else {
                        g2.setColor(getForeground());
                    }
                    inputLoc = getInputLocation(inputs[i]);
                    g2.drawLine(inputLoc.x, inputLoc.y, ir.x + ir.width, ir.y + (ir.height / 2));
		        }
		    }
		}
	}
	
	private void paintOutput(Graphics2D g2, Point p, String label,
            boolean highlight, boolean active) {
	    int length = OUTPUT_STICK_LENGTH;
        if (highlight) {
            g2.setColor(getHilightColor());
        } else if (active) {
	        g2.setColor(getActiveColor());
	    } else {
	        g2.setColor(getForeground());
	    }
	    g2.drawLine(p.x, p.y, p.x + length, p.y);
	    g2.drawLine(p.x + length, p.y, p.x + (int) (length*0.75), p.y - (int) (length*0.25));
	    g2.drawLine(p.x + length, p.y, p.x + (int) (length*0.75), p.y + (int) (length*0.25));
	    if (label != null) {
	        g2.setFont(getLabelFont(g2));
	        g2.drawString(label, p.x, p.y + 15);
	    }
	}

	private void paintInput(Graphics2D g2, Point p, Gate.Input input) {
	    int bubbleDiameter = 10;
		g2.drawOval(p.x - INPUT_STICK_LENGTH, p.y - (bubbleDiameter / 2), bubbleDiameter, bubbleDiameter);
		g2.drawLine(p.x, p.y, p.x - INPUT_STICK_LENGTH + bubbleDiameter, p.y);
		if (input.getLabel() != null) {
		    g2.setFont(getLabelFont(g2));
		    int labelLength = g2.getFontMetrics().stringWidth(input.getLabel());
		    int ascent = g2.getFontMetrics().getAscent();
		    g2.drawString(input.getLabel(), p.x - labelLength, p.y + ascent + bubbleDiameter);
		}
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setColor(getForeground());
		
		for (Map.Entry<Gate,Rectangle> ent : gatePositions.entrySet()) {
			Gate gate = ent.getKey();
			Rectangle gr = ent.getValue();
			paintGate(g2, gate, gr);
		}
        
        if (pendingConnectionLine != null) {
            if (pendingConnectionLine.isConnectionPossible()) {
                g2.setColor(Color.RED);
            } else {
                g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
                                             BasicStroke.JOIN_MITER, 1.0f,
                                             new float[] {5.0f, 5.0f}, 0f));
            }
            Point p1 = pendingConnectionLine.getCursorEnd();
            Point p2 = pendingConnectionLine.getFixedEnd();
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
        
        if (locked) {
            final String message = "Locked";
            Font f = getFont().deriveFont(20f);
            FontMetrics fm = getFontMetrics(f);
            g2.setFont(f);
            g2.setColor(Color.RED);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            g2.drawString(message,
                    getWidth() / 2 - fm.stringWidth(message) / 2,
                    fm.getHeight() * 2);
        }
	}
	
    public void removeAllGates() {
        // disconnect everything (to be fancy, we could have only removed
        // things connected to the permanent gates, but that's more work)
        for (Gate g : gatePositions.keySet()) {
            for (Gate.Input inp : g.getInputs()) {
                inp.connect(null);
            }
        }

        gatePositions.keySet().retainAll(permanentGates);
        
        repaint();
    }
    
	public void addGate(Gate g, Point p) {
		gatePositions.put(g, new Rectangle(p.x, p.y, DEFAULT_GATE_WIDTH, DEFAULT_GATE_HEIGHT));
		repaint();
	}

	public Gate getGateAt(Point p) {
        for (Map.Entry<Gate,Rectangle> ent : gatePositions.entrySet()) {
			Rectangle r = ent.getValue();
			if (r.contains(p)) return (Gate) ent.getKey();
		}
		return null;
	}
	
    /**
     * Searches for a connecting wire that nearly intersects the given point.
     * The current match radius is 4 pixels.
     * 
     * @return The input that the intersecting wire is connected to, or null
     * if no connecting wire comes near p.
     */
    public Gate.Input getWireAt(Point p) {
        final int r = 4;  // the radius of matching
        for (Gate g : gatePositions.keySet()) {
            Gate.Input[] inputs = g.getInputs();
            for (int i = 0; i < inputs.length; i++) {
                Gate.Input inp = inputs[i];
                if (inp.getConnectedGate() != null) {
                    Point start = getInputLocation(inputs[i]);
                    Point end = getOutputLocation(inp.getConnectedGate());
                    Line2D wire = new Line2D.Float(start.x, start.y, end.x, end.y);
                    if (wire.intersects(p.x-r, p.y-r, r*2, r*2)) {
                        //((Graphics2D) getGraphics()).draw(wire);
                        return inp;
                    }
                }
            }
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
	    public static final int MODE_CONNECTING_FROM_INPUT = 2;
	    public static final int MODE_MOVING = 3;
	    public static final int MODE_CONNECTING_FROM_OUTPUT = 4;
	    
	    private int mode = MODE_IDLE;
	    private Point dragOffset;
	    
	    @Override
		public void mouseDragged(MouseEvent e) {
            if (locked) return;
		    Point p = e.getPoint();
		    if (mode == MODE_CONNECTING_FROM_INPUT) {
		        pendingConnectionLine = pendingConnectionLine.moveCursor(p, getGateAt(p) != null);
		        repaint();
		    } else if (mode == MODE_CONNECTING_FROM_OUTPUT) {
                Gate g = getGateAt(p);
		        pendingConnectionLine = pendingConnectionLine.moveCursor(p,
                        (g == null ? false : getGateInput(g, p.x, p.y) != null));
		        repaint();
		    } else if (mode == MODE_MOVING) {
		        Rectangle r = gatePositions.get(movingGate);
		        r.x = p.x - dragOffset.x;
		        r.y = p.y - dragOffset.y;
		        repaint();
		    }
		}

        @Override
		public void mouseMoved(MouseEvent e) {
            if (locked) return;
		    Point p = e.getPoint();
		    if (mode == MODE_IDLE) {
		        Gate g = getGateAt(p);
		        
		        if (hilightGate != null && g == null) {
		            playSound("leave_gate");
		        } else if (hilightGate == null && g != null) {
		            playSound("enter_gate");
		        }
		        
		        setHilightGate(g);
		        Gate.Input newHilightInput = null;
		        Gate newHilightOutput = null;
		        if (g != null) {
		            Gate.Input inp = getGateInput(g, p.x, p.y); 
		            if (inp != null) {
		                newHilightInput = inp;
		            } else if (isGateOutput(g, p.x, p.y)) {
		                newHilightOutput = g;
		            }
		        }
		        
		        // only call these once now that we know the ones we want
		        // (avoids extra repaints)
		        setHilightInput(newHilightInput);
		        setHilightOutput(newHilightOutput);
                
                setHilightWireInput(getWireAt(p));
		    }
		    newGatePosition = p;
		}

        @Override
        public void mousePressed(MouseEvent e) {
            if (locked) return;
            if (mode == MODE_IDLE) {
                Point p = e.getPoint();
                Gate g = getGateAt(p);
                if (e.isPopupTrigger()) {
                    showPopup(e.getPoint());
                } else {
                    if (g != null) {
                        Gate.Input inp = getGateInput(g, p.x, p.y); 
                        if (inp != null) {
                            mode = MODE_CONNECTING_FROM_INPUT;
                            pendingConnectionLine = new ConnectionLine(getInputLocation(inp), p, false);
                            connectionStartInput = inp;
                            playSound("start_drawing_wire");
                            loopSound("pull_wire");
                        } else if (isGateOutput(g, p.x, p.y)) {
                            mode = MODE_CONNECTING_FROM_OUTPUT;
                            pendingConnectionLine = new ConnectionLine(getOutputLocation(g), p, false);
                            connectionStartOutput = g;
                            playSound("start_drawing_wire");
                            loopSound("pull_wire");
                        } else {
                            mode = MODE_MOVING;
                            movingGate = g;
                            Rectangle r = gatePositions.get(g);
                            dragOffset = new Point(p.x - r.x, p.y - r.y);
                            loopSound("drag-"+getSoundName(movingGate));
                        }
                    }
                }
            }
		}

        @Override
        public void mouseReleased(MouseEvent e) {
            if (locked) return;
            if (e.isPopupTrigger()) {
                showPopup(e.getPoint());
            } else if (mode == MODE_CONNECTING_FROM_INPUT) {
                Gate g = getGateAt(e.getPoint());
                if (g != null) {
                    connectGates(g, connectionStartInput);
                    playSound("terminated_wire");
                } else {
                    playSound("unterminated_wire");
                }
                connectionStartInput = null;
                pendingConnectionLine = null;
                mode = MODE_IDLE;
                stopSound("pull_wire");
            } else if (mode == MODE_CONNECTING_FROM_OUTPUT) {
                Point p = e.getPoint();
                Gate g = getGateAt(p);
                if (g != null) {
                    Gate.Input input = getGateInput(g, p.x, p.y);
                    if (input != null) {
                        connectGates(connectionStartOutput, input);
                    }
                    playSound("terminated_wire");
                } else {
                    playSound("unterminated_wire");
                }
                connectionStartOutput = null;
                pendingConnectionLine = null;
                mode = MODE_IDLE;
                stopSound("pull_wire");
            } else if (mode == MODE_MOVING) {
                stopSound("drag-"+getSoundName(movingGate));
                movingGate = null;
                dragOffset = null;
                mode = MODE_IDLE;
            }
            repaint();
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e.getPoint());
            }
        }
        
        private void showPopup(Point p) {
            JPopupMenu menu = new JPopupMenu();
            menu.add(addAndGateAction);
            menu.add(addOrGateAction);
            menu.add(addNotGateAction);
            menu.add(removeAction);
            menu.add(removeAllAction);
            menu.show(CircuitEditor.this, p.x, p.y);
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
        if (ni == 0) return null;
	    Rectangle bb = gatePositions.get(g); // the bounding box for this gate
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
	    Rectangle r = gatePositions.get(enclosingGate);
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

    private boolean isGateOutput(Gate g, int x, int y) {
        Rectangle bb = gatePositions.get(g); // the bounding box for this gate
        x -= bb.x;
        y -= bb.y;
        if (x > bb.width || x < bb.width - OUTPUT_STICK_LENGTH) return false;
        else return true;
    }

    private Point getOutputLocation(Gate g) {
        Rectangle bb = gatePositions.get(g); // the bounding box for this gate
        return new Point(bb.x + bb.width, bb.y + (bb.height / 2));
    }
    
    /**
     * Evaluates each gate in the circuit one time, then schedules a repaint.
     */
    public void evaluateOnce() {
        for (Map.Entry<Gate,Rectangle> ent : gatePositions.entrySet()) {
			Gate gate = ent.getKey();
			gate.evaluateInput();
		}
        for (Map.Entry<Gate,Rectangle> ent : gatePositions.entrySet()) {
            Gate gate = ent.getKey();
            gate.latchOutput();
        }
		repaint();
    }

    public void resetState() {
        for (Map.Entry<Gate,Rectangle> ent : gatePositions.entrySet()) {
            Gate gate = ent.getKey();
            gate.reset();
        }
    }

    public Color getHilightColor() {
        return hilightColor;
    }

    public void setHilightColor(Color hilightColor) {
        this.hilightColor = hilightColor;
    }

    /**
     * Updates the currently-highlighted input, and issues a repaint request
     * if the given input differs from the current one.
     */
    public void setHilightInput(Gate.Input hilightInput) {
        if (this.hilightInput != hilightInput) {
            this.hilightInput = hilightInput;
            repaint();
        }
    }

    /**
     * Updates the currently-highlighted wire, and issues a repaint request
     * if the given wire differs from the current one.
     */
    public void setHilightWireInput(Gate.Input hilightWireInput) {
        if (this.hilightWireInput != hilightWireInput) {
            this.hilightWireInput = hilightWireInput;
            repaint();
        }
    }

    /**
     * Updates the currently-highlighted output, and issues a repaint request
     * if the given output differs from the current one.
     */
    public void setHilightOutput(Gate hilightOutput) {
        if (this.hilightOutput != hilightOutput) {
            this.hilightOutput = hilightOutput;
            repaint();
        }
    }

    /**
     * Returns an unmodifiable view of the gate position map.
     */
    public Map<Gate, Rectangle> getGatePositions() {
        return Collections.unmodifiableMap(gatePositions);
    }
    
    public void setLocked(boolean v) {
        locked = v;
        repaint();
    }
    /**
     * Returns the sound manager name for the given gate.  For example,
     * if it's an AND gate, return value is "AND"; for an OR gate, the
     * return value is "OR".
     * 
     * @return the sound name for the given gate.  Returns "MISC" for unknown
     * gates.
     */
    private String getSoundName(Gate g) {
        if (g instanceof AndGate) return "AND";
        else if (g instanceof OrGate) return "OR";
        else if (g instanceof NotGate) return "NOT";
        else return "MISC";
    }
    
    private void playSound(String name) {
        sm.play(name);
    }
    
    private void loopSound(String name) {
        sm.loop(name);
    }

    private void stopSound(String name) {
        sm.stop(name);
    }

}
