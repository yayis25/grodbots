package net.bluecow.robot.gate;

import javax.swing.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

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
	 * These are the inputs to this gate. Subclass constructors should
	 * initialise this array to the correct length and types.
	 */
	protected Gate.Input inputs[];

	/**
	 * This is the listener that receives the change events from all the inputs.
	 */
	private ChangeListener inputChangeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			// FIXME: This will cause a big event storm. We
			// probably need to remember what the previous output
			// state was and only fire a change if the output
			// truly changed.
			fireChangeEvent();
		}
	};

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

		/**
		 * This is the gate we monitor for changes, and whose output state we
		 * report as our input state.
		 */
		private Gate inputGate;

		/**
		 * Connects this input to the output of the given gate. If this input
		 * was already connected, throws an exception.
		 * 
		 * @throws IllegalStateException
		 *             if already connected.
		 */
		public void connect(Gate g) {
			if (inputGate != null) {
				throw new IllegalStateException("This input is already connected!");
			}
			inputGate = g;
			if (g != null) {
			    inputGate.addChangeListener(inputChangeListener);
			}
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
	}

	/**
	 * This is the method that subclasses will implement to evaluate their
	 * inputs.
	 */
	public abstract boolean getOutputState();

	/**
	 * Returns the list of inputs.
	 */
	public Gate.Input[] getInputs() {
		return inputs;
	}

	// ------------- Change Event Support ----------------

	/**
	 * The list of change listeners who want to know when this gate's inputs
	 * have changed.
	 */
	private List changeListeners = new ArrayList();

	/**
	 * Adds a listener who will be notified with a change event every time an
	 * input changes state.
	 */
	public void addChangeListener(ChangeListener l) {
		changeListeners.add(l);
	}

	/**
	 * Removes change listener from the list. If the given change listener
	 * wasn't on the list in the first place, this method does nothing.
	 */
	public void removeChangeListener(ChangeListener l) {
		changeListeners.remove(l);
	}

	/**
	 * Notifies all change listeners.
	 */
	public void fireChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);
		Iterator it = changeListeners.iterator();
		while (it.hasNext()) {
			((ChangeListener) it.next()).stateChanged(e);
		}
	}
	
	// -------------- ACCESSORS and MUTATORS ------------------
	
	public String getLabel() {
		return label;
	}
}
