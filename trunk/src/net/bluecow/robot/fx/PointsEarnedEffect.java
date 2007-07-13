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
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.robot.fx;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

/**
 * The PointsEarnedEffect gives a slowly-rising-text visual effect.  The message
 * (often a number, representing bonus points just earned by the player) slowly
 * rises from its initial position, also fading from opaque to transparent as it
 * rises.
 *
 * @author fuerth
 * @version $Id$
 */
public class PointsEarnedEffect extends AbstractEffect {

    /**
     * The message to display (the pointsAmount as a string).
     */
    private String message;
    
    /**
     * The time-to-live for this effect, in frames.  Defaults to 15.
     */
    private int ttl = 15;
    
    /**
     * When this number of frames remains, the message will start to fade smoothly to 0.
     * If you don't want a fade out effect, set this to 0.
     */
    private int fadeoutTtl = 5;
    
    /**
     * The colour that the message string will paint in.  Defaults to white.
     */
    private Color color = Color.WHITE;
    
    /**
     * Creates a new effect that shows a slowly-rising number, starting at
     * the given position.
     * 
     * @param startPosition The position (in pixels) to start the points display at
     * @param pointsAmount The number to display
     */
    public PointsEarnedEffect(Point startPosition, int pointsAmount) {
        message = String.valueOf(pointsAmount);
        bounds.setLocation(startPosition);
    }
    
    @Override
    public boolean isFinished() {
        return ttl <= 0;
    }

    @Override
    public void nextFrame() {
        ttl -= 1;
        bounds.y -= 1;
    }

    @Override
    public void paint(Graphics2D g2, int x, int y) {
        
        // System.out.println("Drawing string '"+message+"' at ("+x+","+y+") in "+g2.getColor());
        
        g2.setColor(color);
        
        if (ttl < fadeoutTtl) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float) ttl) / ((float) fadeoutTtl)));
        }
        
        g2.drawString(message, x, y);
    }
}
