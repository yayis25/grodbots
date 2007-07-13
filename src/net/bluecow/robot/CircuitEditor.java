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
package net.bluecow.robot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
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
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import net.bluecow.robot.GameConfig.GateConfig;
import net.bluecow.robot.gate.Gate;

public class CircuitEditor extends JPanel {

    private class ZoomEffect implements ActionListener {
        
        /**
         * The timer that periodically triggers rendering of the next frame.
         */
        private final Timer timer;
        
        /**
         * Goes from 0.0 to 1.0 in increments of frameStep.
         */
        private double progress = 0.0;
        
        /**
         * The increments in which this effect progresses from 0.0 (the starting
         * point) to 1.0 (the ending point).
         */
        private Double frameStep;
        
        private Rectangle start;
        
        private Rectangle end;
        
        /**
         * The gate that's zooming.
         */
        private Gate gate;

        private int startInputStickLength;

        private int startOutputStickLength;

        private int endInputStickLength;

        private int endOutputStickLength;
        
        ZoomEffect(int nframes, Gate gate, Rectangle end,
                int endInputStickLength, int endOutputStickLength) {
            frameStep = 1.0/nframes;
            this.gate = gate;
            this.start = gate.getBounds();
            this.end = new Rectangle(end);
            this.startInputStickLength = gate.getInputStickLength();
            this.startOutputStickLength = gate.getOutputStickLength();
            this.endInputStickLength = endInputStickLength;
            this.endOutputStickLength = endOutputStickLength;
            timer = new Timer(20, this);
            timer.start();
        }

        public void actionPerformed(ActionEvent e) {
            if (progress >= 1.0) {
                progress = 1.0;
            }
            
            //  8        e  e = (9,8)
            //  7       /
            //  6      /
            //  5     /
            //  4    r      r = (5,4)
            //  3   /                   2 + 3/7 * (9 - 2)  =  2 + 3/7 * 7  =  2 + 3  =  5
            //  2  /
            //  1 s         s = (2,1)
            //  0123456789
            Rectangle r = new Rectangle(
                    (int) (start.x + progress * (end.x - start.x)),
                    (int) (start.y + progress * (end.y - start.y)),
                    (int) (start.width + progress * (end.width - start.width)),
                    (int) (start.height + progress * (end.height - start.height))
                    );
            gate.setBounds(r);
            gate.setInputStickLength((int) (startInputStickLength + progress * (endInputStickLength - startInputStickLength)));
            gate.setOutputStickLength((int) (startOutputStickLength + progress * (endOutputStickLength - startOutputStickLength)));
            
            Graphics2D g2 = (Graphics2D) getGraphics();
            g2.translate(r.x, r.y);
            gate.drawBody(g2);
            g2.translate(-r.x, -r.y);
            
            if (progress == 1.0) {
                timer.stop();
                repaint();
            } else {
                progress += frameStep;
            }
        }
    }
    
    private class Toolbox {
        
        private List<Gate> gates;
        private Rectangle bounds;
        
        /**
         * The number of pixels tall this toolbox would like to paint the mini gates.
         */
        private int preferredGateHeight = 20;
        
        /**
         * The length of the input sticks and output sticks. Each stick will have
         * this length; it's not the total combined length.
         */
        private int miniStickLength = 8;

        /**
         * The number of pixels to leave at the bottom of the toolbox (below the bottom labels'
         * baseline).
         */
        private int bottomMargin = 3;

        /**
         * Sets up the toolbox list, which contains one gate instance for each type
         * of gate in the circuit's gate allowance set.
         */
        Toolbox() {
            gates = new ArrayList<Gate>(circuit.getGateAllowances().size());
            for (Map.Entry<Class<? extends Gate>, Integer> allowance : circuit.getGateAllowances().entrySet()) {
                try {
                    Gate miniGate = allowance.getKey().newInstance();
                    miniGate.setInputStickLength(miniStickLength);
                    miniGate.setOutputStickLength(miniStickLength);
                    miniGate.setCircleSize(4);
                    miniGate.setDrawingTerminations(false);
                    gates.add(miniGate);
                } catch (InstantiationException e) {
                    System.out.println("Couldn't create mini gate instance for "+allowance.getKey());
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    System.out.println("Couldn't create mini gate instance for "+allowance.getKey());
                    e.printStackTrace();
                }
            }
        }


        public List<Gate> getGates() {
            return gates;
        }
        
        public boolean contains(Point p) {
            boolean contains = bounds.contains(p);
            // System.out.println("Toolbox "+bounds+" contains "+p+"? "+contains);
            return contains;
        }

        /**
         * Sets up this toolbox to occupy the given rectangular region.  This
         * involves setting up the bounds of the minigates so they are evenly
         * distributed across the toolbox's width.
         */
        public void setBounds(Rectangle bounds) {
            this.bounds = new Rectangle(bounds);
            final FontMetrics fm = getFontMetrics(getFont());
            final int labelGap = fm.getHeight();
            final int miniWidth = preferredGateHeight + miniStickLength * 2;
            
            int gateNum = 0;
            for (Gate miniGate : gates) {
                int x = (int) ((double) getWidth() / (double) gates.size() * (gateNum + 0.5));
                miniGate.setBounds(new Rectangle(x - miniWidth/2, bounds.y + labelGap, miniWidth, bounds.height - labelGap*2 - bottomMargin));
                gateNum++;
            }
        }
        
        public Rectangle getBounds() {
            return new Rectangle(bounds);
        }


        /**
         * Returns the preferred size of this toolbox, given the gate height, current
         * font metrics of the circuit editor, and the number of gates in the toolbox.
         * 
         * <p>Note, the width component of the return value is garbage.  Currently,
         * the layout manager ignores the preferred width.  If you need a reasonable response
         * for preferred width, you'll have to tweak this method a bit.
         */
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(
                    preferredGateHeight * gates.size(),
                    fm.getHeight() + preferredGateHeight + fm.getHeight() + bottomMargin);
        }

        public void paint(Graphics2D g2) {
            // XXX: painting probably doesn't work properly unless the toolbox is at 0,0.
            
            g2.setColor(new Color(0.1f, 0.1f, 0.1f));
            Composite backupComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
            g2.setComposite(backupComposite);

            g2.setColor(getForeground());
            //g2.draw(bounds);
            final FontMetrics fm = g2.getFontMetrics();
            for (Gate miniGate : getGates()) {
                Integer allowance = circuit.getGateAllowances().get(miniGate.getClass());
                Rectangle r = miniGate.getBounds();
                
                final String allowanceString = allowance.toString();
                final int allowanceStringWidth = fm.stringWidth(allowanceString);
                g2.drawString(allowanceString, r.x + r.width/2 - allowanceStringWidth/2, bounds.y + fm.getHeight()); // I think it should be bounds.y + fm.getAscent(), but that paints it too high up

                g2.translate(r.x, r.y);
                miniGate.drawBody(g2);
                miniGate.drawInputs(g2, null);
                miniGate.drawOutput(g2, false);
                g2.translate(-r.x, -r.y);
                
                final String labelString = miniGate.getType() == null ? "NULL" : miniGate.getType();
                final int labelStringWidth = fm.stringWidth(labelString);
                g2.drawString(labelString, r.x + r.width/2 - labelStringWidth/2, bounds.y + bounds.height - fm.getHeight() / 2);
                // bounding box for label text
                //g2.drawRect(r.x + r.width/2 - labelStringWidth/2, bounds.y + r.y + r.height, labelStringWidth, fm.getHeight());
            }
        }
        
        /**
         * Returns the class of the minigate in this toolbox whose bounding
         * box contains the point <tt>p</tt>.
         */
        public Class<? extends Gate> getGateAt(Point p) {
            for (Gate g : gates) {
                if (g.getBounds().contains(p)) return g.getClass();
            }
            return null;
        }

        /**
         * Returns the bounding rectangle for the minigate in the toolbox
         * of the given class.  If there is no gate of the given class in this
         * toolbox, returns null.
         */
        public Gate getGateForClass(Class<? extends Gate> gateClass) {
            for (Gate g : gates) {
                if (g.getClass() == gateClass) return g;
            }
            return null;
        }
    }
    
    /**
     * Removes the highlighted wire or gate.  If both a gate and a wire are
     * highlighted, removes only the wire.
     */
    private class RemoveAction extends AbstractAction {

        public RemoveAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            if (circuit.isLocked()) return;
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
                    Gate zoomTo = toolbox.getGateForClass(hilightGate.getClass());
                    new ZoomEffect(ZOOM_STEPS, hilightGate, zoomTo.getBounds(), zoomTo.getInputStickLength(), zoomTo.getOutputStickLength());
                    hilightGate = null;
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
            if (circuit.isLocked()) return;
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

        /**
         * Distributes the robot inputs and outputs evenly along the right- and
         * left-hand sides respectively, and puts the toolbox at the top.  The
         * toolbox height is set to its preferred height, but its width is always
         * pegged to the width of the container, minus a margin.
         */
        public void layoutContainer(Container parent) {
            CircuitEditor ce = (CircuitEditor) parent;
            Circuit circuit = ce.circuit;
            double height = ce.getHeight();
            double n = circuit.getOutputs().size();
            for (int i = 0; i < circuit.getOutputs().size(); i++) {
                Gate g = circuit.getOutputs().get(i);
                Rectangle bounds = g.getBounds();
                int y = (int) ( ((double) i) * (height / n) + (height / n / 2.0) - bounds.height/2.0 );
                g.setBounds(new Rectangle(0, y, bounds.width, bounds.height));
            }
            Gate ig = circuit.getInputsGate(); 
            ig.setBounds(new Rectangle(
                            ce.getWidth() - ig.getInputStickLength(),
                            0,
                            ig.getInputStickLength() + ig.getOutputStickLength(),
                            ce.getHeight()));
            final int tbMargin = ig.getInputStickLength();
            final Dimension tbPrefSize = ce.toolbox.getPreferredSize();
            ce.toolbox.setBounds(new Rectangle(tbMargin, 0, ce.getWidth() - tbMargin*2, tbPrefSize.height));
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
            if (circuit.isLocked()) return;
            if (circuit.getGateAllowances().get(gc.getGateClass()) == 0) {
                System.out.println("Not adding "+gc.getGateClass()+" because no more are allowed");
                // TODO: play buzzer sound
                return;
            }
            try {
                Gate newGate = gc.getGateClass().newInstance();
                circuit.addGate(newGate, 
                        new Rectangle(
                                newGatePosition.x,
                                newGatePosition.y,
                                (int) (defaultGateSize.width * gateSizeCoefficient),
                                (int) (defaultGateSize.height * gateSizeCoefficient)));
                playSound("create-"+gc.getName());
                
                Gate zoomFrom = toolbox.getGateForClass(gc.getGateClass());
                int nisl = (int) (newGate.getInputStickLength() * gateSizeCoefficient);
                int nosl = (int) (newGate.getOutputStickLength() * gateSizeCoefficient);
                Rectangle zoomTo = newGate.getBounds();
                newGate.setBounds(zoomFrom.getBounds());
                newGate.setInputStickLength(zoomFrom.getInputStickLength());
                newGate.setOutputStickLength(zoomFrom.getOutputStickLength());
                new ZoomEffect(ZOOM_STEPS, newGate, zoomTo, nisl, nosl);
            } catch (InstantiationException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(CircuitEditor.this, "Couldn't create new Gate instance:\n"+e1.getMessage());
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(CircuitEditor.this, "Couldn't access new Gate instance:\n"+e1.getMessage());
            }
        }
	}
    
    private class SetGateSizeAction extends AbstractAction {
        
        private static final double MIN_SCALE = 0.2;
        private double scaleFactor;
        
        SetGateSizeAction(String name, double scaleFactor) {
            super(name);
            this.scaleFactor = scaleFactor;
        }
        
        public void actionPerformed(ActionEvent e) {
            gateSizeCoefficient += scaleFactor;
            if (gateSizeCoefficient < MIN_SCALE) gateSizeCoefficient = MIN_SCALE;
            Set<Gate> nonScaledGates = new HashSet<Gate>();
            nonScaledGates.add(circuit.getInputsGate());
            nonScaledGates.addAll(circuit.getOutputs());
            for (Gate g : circuit.getGates()) {
                if (nonScaledGates.contains(g)) continue;
                Rectangle oldbounds = g.getBounds();
                g.setBounds(new Rectangle(
                        oldbounds.x, oldbounds.y,
                        (int) (defaultGateSize.width * gateSizeCoefficient),
                        (int) (defaultGateSize.height * gateSizeCoefficient)));
                g.setOutputStickLength((int) (defaultOutputStickLength * gateSizeCoefficient));
                g.setInputStickLength((int) (defaultInputStickLength * gateSizeCoefficient));
            }
            repaint();
        }
    }

    /**
     * The number of steps in a normal zoom effect.
     */
    private static final int ZOOM_STEPS = 10;
    
    private static final Dimension defaultGateSize = new Dimension(85, 50);
    
    private static final int defaultInputStickLength = 22;
    
    private static final int defaultOutputStickLength = 20;
    
    private double gateSizeCoefficient = 1.0;

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
     * Action that removes the highlighted gate.  See {@link RemoveAction}.
     */
    private RemoveAction removeAction;
    
    /**
     * Action that removes all gates.  See {@link RemoveAllAction}.
     */
    private RemoveAllAction removeAllAction;
    
    /**
     * The sound manager to use for playing UI sound effects.
     */
    private SoundManager sm;
    
    /**
     * Handles all the mouse activity on this component.
     */
    private MouseInput mouseListener;

    /**
     * The circuit we're editing.
     */
    private Circuit circuit;

    private SetGateSizeAction shrinkAction;

    private SetGateSizeAction growAction;
    
    /**
     * Creates a new circuit editor for the given circuit.
     * 
     * @param circuit The circuit to edit.
     * @param sm The sound manager to use for playing sound effects.
     */
	public CircuitEditor(Circuit circuit, SoundManager sm) {
	    this.circuit = circuit;
	    this.sm = sm;
	    setPreferredSize(new Dimension(400, 400));
	    mouseListener = new MouseInput();
	    setupActions();
        toolbox = new Toolbox();
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
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
        for (GateConfig gc : circuit.getGateConfigs().values()) {
            String actionKey = "addGate("+gc.getName()+")";
            getInputMap().put(gc.getAccelerator(), actionKey);
            Action addGateAction = new AddGateAction(gc);
            getActionMap().put(actionKey, addGateAction);
        }

        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "remove");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "remove");
        removeAction = new RemoveAction("Remove");
        getActionMap().put("remove", removeAction);

        getInputMap().put(KeyStroke.getKeyStroke('!'), "removeAll");
        removeAllAction = new RemoveAllAction();
        getActionMap().put("removeAll", removeAllAction);
        
        getInputMap().put(KeyStroke.getKeyStroke('-'), "shrinkGates");
        shrinkAction = new SetGateSizeAction("Ensmallen Gates", -0.1);
        getActionMap().put("shrinkGates", shrinkAction);
        
        getInputMap().put(KeyStroke.getKeyStroke('+'), "growGates");
        getInputMap().put(KeyStroke.getKeyStroke('='), "growGates");
        growAction = new SetGateSizeAction("Embiggen Gates", 0.1);
        getActionMap().put("growGates", growAction);
    }

    /**
     * Paints the given gate using the given Graphics.
     */
	private void paintGate(Graphics2D g2, Gate gate) {
        Rectangle r = gate.getBounds();
		g2.translate(r.x, r.y);
        
		// for debugging the draw routines
		//g2.drawRect(0, 0, r.width, r.height);
		
		gate.drawInputs(g2, hilightInput);
        
		// and the output
		gate.drawOutput(g2, gate == hilightOutput);
		
        if (gate == hilightGate) {
            g2.setColor(getHilightColor());
        } else if (gate.getOutputState() == true) {
            g2.setColor(activeColor);
        } else {
            g2.setColor(getForeground());
        }

        // individual gate bodies
        gate.drawBody(g2);
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
		        Rectangle ir = inputs[i].getConnectedGate().getBounds();
		        if (ir != null) {
                    if (inputs[i].getState() == true) {
                        g2.setColor(getActiveColor());
                    } else if (inputs[i] == hilightWireInput) {
                        g2.setColor(getHilightColor());
                    } else {
                        g2.setColor(getForeground());
                    }
                    Point inputLoc = inputs[i].getPosition();
                    g2.drawLine(inputLoc.x, inputLoc.y, ir.x + ir.width, ir.y + (ir.height / 2));
		        }
		    }
		}
	}

    @Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setColor(getForeground());
		
		for (Gate gate : circuit.getGates()) {
			paintGate(g2, gate);
		}
        
        Stroke backupStroke = g2.getStroke();
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
        g2.setStroke(backupStroke);
        
        toolbox.paint(g2);
        
        if (circuit.isLocked()) {
            final String message = "Locked";
            Font f = getFont().deriveFont(20f);
            FontMetrics fm2 = getFontMetrics(f);
            g2.setFont(f);
            g2.setColor(Color.RED);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            g2.drawString(message,
                    getWidth() / 2 - fm2.stringWidth(message) / 2,
                    fm2.getHeight() * 2);
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
	
    private Toolbox toolbox;
    
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
            if (circuit.isLocked()) return;
		    Point p = e.getPoint();
		    if (mode == MODE_CONNECTING_FROM_INPUT) {
		        pendingConnectionLine = pendingConnectionLine.moveCursor(p,
                        circuit.getGateAt(p) != null && circuit.getGateAt(p).isOutputConnectable());
		        repaint();
		    } else if (mode == MODE_CONNECTING_FROM_OUTPUT) {
                Gate g = circuit.getGateAt(p);
		        pendingConnectionLine = pendingConnectionLine.moveCursor(p,
                        (g == null ? false : g.getInputAt(p.x, p.y) != null));
		        repaint();
		    } else if (mode == MODE_MOVING) {
		        Rectangle r = movingGate.getBounds();
		        r.x = p.x - dragOffset.x;
		        r.y = p.y - dragOffset.y;
                movingGate.setBounds(r);
		        repaint();
		    }
		}

        @Override
		public void mouseMoved(MouseEvent e) {
            if (circuit.isLocked()) return;
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
		            Gate.Input inp = g.getInputAt(p.x, p.y); 
		            if (inp != null) {
		                newHilightInput = inp;
		            } else if (g.isOutput(p.x, p.y)) {
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
            if (circuit.isLocked()) return;
            if (mode == MODE_IDLE) {
                Point p = e.getPoint();
                
                // either create a new gate from a toolbox click, or find the gate in the circuit under the pointer
                Class<? extends Gate> gclass = toolbox.getGateAt(p);
                Gate g;
                if (gclass != null) {
                    Integer allowance = circuit.getGateAllowances().get(gclass);
                    if (allowance != null && allowance != 0) {
                        try {
                            g = gclass.newInstance();
                            Rectangle bounds = 
                                new Rectangle(
                                        
                                        p.x - defaultGateSize.width/2,
                                        p.y - defaultGateSize.height/2,
                                        (int) (defaultGateSize.width + gateSizeCoefficient),
                                        (int) (defaultGateSize.height + gateSizeCoefficient));
                            circuit.addGate(g, bounds);
                            playSound("create-"+getSoundName(g));
                        } catch (Exception ex) {
                            g = null;
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(CircuitEditor.this, "Can't get gate from toolbox:\n"+ex.getMessage());
                        }
                    } else {
                        g = null;
                    }
                } else {
                    g = circuit.getGateAt(p);
                }
                
                if (e.isPopupTrigger()) {
                    showPopup(e.getPoint());
                } else if (g != null) {
                    Gate.Input inp = g.getInputAt(p.x, p.y); 
                    if (inp != null) {
                        mode = MODE_CONNECTING_FROM_INPUT;
                        pendingConnectionLine = new ConnectionLine(inp.getPosition(), p, false);
                        connectionStartInput = inp;
                        playSound("start_drawing_wire");
                        loopSound("pull_wire");
                    } else if (g.isOutput(p.x, p.y) && g.isOutputConnectable()) {
                        mode = MODE_CONNECTING_FROM_OUTPUT;
                        pendingConnectionLine = new ConnectionLine(g.getOutputPosition(), p, false);
                        connectionStartOutput = g;
                        playSound("start_drawing_wire");
                        loopSound("pull_wire");
                    } else {
                        mode = MODE_MOVING;
                        movingGate = g;
                        hilightGate = g;
                        Rectangle r = g.getBounds();
                        dragOffset = new Point(p.x - r.x, p.y - r.y);
                        loopSound("drag-"+getSoundName(movingGate));
                    }
                }
            }
		}

        @Override
        public void mouseReleased(MouseEvent e) {
            if (circuit.isLocked()) return;
            Point p = e.getPoint();
            if (e.isPopupTrigger()) {
                showPopup(e.getPoint());
            } else if (mode == MODE_CONNECTING_FROM_INPUT) {
                Gate g = circuit.getGateAt(p);
                if (g != null && g.isOutputConnectable()) {
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
                Gate g = circuit.getGateAt(p);
                if (g != null) {
                    Gate.Input input = g.getInputAt(p.x, p.y);
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
                if (toolbox.contains(p)) {
                    removeAction.actionPerformed(null);
                }
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
            menu.addSeparator();
            menu.add(removeAction);
            menu.add(removeAllAction);
            menu.addSeparator();
            menu.add(shrinkAction);
            menu.add(growAction);
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
        GateConfig gateConfig = circuit.getGateConfigs().get(g.getClass());
        return gateConfig == null ? null : gateConfig.getName();
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
