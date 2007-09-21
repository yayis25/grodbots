/*
 * Created on Sep 17, 2007
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * A class for executing an effect in an asynchronous painting environment
 * such as a typical Swing component.  This class uses a Swing Timer to
 * periodically update and paint an effect on a target component.
 * <p>
 * In a synchronous environment, such as a game loop that handles repaints
 * internally, use of this class is not appropriate.  The game loop should
 * advance the effect and paint it explicitly once per frame.
 *
 * @author fuerth
 * @version $Id:$
 */
public class AsyncEffectRenderer {
    
    private static final boolean debugOn = false;

    private static void debug(String msg) {
        if (debugOn) System.out.println(msg);
    }
    
    /**
     * The effect that is being rendered by this effect renderer.
     */
    private final Effect effect;
    
    /**
     * The timer that triggers the next frame of the effect. 
     */
    private final Timer timer;
    
    /**
     * The component the effect is rendered into.
     */
    private final Component targetComponent;
    
    /**
     * Handles rendering a frame of the effect for each tick of the timer.
     */
    private final ActionListener timerHandler = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            
            debug("Rendering a frame!");
            
            effect.nextFrame();
            if (effect.isFinished()) {
                timer.stop();
                targetComponent.repaint();
                return;
            }

            Graphics2D g = (Graphics2D) targetComponent.getGraphics();
            if (g == null) {
                debug("Not painting effect because target component is not displayable");
                return;
            }
            targetComponent.paint(g);
            effect.paint(g);
            if (debugOn) {
                g.setColor(Color.RED);
                g.drawRect(effect.getX(), effect.getY(), effect.getWidth(), effect.getHeight());
            }
            g.dispose();
        }
    };

    /**
     * Creates a new effect renderer for the given effect. Each frame of the
     * effect will be painted into the given target component.
     * <p>
     * The rendering will not begin until the {@link #start()} method has been
     * called.
     * 
     * @param effect The effect to render
     * @param targetComponent The component to render the effect into
     */
    public AsyncEffectRenderer(Effect effect, Component targetComponent) {
        if (effect == null) throw new NullPointerException("Null effect not allowed");
        if (targetComponent == null) throw new NullPointerException("Null target component not allowed");
        this.effect = effect;
        this.targetComponent = targetComponent;
        timer = new Timer(20, timerHandler);
    }
    
    /**
     * Begins rendering the effect.  The rendering will cease when the effect
     * reports is has completed, or when the {@link #stop()} method is called.
     */
    public void start() {
        timer.start();
    }

    /**
     * Stops rendering the effect.
     */
    public void stop() {
        timer.stop();
    }
}
