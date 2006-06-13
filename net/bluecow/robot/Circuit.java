/*
 * Created on May 21, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bluecow.robot.GameConfig.GateConfig;
import net.bluecow.robot.gate.Gate;
import net.bluecow.robot.gate.Gate.Input;

/**
 * The Circuit class represents a collection of logic gates that can
 * be connected together in different ways and evaluated one step at
 * a time with consistent, repeatable results.
 *
 * @author fuerth
 * @version $Id$
 */
public class Circuit {

    private static final int DEFAULT_GATE_WIDTH = 85;

    private static final int DEFAULT_GATE_HEIGHT = 50;

    static final int INPUT_STICK_LENGTH = 22;
    
    static final int OUTPUT_STICK_LENGTH = 20;

    private List<Gate> outputs;

    private Gate inputsGate;
    
    /**
     * Maps each type of gate to its GateConfig instance.
     */
    private Map<Class<Gate>, GateConfig> gateConfigs = new HashMap<Class<Gate>, GateConfig>();

    /**
     * Maps each type of gate to the number of instances of it that are 
     * allowed to be added to this circuit.  For example, if the gateAllowances
     * map contains the mapping <tt>{ AndGate -&gt; 2 }</tt> and you add an
     * AND gate to the circuit, this map will be updated to <tt>{ AndGate -&gt; 1 }</tt>.
     * If you then remove an AND gate from the circuit, the map will go back to
     * <tt>{ AndGate -&gt; 2 }</tt>.
     */
    private Map<Class<? extends Gate>, Integer> gateAllowances = new HashMap<Class<? extends Gate>, Integer>();

    /**
     * Maps Gate instances to Rectangles that say where and how big to paint them.
     */
    private Map<Gate,Rectangle> gatePositions = new HashMap<Gate,Rectangle>();
    
    /**
     * The gates in this set should not be deletable by the user.
     */
    private Set<Gate> permanentGates = new HashSet<Gate>();

    /**
     * List of listeners to be notified when this circuit's state has changed.
     * Changes include gates being added, removed, or repositioned; an evaluation
     * happening (so some input and output states may have changed); or anything
     * else that might cause a visualization of the circuit to need to be redrawn.
     */
    private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

    public Circuit(Gate inputs, Collection<? extends Gate> outputs, Collection<GateConfig> gateConfigs) {
        inputsGate = inputs;
        permanentGates.add(inputsGate);
        gatePositions.put(inputsGate, new Rectangle(0, 0, DEFAULT_GATE_WIDTH, DEFAULT_GATE_HEIGHT));
        
        this.outputs = new ArrayList<Gate>(outputs);
        for (Gate output : outputs) {
            permanentGates.add(output);
            gatePositions.put(output, new Rectangle(0, 0, OUTPUT_STICK_LENGTH, DEFAULT_GATE_HEIGHT));
        }
        
        this.gateConfigs = new HashMap<Class<Gate>, GateConfig>();
        for (GateConfig gc : gateConfigs) {
            this.gateConfigs.put(gc.getGateClass(), gc);
        }
    }
    
    /**
     * Removes the given gate from this circuit, first disconnecting it from
     * all gates its inputs and outputs are connected to.
     * 
     * @param g The gate to remove.
     * @return true if the removal was successful; false otherwise.  Removal will fail
     * if the given gate is in the {@link #permanentGates} set, or if the given gate
     * is not part of this circuit.
     */
    public boolean remove(Gate g) {
        if (permanentGates.contains(g)) {
            return false;
        }

        // disconnect all the inputs the hilight gate outputs to
        for (Gate gg : gatePositions.keySet()) {
            for (int i = 0; i < gg.getInputs().length; i++) {
                if (gg.getInputs()[i].getConnectedGate() == g) {
                    gg.getInputs()[i].connect(null);
                }
            }
        }
        
        // disconnect the outputs from the doomed gate's inputs
        for (int i = 0; i < g.getInputs().length; i++) {
            g.getInputs()[i].connect(null);
        }
        
        boolean removed = gatePositions.remove(g) != null;
        
        if (removed) {
            Integer allowance = gateAllowances.get(g.getClass());
            if (allowance != null && allowance >= 0) {
                gateAllowances.put(g.getClass(), allowance + 1);
            }
            fireChangeEvent();
            return true;
        } else {
            return false;
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
        
        fireChangeEvent();
    }

    public void addGate(Gate g, Point p) {
        Integer numAllowed = gateAllowances.get(g.getClass()); 
        if (numAllowed == null || numAllowed == 0) {
            throw new IllegalArgumentException("No more gates of type "+g.getClass()+" are allowed");
        }
        
        // only decrease the allowance if it's nonnegative (negative allowance means unlimited)
        if (numAllowed > 0) {
            gateAllowances.put(g.getClass(), new Integer(numAllowed-1));
        }
        
        gatePositions.put(g, new Rectangle(p.x, p.y, DEFAULT_GATE_WIDTH, DEFAULT_GATE_HEIGHT));
        fireChangeEvent();
    }

    public Gate getGateAt(Point p) {
        for (Map.Entry<Gate,Rectangle> ent : gatePositions.entrySet()) {
            Rectangle r = ent.getValue();
            if (r.contains(p)) return (Gate) ent.getKey();
        }
        return null;
    }

    public List<Gate> getOutputs() {
        return outputs;
    }

    public Gate getInputsGate() {
        return inputsGate;
    }
    
    public Rectangle getGateBounds(Gate g) {
        Rectangle bounds = gatePositions.get(g);
        if (bounds == null) {
            return null;
        } else {
            return new Rectangle(bounds);
        }
    }
    
    public void setGateBounds(Gate g, Rectangle bounds) {
        if (!gatePositions.containsKey(g)) {
            throw new IllegalArgumentException("That gate is not in this circuit!");
        }
        gatePositions.put(g, new Rectangle(bounds));
        fireChangeEvent();
    }

    /**
     * Returns an unmodifiable view of the gate allowances map.
     */
    public Map<Class<? extends Gate>, Integer> getGateAllowances() {
        return Collections.unmodifiableMap(gateAllowances);
    }

    public void addGateAllowance(Class<Gate> gateClass, int count) {
        System.out.println("Setting gate allowance: "+gateClass.getName()+" = "+count);
        gateAllowances.put(gateClass, count);
        fireChangeEvent();
    }

    /**
     * Returns an unmodifiable view of the gate configs map.
     */
    public Map<Class<Gate>, GateConfig> getGateConfigs() {
        return Collections.unmodifiableMap(gateConfigs);
    }

    /**
     * Returns an unmodifiable view of the gate positions map.
     */
    public Map<Gate, Rectangle> getGatePositions() {
        return Collections.unmodifiableMap(gatePositions);
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
    Input getGateInput(Gate g, int x, int y) {
        final int ni = g.getInputs().length;  // number of inputs on this gate
        if (ni == 0) return null;
        Rectangle bb = gatePositions.get(g); // the bounding box for this gate
        x -= bb.x;
        y -= bb.y;
        if (x < 0 || x > INPUT_STICK_LENGTH) return null;
        else return g.getInputs()[y / (bb.height / ni)];
    }
    
    Point getInputLocation(Gate.Input inp) {
        Gate enclosingGate = inp.getGate();
        if (enclosingGate == null) {
            // this doesn't happen under normal circumstances
            System.err.println("Warning: getInputLocation was invoked for an input not attached to a gate");
            return new Point(0, 0);
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

    boolean isGateOutput(Gate g, int x, int y) {
        Rectangle bb = gatePositions.get(g); // the bounding box for this gate
        x -= bb.x;
        y -= bb.y;
        if (x > bb.width || x < bb.width - OUTPUT_STICK_LENGTH) return false;
        else return true;
    }

    Point getOutputLocation(Gate g) {
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
        
        fireChangeEvent();
    }

    public void resetState() {
        for (Map.Entry<Gate,Rectangle> ent : gatePositions.entrySet()) {
            Gate gate = ent.getKey();
            gate.reset();
        }
        
        fireChangeEvent();
    }

    /**
     * Searches for a connecting wire that nearly intersects the given point.
     * The current match radius is 4 units.
     * 
     * @return The input that the intersecting wire is connected to, or null
     * if no connecting wire comes near p.
     */
    public Gate.Input getWireAt(Point p) {
        final int r = 4;  // the radius of matching
        for (Gate g : getGatePositions().keySet()) {
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

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }
    
    private void fireChangeEvent() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : changeListeners) {
            l.stateChanged(e);  // XXX: this will not support removing a listener while the event is being fired
        }
    }
}
