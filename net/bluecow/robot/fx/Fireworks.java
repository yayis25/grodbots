/*
 * Created on Jan 22, 2006
 *
 * This code belongs to Jonathan Fuerth.
 */
package net.bluecow.robot.fx;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

public class Fireworks {
    
    private static class Velocity {
        private float x;
        private float y;
        
        public Velocity(float x, float y) {
            this.x = x;
            this.y = y;
        }
        
        public Velocity add(float x, float y) {
            return new Velocity(this.x + x, this.y + y);
        }
        
        public float getX() {
            return x;
        }
        
        public float getY() {
            return y;
        }
    }
    
    private int burstSize;
    
    // internal state
    private int fuseRemaining;
    private Velocity velocity;
    private Point2D.Float position;
    
    public static final float GRAV_ACCEL = -0.1f;
    
    /**
     * @param heading
     * @param initialSpeed
     * @param fuseLength
     * @param burstSize
     */
    public Fireworks(float heading, float initialSpeed, int fuseLength, int burstSize) {
        super();
        this.fuseRemaining = fuseLength;
        this.burstSize = burstSize;
        
        velocity = new Velocity((float) (Math.sin(heading) * initialSpeed),
                                (float) (Math.cos(heading) * initialSpeed));
        position = new Point2D.Float(100, 100);
    }


    public void nextFrame(Graphics2D g) {
        if (fuseRemaining > 0) {
            velocity = velocity.add(0, GRAV_ACCEL);
            position.x += velocity.getX();
            position.y += velocity.getY();
            fuseRemaining--;
        } else {
            
        }
        
        g.drawLine( (int) position.x, (int) position.y,
                    (int) position.x, (int) position.y);
    }
}
