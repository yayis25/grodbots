/*
 * Created on Jun 9, 2006
 *
 * This code belongs to Jonathan Fuerth.
 */
package net.bluecow.robot.gate;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.QuadCurve2D;

import net.bluecow.robot.Circuit;

public abstract class AbstractOrGate extends AbstractGate {

    protected AbstractOrGate(String label) {
        super(label);
    }

    public void drawBody(Graphics2D g2, Rectangle r, int inputStickLength, int outputStickLength) {
        int backX = inputStickLength - inputStickLength/10;  // adjustment compensates for curve
        double backDepth = r.height/6.0;
        int pointyX = r.width - outputStickLength;
        
        // The back part
        g2.draw(new QuadCurve2D.Double(backX, 0, backX + backDepth, r.height/2, backX, r.height));
        
        // Top curve
        g2.draw(new QuadCurve2D.Double(backX, 0, backX + pointyX/2, 0, pointyX, r.height/2));
        
        // Bottom curve
        g2.draw(new QuadCurve2D.Double(backX, r.height, backX + pointyX/2, r.height, pointyX, r.height/2));

    }
}
