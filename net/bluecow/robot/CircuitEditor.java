package net.bluecow.robot;

import javax.swing.*;
import javax.swing.event.*;
import net.bluecow.robot.gate.*;
import java.awt.*;

public class CircuitEditor extends JPanel {

    private Gate[] outputs;
    private Gate.Input[] inputs;
    private Color activeColor = Color.orange;

    public CircuitEditor(Gate[] outputs, Gate.Input[] inputs) {
	this.outputs = outputs;
	this.inputs = inputs;
    }

    private void paintOutput(Graphics2D g2, Point p) {
	final int length = 15;
	g2.drawLine(p.x, p.y, p.x+length, p.y);
	g2.drawOval(p.x+length, p.y-5, 10, 10);
    }

    private void paintInput(Graphics2D g2, Point p) {
	final int length = 15;
	g2.drawOval(p.x-length-10, p.y-5, 10, 10);
	g2.drawLine(p.x, p.y, p.x-length, p.y);
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

	    paintOutput(g2, new Point(0, (int) ((0.5+i) * (double) getHeight() / (double) outputs.length)));
	}

	// draw the inputs along the right edge
	for (int i = 0; i < inputs.length; i++) {
	    if (inputs[i].getState() == true) {
		g2.setColor(getActiveColor());
	    } else {
		g2.setColor(getForeground());
	    }

	    paintInput(g2, new Point(getWidth(), (int) ((0.5+i) * (double) getHeight() / (double) inputs.length)));
	}
    }

    
    // ---------------- Accessors and Mutators --------------------
    public void setActiveColor(Color v) {
	this.activeColor = v;
	repaint();
    }

    public Color getActiveColor() {
	return activeColor;
    }
}
