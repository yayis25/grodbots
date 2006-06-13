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
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import net.bluecow.robot.GameConfig.GateConfig;
import net.bluecow.robot.gate.Gate;

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
                boolean baleted = circuit.remove(hilightGate);
                repaint();
                if (baleted) {
                    playSound("delete_gate");
                } else {
                    // TODO: get a "you suck" type sound
                }
            }
        }

    }
    
    private class RemoveAllAction extends AbstractAction {
        public RemoveAllAction() {
            super("Clear");
        }
        
        public void actionPerformed(ActionEvent e) {
            if (locked) return;
            circuit.removeAllGates();
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
            Circuit circuit = ce.circuit;
            double height = ce.getHeight();
            double n = circuit.getOutputs().size();
            for (int i = 0; i < circuit.getOutputs().size(); i++) {
                Gate g = circuit.getOutputs().get(i);
                Rectangle bounds = circuit.getGateBounds(g);
                System.out.println("Bounds of gate "+g+": "+bounds);
                int y = (int) ( ((double) i) * (height / n) + (height / n / 2.0) - bounds.height/2.0 );
                circuit.setGateBounds(g, new Rectangle(0, y, bounds.width, bounds.height));
            }
            circuit.setGateBounds(
                    circuit.getInputsGate(),
                    new Rectangle(
                            ce.getWidth() - Circuit.INPUT_STICK_LENGTH,
                            0,
                            Circuit.INPUT_STICK_LENGTH + Circuit.OUTPUT_STICK_LENGTH,
                            ce.getHeight()));
        }
    }

    /**
	 * The AddGateAction adds a new instance of a gate to the enclosing circuit editor. 
	 */
    public class AddGateAction extends AbstractAction implements Action, ChangeListener {
        
        private GateConfig gc;
        
        public AddGateAction(GateConfig gc) {
            super();
            this.gc = gc;
            updateName();
            circuit.addChangeListener(this);
        }
        
        /** Updates this action's name to include the circuit's current allowance amount. */
        private void updateName() {
            StringBuffer name = new StringBuffer();
            name.append(gc.getName());
            Integer allowance = circuit.getGateAllowances().get(gc.getGateClass());
            if (allowance != null && allowance >= 0) {
                name.append(" (").append(allowance).append(")");
            }
            putValue(NAME, name.toString());
            setEnabled(allowance != null && allowance != 0);
        }
        
        public void stateChanged(ChangeEvent e) {
            // the number of gates available might have changed, so...
            updateName();
        }

        public void actionPerformed(ActionEvent e) {
            if (locked) return;
            if (circuit.getGateAllowances().get(gc.getGateClass()) == 0) {
                System.out.println("Not adding "+gc.getGateClass()+" because no more are allowed");
                // TODO: play buzzer sound
                return;
            }
            try {
                Gate newGate = gc.getGateClass().newInstance();
                Point p = new Point(newGatePosition);
                circuit.addGate(newGate, p);
                playSound("create-"+gc.getName());
            } catch (InstantiationException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(CircuitEditor.this, "Couldn't create new Gate instance:\n"+e1.getMessage());
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(CircuitEditor.this, "Couldn't access new Gate instance:\n"+e1.getMessage());
            }
        }
	}
    
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
	
    private RemoveAction removeAction;
    
    private RemoveAllAction removeAllAction;
    
    private SoundManager sm;
    
    /**
     * When locked is true, this component should not allow the circuit to be
     * modified.
     * <p>
     * XXX: move to circuit class? it could make sense both ways.
     */
    private boolean locked;
    
    private MouseInput mouseListener;

    /**
     * The circuit we're editing.
     */
    private Circuit circuit;
    
	public CircuitEditor(Circuit circuit, SoundManager sm) {
	    this.circuit = circuit;
	    this.sm = sm;
	    setPreferredSize(new Dimension(400, 400));
	    mouseListener = new MouseInput();
	    setupActions();
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        setLocked(false);
        setLayout(new CircuitEditorLayout());
        
        circuit.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) { repaint(); }
        });
	}

    /**
     * Sets up the ActionMap and InputMap, including key bindings to all the
     * gate creation actions.  There will be one create action for each type of
     * gate in the circuit's gate allowances set.
     * 
     * <p>Gate creation actions live in the inputMap with a name of 
     * <code>addGate(fully.qualified.gate.class.name)</code>.
     */
    private void setupActions() {
        System.out.println("Creating actions for making gates...");
        for (GateConfig gc : circuit.getGateConfigs().values()) {
            String actionKey = "addGate("+gc.getName()+")";
            getInputMap().put(gc.getAccelerator(), actionKey);
            Action addGateAction = new AddGateAction(gc);
            getActionMap().put(actionKey, addGateAction);
            System.out.println("  "+actionKey);
        }

        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "remove");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "remove");
        removeAction = new RemoveAction("Remove");
        getActionMap().put("remove", removeAction);

        getInputMap().put(KeyStroke.getKeyStroke('!'), "removeAll");
        removeAllAction = new RemoveAllAction();
        getActionMap().put("removeAll", removeAllAction);
    }

	private void paintGate(Graphics2D g2, Gate gate, Rectangle r) {
		g2.translate(r.x, r.y);
        
		// for debugging the draw routines
		//g2.drawRect(0, 0, r.width, r.height);
		
		gate.drawInputs(g2, r, Circuit.INPUT_STICK_LENGTH, Circuit.OUTPUT_STICK_LENGTH, hilightInput);
        
		// and the output
		gate.drawOutput(g2, r, gate == hilightOutput, Circuit.OUTPUT_STICK_LENGTH);
		
        if (gate == hilightGate) {
            g2.setColor(getHilightColor());
        } else if (gate.getOutputState() == true) {
            g2.setColor(activeColor);
        } else {
            g2.setColor(getForeground());
        }

        // individual gate bodies
        gate.drawBody(g2, r, Circuit.INPUT_STICK_LENGTH, Circuit.OUTPUT_STICK_LENGTH);
        g2.translate(-r.x, -r.y);
		
        // paint the connecting lines
        Gate.Input[] inputs = gate.getInputs();
		for (int i = 0; inputs != null && i < inputs.length; i++) {
			if (inputs[i].getState() == true) {
				g2.setColor(getActiveColor());
			} else {
				g2.setColor(getForeground());
			}

		    if (inputs[i].getConnectedGate() != null) {
		        Rectangle ir = circuit.getGateBounds(inputs[i].getConnectedGate());
		        if (ir != null) {
                    if (inputs[i].getState() == true) {
                        g2.setColor(getActiveColor());
                    } else if (inputs[i] == hilightWireInput) {
                        g2.setColor(getHilightColor());
                    } else {
                        g2.setColor(getForeground());
                    }
                    Point inputLoc = circuit.getInputLocation(inputs[i]);
                    g2.drawLine(inputLoc.x, inputLoc.y, ir.x + ir.width, ir.y + (ir.height / 2));
		        }
		    }
		}
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setColor(getForeground());
		
		for (Map.Entry<Gate,Rectangle> ent : circuit.getGatePositions().entrySet()) {
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
        
        // toolbox
        g2.setColor(getForeground());
        int numGatesInToolbox = circuit.getGateAllowances().size();
        int gateNum = 0;
        for (Map.Entry<Class<? extends Gate>, Integer> allowance : circuit.getGateAllowances().entrySet()) {
            int y = 20;
            int x = (int) ((double) getWidth() / (double) numGatesInToolbox * (double) gateNum);
            g2.drawString(allowance.getValue().toString(), x, y);
            try {
                // XXX: establish these things up front, not at every paint request
                Gate miniGate = allowance.getKey().newInstance();
                Rectangle miniBounds = new Rectangle(x, y, 40, 20);
                int miniStickLength = 15;
                //miniGate.setCircleSize(4);
                g2.translate(miniBounds.x, miniBounds.y);
                miniGate.drawBody(g2, miniBounds, miniStickLength, miniStickLength);
                miniGate.drawInputs(g2, miniBounds, miniStickLength, miniStickLength, null);
                miniGate.drawOutput(g2, miniBounds, false, miniStickLength);
                g2.translate(-miniBounds.x, -miniBounds.y);
            } catch (InstantiationException e) {
                System.out.println("Couldn't create mini gate instance for "+allowance.getKey());
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                System.out.println("Couldn't create mini gate instance for "+allowance.getKey());
                e.printStackTrace();
            }
            gateNum++;
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
		        pendingConnectionLine = pendingConnectionLine.moveCursor(p, circuit.getGateAt(p) != null);
		        repaint();
		    } else if (mode == MODE_CONNECTING_FROM_OUTPUT) {
                Gate g = circuit.getGateAt(p);
		        pendingConnectionLine = pendingConnectionLine.moveCursor(p,
                        (g == null ? false : circuit.getGateInput(g, p.x, p.y) != null));
		        repaint();
		    } else if (mode == MODE_MOVING) {
		        Rectangle r = circuit.getGateBounds(movingGate);
		        r.x = p.x - dragOffset.x;
		        r.y = p.y - dragOffset.y;
		        circuit.setGateBounds(movingGate, r);
		        repaint();
		    }
		}

        @Override
		public void mouseMoved(MouseEvent e) {
            if (locked) return;
		    Point p = e.getPoint();
		    if (mode == MODE_IDLE) {
		        Gate g = circuit.getGateAt(p);
		        
		        if (hilightGate != null && g == null) {
		            playSound("leave_gate");
		        } else if (hilightGate == null && g != null) {
		            playSound("enter_gate");
		        }
		        
		        setHilightGate(g);
		        Gate.Input newHilightInput = null;
		        Gate newHilightOutput = null;
		        if (g != null) {
		            Gate.Input inp = circuit.getGateInput(g, p.x, p.y); 
		            if (inp != null) {
		                newHilightInput = inp;
		            } else if (circuit.isGateOutput(g, p.x, p.y)) {
		                newHilightOutput = g;
		            }
		        }
		        
		        // only call these once now that we know the ones we want
		        // (avoids extra repaints)
		        setHilightInput(newHilightInput);
		        setHilightOutput(newHilightOutput);
                
                setHilightWireInput(circuit.getWireAt(p));
		    }
		    newGatePosition = p;
		}

        @Override
        public void mousePressed(MouseEvent e) {
            if (locked) return;
            if (mode == MODE_IDLE) {
                Point p = e.getPoint();
                Gate g = circuit.getGateAt(p);
                if (e.isPopupTrigger()) {
                    showPopup(e.getPoint());
                } else {
                    if (g != null) {
                        Gate.Input inp = circuit.getGateInput(g, p.x, p.y); 
                        if (inp != null) {
                            mode = MODE_CONNECTING_FROM_INPUT;
                            pendingConnectionLine = new ConnectionLine(circuit.getInputLocation(inp), p, false);
                            connectionStartInput = inp;
                            playSound("start_drawing_wire");
                            loopSound("pull_wire");
                        } else if (circuit.isGateOutput(g, p.x, p.y)) {
                            mode = MODE_CONNECTING_FROM_OUTPUT;
                            pendingConnectionLine = new ConnectionLine(circuit.getOutputLocation(g), p, false);
                            connectionStartOutput = g;
                            playSound("start_drawing_wire");
                            loopSound("pull_wire");
                        } else {
                            mode = MODE_MOVING;
                            movingGate = g;
                            Rectangle r = circuit.getGateBounds(g);
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
                Gate g = circuit.getGateAt(e.getPoint());
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
                Gate g = circuit.getGateAt(p);
                if (g != null) {
                    Gate.Input input = circuit.getGateInput(g, p.x, p.y);
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
            Map<Class<? extends Gate>, Integer> allowances = circuit.getGateAllowances();
            for (GateConfig gc : circuit.getGateConfigs().values()) {
                if (allowances.get(gc.getGateClass()) != null) {
                    menu.add(getActionMap().get("addGate("+gc.getName()+")"));
                }
            }
            menu.add(removeAction);
            menu.add(removeAllAction);
            menu.show(CircuitEditor.this, p.x, p.y);
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

    public void setLocked(boolean v) {
        locked = v;
        repaint();
    }
    
    /**
     * Returns the circuit that this editor is editing.
     */
    public Circuit getCircuit() {
        return circuit;
    }
    
    /**
     * Returns the sound manager name for the given gate by looking it up in
     * that gate's GateConfig.
     */
    private String getSoundName(Gate g) {
        return circuit.getGateConfigs().get(g.getClass()).getName();
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