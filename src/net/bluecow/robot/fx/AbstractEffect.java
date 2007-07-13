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
 * Created on Feb 18, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.fx;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.Map;

public abstract class AbstractEffect implements Effect {

    /**
     * This effect's current position and size.
     */
    protected Rectangle bounds = new Rectangle();
    
    /**
     * This effect's scaling factor. Scales the size that this effect paints
     * itself at, but not its position. The implementation of paint() should
     * respect this value.
     */
    protected double scale = 1.0;
    
    /**
     * This effect's transform.  This abstract effect class does not attempt to do
     * anything with the transform beyond remembering its value for the get and
     * set methods.  Subclasses are responsible for applying this effect in whatever
     * way is appropriate.
     */
    protected AffineTransform transform;
    
    protected AbstractEffect() {
        // nothing to do
    }

    /**
     * Returns an empty map.
     */
    public Map<String, String> getAttributes() {
        return Collections.emptyMap();
    }

    public int getX() {
        return bounds.x;
    }
    
    public int getY() {
        return bounds.y;
    }
    
    public int getHeight() {
        return bounds.height;
    }

    public int getWidth() {
        return bounds.width;
    }
    
    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }
    
    public AffineTransform getTransform() {
        return transform;
    }

    public void setTransform(AffineTransform transform) {
        this.transform = transform;
    }

    public abstract void nextFrame();
    
    /**
     * Left for implementations to implement.
     */
    public abstract void paint(Graphics2D g2, int x, int y);

    /**
     * Paints this effect at its current position by calling the
     * {@link #paint(Graphics2D, int, int)} method.
     */
    public void paint(Graphics2D g2) {
        paint(g2, bounds.x, bounds.y);
    }

    /**
     * Returns a new Point object of the current bounds rectangle's
     * position.
     */
    public Point getPosition() {
        return new Point(bounds.x, bounds.y);
    }

    /**
     * Not implemented, because it is up to each effect to know when its
     * time is up.
     */
    public abstract boolean isFinished();
    
    /**
     * Not currently implemented.
     */
    @Override
    public Effect clone() {
        throw new RuntimeException("There isn't currently a generic clone for effects.");
    }
}
