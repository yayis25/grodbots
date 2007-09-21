/*
 * Created on Sep 14, 2007
 *
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

package net.bluecow.robot.fx;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

public class ZoomEffect extends AbstractEffect {

    /**
     * Goes from 0.0 to 1.0 in increments of frameStep.
     */
    private double progress = 0.0;

    /**
     * The increments in which this effect progresses from 0.0 (the starting
     * point) to 1.0 (the ending point).
     */
    private final Double frameStep;

    /**
     * The position and size for the first frame of animation.
     */
    private final Rectangle start;

    /**
     * The position and size for the last frame of animation.
     */
    private final Rectangle end;

    /**
     * The image that's zooming.
     */
    private Image image;

    /**
     * Creates a new zoom effect with the given parameters.
     * 
     * @param nframes The number of frames the effect should take before completion.
     * @param sprite The sprite to draw for each frame.
     * @param start The beginning position and size at which to draw the sprite.
     * @param end The final position and size at which to draw the sprite.
     */
    public ZoomEffect(int nframes, Image image, Rectangle start, Rectangle end) {
        frameStep = 1.0/nframes;
        this.image = image;
        this.start = start;
        this.end = new Rectangle(end);
    }

    public void nextFrame() {
        if (progress >= 1.0) {
            progress = 1.0;
        }

        //  8        e  e = (9,8)
        //  7       /
        //  6      /
        //  5     /
        //  4    r      r = (5,4)
        //  3   /                   2 + 3/7 * (9 - 2)  =  2 + 3/7 * 7  =  2 + 3  =  5
        //  2  /
        //  1 s         s = (2,1)
        //  0123456789
        bounds = new Rectangle(
                (int) (start.x + progress * (end.x - start.x)),
                (int) (start.y + progress * (end.y - start.y)),
                (int) (start.width + progress * (end.width - start.width)),
                (int) (start.height + progress * (end.height - start.height))
        );

        progress += frameStep;
    }

    @Override
    public boolean isFinished() {
        return progress >= 1.0;
    }

    @Override
    public void paint(Graphics2D g2, int x, int y) {
        AffineTransform transform = new AffineTransform();
        transform.translate(bounds.x, bounds.y);
        transform.scale(
                bounds.getWidth() / image.getWidth(null),
                bounds.getHeight() / image.getHeight(null));
        g2.drawImage(image, transform, null);
    }

}
