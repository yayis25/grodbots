/*
 * Created on Jun 10, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.gate;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import net.bluecow.robot.Circuit;

public abstract class AbstractAndGate extends AbstractGate {

    public AbstractAndGate(String label) {
        super(label);
    }

    @Override
    public void drawBody(Graphics2D g2, Rectangle r, int inputStickLength, int outputStickLength) {
        int backX = inputStickLength;
        int arcRadius = r.height/2;
        int straightLength = r.width - arcRadius - inputStickLength - outputStickLength;
        if (straightLength < 0) straightLength = 0;
        g2.drawLine(backX, 0, backX, r.height);
        g2.drawLine(backX, 0, backX+straightLength, 0);
        g2.drawLine(backX, r.height, backX+straightLength, r.height);
        g2.drawArc(backX + straightLength - arcRadius, 0, arcRadius*2, r.height, 270, 90);
        g2.drawArc(backX + straightLength - arcRadius, 0, arcRadius*2, r.height, 0, 90);
    }

}
