/*
 * Created on Jan 22, 2006
 *
 * This code belongs to Jonathan Fuerth.
 */
package net.bluecow.robot.fx;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * TODO make this class implement the Effect interface
 *
 * @author fuerth
 * @version $Id$
 */
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
    
    // internal state
    private int fuseRemaining;
    private Velocity velocity;
    private Point2D.Float position;
    
    // these should be refactored into a Projectile class
    private Point2D.Float[] shrapnelPosition;
    private Velocity[] shrapnelVelocity;
    private int[] shrapnelTTL;
    
    public static final float GRAV_ACCEL = -0.15f;
    
    /**
     * @param heading
     * @param initialSpeed
     * @param fuseLength
     * @param burstSize
     */
    public Fireworks(float heading, float initialSpeed, int fuseLength,
            int burstSize,
            double burstPowerStdDev, double burstPowerMean,
            double ttlStdDev, double ttlMean) {
        super();
        this.fuseRemaining = fuseLength;
        
        velocity = new Velocity((float) (Math.sin(heading) * initialSpeed),
                                (float) (Math.cos(heading) * initialSpeed));
        position = new Point2D.Float(100, 0);
        shrapnelPosition = new Point2D.Float[burstSize];
        shrapnelVelocity = new Velocity[burstSize];
        shrapnelTTL = new int[burstSize];
        Random rnd = new Random();
        for (int i = 0; i < burstSize; i++) {
            shrapnelPosition[i] = new Point2D.Float(0f, 0f);
            shrapnelVelocity[i] = new Velocity(
                    (float) (Math.sin(i*(Math.PI*2.0/burstSize)) * (rnd.nextGaussian() * burstPowerStdDev + burstPowerMean)),
                    (float) (Math.cos(i*(Math.PI*2.0/burstSize)) * (rnd.nextGaussian() * burstPowerStdDev + burstPowerMean)));
            shrapnelTTL[i] = (int) (rnd.nextGaussian() * ttlStdDev + ttlMean);
        }
    }


    /**
     * Draws the next frame on the given graphics.  Returns true while the
     * animation still has more frames to draw; false when the show is over.
     */
    public boolean nextFrame(Graphics2D g) {
        boolean done = true;
        if (fuseRemaining > 0) {
            velocity = velocity.add(0, GRAV_ACCEL);
            position.x += velocity.getX();
            position.y += velocity.getY();
            fuseRemaining--;
            g.drawLine( (int) position.x, (int) position.y,
                        (int) position.x, (int) position.y);
            done = false;
        } else {
            for (int i = 0; i < shrapnelPosition.length; i++) {
                Point2D.Float p = shrapnelPosition[i];
                Velocity v = shrapnelVelocity[i];
                v = v.add(0, GRAV_ACCEL);
                shrapnelVelocity[i] = v;
                p.x += v.x + velocity.x;
                p.y += v.y + velocity.y;
                if (shrapnelTTL[i] >= 0) {
                    g.drawLine( (int) (position.x + p.x), (int) (position.y + p.y),
                               (int) (position.x + p.x - v.x), (int) (position.y + p.y - v.y) );
                }
                shrapnelTTL[i]--;
                boolean thisDone = (position.y + p.y < 0) || (shrapnelTTL[i] < 0);
                done &= thisDone;
            }

        }
        return !done;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                List<Fireworks> liveFire = new LinkedList<Fireworks>();
                liveFire.add(new Fireworks(0.3f, 10.0f, 35, 300, 1.5, 4.5, 10.0, 30.0));
                //liveFire.add(new Fireworks(0.3f, 10.0f, 35, 300, 1.0, 1.5, 10.0, 30.0));
                JFrame f = new JFrame("Fireworks!");
                JPanel p = new JPanel();
                f.setContentPane(p);
                f.setSize(640,480);
                f.setVisible(true);
                Graphics2D g = (Graphics2D) p.getGraphics();
                g.translate(0, f.getHeight());
                g.scale(1.0, -1.0);
                boolean keepGoing;
                do {
                    keepGoing = false;
                    try {
                        g.setColor(p.getBackground());
                        g.fillRect(0, 0, f.getWidth(), f.getHeight());
                        g.setColor(p.getForeground());
                        for (Fireworks anim : liveFire) {
                            keepGoing |= anim.nextFrame(g);
                        }
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        System.out.println("Woops, interrupted");
                    }
                } while (keepGoing);
                System.exit(0);
            }
        });
    }
}
