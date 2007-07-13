/*
 * Copyright (c) 2007, Jonathan Fuerth
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Jonathan Fuerth nor the names of other
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
