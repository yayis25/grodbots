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
 * Created on Apr 18, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Map;

/**
 * Sprite is an interface to any type of moving or static image resource
 * in the Robot game, possibly animated by having multiple frames (all of the
 * same size) with variable delays between them.
 *
 * @author fuerth
 * @version $Id$
 */
public interface Sprite {

    /**
     * Attribute key for the resource path of the file that defines this 
     * Sprite's appearance.
     * 
     * <p>See {@link #getAttributes()}.
     */
    public static final String KEY_HREF = "href";
    
    /**
     * Sprites are cloneable.  Their clones will work independently
     * of their originals, but might not copy the actual image resources.
     */
    public Sprite clone();
    
    /**
     * Returns the attribute map that was passed to the SpriteManager in
     * order to create this sprite object.  Passing this map back to the
     * SpriteManager would result in the creation of an identically-configured
     * sprite.
     */
    Map<String, String> getAttributes();
    
    /**
     * Paints this sprite into the given graphics
     * at the given (x,y) coordinates.
     */
    void paint(Graphics2D g2, int x, int y);

    /**
     * Returns the width of a bounding box that contains all the pixels in
     * this sprite.
     */
    int getWidth();

    /**
     * Returns the height of a bounding box that contains all the pixels in
     * this sprite.
     */
    int getHeight();
    
    /**
     * Reports the scaling factor that will be applied to the source image data
     * before it is drawn (the scale factor also affects the width and height
     * reported by the sprite).
     */
    double getScale();
    
    /**
     * Sets the sprite's scale factor.  1.0 means to draw the sprite at its natural
     * size.
     */
    void setScale(double scale);
    
    /**
     * Returns the current transformation that will be applied when painting this
     * sprite.  The scale factor (see {@link #getScale()}) will be applied after
     * this transformation.  This feature is intended for special
     * effects that temporarily warp or distort the sprite during game play.  Its
     * default value is always the identity transform.
     * <p>
     * This transformation will not be taken into account when reporting this
     * sprite's visible dimensions.  This policy differs from that of the scale
     * setting.
     */
    AffineTransform getTransform();
    
    /**
     * Sets the transformation that will be applied when painting this
     * sprite.  The scale factor (see {@link #getScale()}) will be applied after
     * this transformation.  This feature is intended for special
     * effects that temporarily warp or distort the sprite during game play.  Its
     * default value is always the identity transform.
     * <p>
     * This transformation will not be taken into account when reporting this
     * sprite's visible dimensions.  This policy differs from that of the scale
     * setting.
     */
    void setTransform(AffineTransform transform);
    
    /**
     * Tells this sprite that a frame of the overall gamee has passed, and it
     * should paint the next frame in its sequence next time it is asked to paint.
     */
    void nextFrame();
}
