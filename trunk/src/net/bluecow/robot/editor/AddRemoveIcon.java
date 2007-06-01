/*
 * Created on Jan 11, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.Icon;

public class AddRemoveIcon implements Icon {

    public static enum Type {
        ADD, REMOVE;
    }
    
    /**
     * This icon's type (add or remove).
     */
    private final Type type;
    
    /**
     * This icon's width and height in pixels.
     */
    private final int size = 8;
    
    /**
     * The width of a stroke (the horizontal and/or vertical line this
     * icon draws) in pixels.
     */
    private final float strokeWidth = 1.999f;
    
    public AddRemoveIcon(Type type) {
        this.type = type;
    }
    
    public int getIconHeight() {
        return size;
    }

    public int getIconWidth() {
        return size;
    }

    /**
     * Paints a "+" or "-" symbol, depending on this icon's type.
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        float xf = x;
        float yf = y;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
        Line2D horiz = new Line2D.Float(
                xf,      yf+(size/2f),
                xf+size, yf+(size/2f));
        g2.draw(horiz);
        if (type == Type.ADD) {
            Line2D vert = new Line2D.Float(
                    xf+(size/2f), yf,
                    xf+(size/2f), yf+size);
            g2.draw(vert);
        }
        g2.dispose();
    }

}
