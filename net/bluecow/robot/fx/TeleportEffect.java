/*
 * Created on Feb 19, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.fx;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import net.bluecow.robot.Playfield;
import net.bluecow.robot.Robot;
import net.bluecow.robot.sprite.Sprite;

public class TeleportEffect extends AbstractEffect {

    private int frame = 0;
    private int nframes = 10;
    
    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;
    
    /**
     * The playfield location we're moving the robot to.
     */
    private final Point2D target;
    
    /**
     * The width (alsi height) of a square on the playfield.
     */
    private final int squareWidth;
    
    /**
     * The robot we're teleporting.
     */
    private final Robot robot;
    
    /**
     * Creates a teleport effect that shows teleportation of the robot from
     * its current location to location (x,y).
     */
    public TeleportEffect(double x, double y, Robot robot, Playfield playfield) {
        this.x1 = (int) robot.getX();
        this.y1 = (int) robot.getY();
        this.x2 = (int) x;
        this.y2 = (int) y;
        target = new Point2D.Double(x, y);
        this.squareWidth = playfield.getSquareWidth();
        this.robot = robot;
    }
    
    @Override
    public boolean isFinished() {
        boolean finished = frame >= nframes;
        if (finished) {
            robot.getSprite().setTransform(new AffineTransform());
        }
        return finished;
    }

    @Override
    public void nextFrame() {
        frame += 1;
    }

    @Override
    public void paint(Graphics2D g2, int x, int y) {
        System.out.println("Painting teleport. frame="+frame);
        
        int halfway = nframes / 2;
        
        // outbound
        int r = frame;
        paintCentredCircle(g2, x1, y1, r);
        
        // inbound
        r = nframes - frame;
        paintCentredCircle(g2, x2, y2, r);
        
        if (frame == halfway) {
            robot.setPosition(target);
        }
        
        /* the effect severity increases as we approach the halfway point of the
         * teleport, and decreases as we move away from it toward the finish.
         */
        double effectSeverity = Math.min(100.0, Math.abs(1.0 / (nframes / 2.0 - frame)));
        final Sprite roboSprite = robot.getSprite();
        AffineTransform transform = new AffineTransform();
        transform.translate(roboSprite.getWidth()/2.0, roboSprite.getHeight()/2.0);
        final double xscale = effectSeverity * 2.0;
        final double yscale = 1.0 / (effectSeverity + 1.0);
        System.out.println("Severity="+effectSeverity+"; Scaling "+xscale+","+yscale);
        transform.scale(xscale, yscale);
        transform.translate(-roboSprite.getWidth()/2.0, -roboSprite.getHeight()/2.0);
        roboSprite.setTransform(transform);
    }
    
    /**
     * Paints a circle with radius <tt>r</tt> centred on the playfield square <tt>(x,y)</tt>.
     * Correct behaviour depends on the {@link #squareWidth} property being
     * set correctly.
     */
    private void paintCentredCircle(Graphics2D g2, int x, int y, int r) {
        g2.drawOval(x * squareWidth + squareWidth/2 - r, y * squareWidth + squareWidth/2 - r, r*2, r*2);
    }

}
