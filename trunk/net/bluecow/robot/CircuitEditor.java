package net.bluecow.robot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import net.bluecow.robot.gate.Gate;
import net.bluecow.robot.gate.Gate.Input;

public class CircuitEditor extends JPanel {

	private Gate[] outputs;

	private Gate.Input[] inputs;

	/**
	 * Maps Gate instances to Rectangles that say where and how big to paint them.
	 */
	private Map gatePositions = new HashMap();
	
	/**
	 * The gate that should be highlighted.  This is the gate that currently has keyboard and mouse focus.
	 */
	private Gate hilightGate;

	/**
	 * The Gate where the current drag operation started.  This is set to null when there is not a drag operation in progress.
	 */
	private Gate dragStartGate;
	
	/**
	 * The colour to make a gate or connection which is active (in the TRUE/ON state). 
	 */
	private Color activeColor = Color.orange;
	
	/**
	 * The colour to make the highlight gate.
	 */
	private Color hilightColor = Color.BLUE;
	
	
	public CircuitEditor(Gate[] outputs, Gate.Input[] inputs) {
		this.outputs = outputs;
		this.inputs = inputs;
	}

	private void paintGate(Graphics2D g2, Gate gate) {
		Point p = (Point) gatePositions.get(gate);
		g2.drawOval(p.x-10,p.y-10,20,20);
	}
	private void paintOutput(Graphics2D g2, Point p) {
		final int length = 15;
		g2.drawLine(p.x, p.y, p.x + length, p.y);
		g2.drawOval(p.x + length, p.y - 5, 10, 10);
	}

	private void paintInput(Graphics2D g2, Point p) {
		final int length = 15;
		g2.drawOval(p.x - length - 10, p.y - 5, 10, 10);
		g2.drawLine(p.x, p.y, p.x - length, p.y);
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());

		// draw the outputs along the left edge
		for (int i = 0; i < outputs.length; i++) {
			if (outputs[i].getOutputState() == true) {
				g2.setColor(getActiveColor());
			} else {
				g2.setColor(getForeground());
			}

			paintOutput(g2, new Point(0, (int) ((0.5 + i)
					* (double) getHeight() / (double) outputs.length)));
		}

		// draw the inputs along the right edge
		for (int i = 0; i < inputs.length; i++) {
			if (inputs[i].getState() == true) {
				g2.setColor(getActiveColor());
			} else {
				g2.setColor(getForeground());
			}

			paintInput(g2, new Point(getWidth(), (int) ((0.5 + i)
					* (double) getHeight() / (double) inputs.length)));
		}
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
		target.connect(source);
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
	
	private class MouseInput extends MouseInputAdapter {
		public void mouseDragged(MouseEvent e) {
			if (dragStartGate != null) {
				repaint();
			}
		}

		public void mouseMoved(MouseEvent e) {
			Gate gate = getGateAt(e.getPoint());
			if (gate != null) {
				hilightGate = gate;
			}
		}

		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			super.mousePressed(e);
		}

		public void mouseReleased(MouseEvent e) {
			if (dragStartGate != null) {
				Gate g = getGateAt(e.getPoint());
				if (g != null) {
					Rectangle gloc = (Rectangle) gatePositions.get(g);
					Point p = e.getPoint();
					Gate.Input inp = getGateInput(g, p.x - gloc.x, p.y - gloc.y);
					connectGates(dragStartGate, inp);
				}
				dragStartGate = null;
			}
		}

		/**
		 * Returns the input of the gate which is at or near the given
		 * point (relative to the top left corner of the gate).  Currently,
		 * inputs are equally spaced along the left-hand side of the gate's
		 * bounding rectangle, so this is just a simple calculation.  It
		 * may become more sophisticated as required.
		 * 
		 * @param g The gate.
		 * @param x The x offset, in pixels, from the left of the gate's bounding box.
		 * @param y The y offset, in pixels, from the top of the gate's bounding box.
		 * @return The nearest input, or null if there is no input nearby.
		 */
		private Input getGateInput(Gate g, int x, int y) {
			final int nt = 4; // the nearness threshold, in pixels
			final int ni = g.getInputs().length;  // number of inputs on this gate
			Rectangle bb = (Rectangle) gatePositions.get(g); // the bounding box for this gate
			if (x > nt) return null;
			else return g.getInputs()[y / ni];
		}
	}
}
