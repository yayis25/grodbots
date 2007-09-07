/*
 * Created on Aug 27, 2007
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

package net.bluecow.robot;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import net.bluecow.robot.event.CircuitEvent;
import net.bluecow.robot.event.CircuitListener;

/**
 * The EditorsPanel is a custom component that displays thumbnail previews of
 * any number of circuit editors.  It also acts like a set of radio buttons,
 * with each editor thumbnail being one of the buttons.  At any given time,
 * exactly one editor is the "current" editor, and a change event is fired when
 * the current editor changes.
 *
 * @author fuerth
 * @version $Id:$
 */
public class EditorsPanel extends JPanel {

    private static final boolean debugOn = false;

    private static void debug(String msg) {
        if (debugOn) System.out.println(msg);
    }
    
    private static class CircuitStuff {
        Circuit circuit;
        CircuitEditor circuitEditor;
        // need rectangle or list index?
    }
    
    /**
     * Receives events from all of the circuits and asks the panel
     * to repaint when necessary.
     */
    private CircuitListener repaintHandler = new CircuitListener() {

        public void gatesAdded(CircuitEvent evt) {
            debug("gatesAdded: " + evt.getGatesAffected());
            repaint();
        }

        public void gatesChangedState(CircuitEvent evt) {
            debug("gatesChangedState: " + evt.getGatesAffected());
            repaint();
        }

        public void gatesConnected(CircuitEvent evt) {
            debug("gatesConnected: " + evt.getGatesAffected());
            repaint();
        }

        public void gatesRemoved(CircuitEvent evt) {
            debug("gatesRemoved: " + evt.getGatesAffected());
            repaint();
        }

        public void gatesRepositioned(CircuitEvent evt) {
            debug("gatesRepositioned: " + evt.getGatesAffected());
            repaint();
        }
        
    };
    
    /**
     * The sound manager circuit editors in this panel should be created with.
     */
    private final SoundManager soundManager;
    
    /**
     * The size of each circuit editor thumbnail image.  Also the "hot zone" for
     * buttons.
     */
    private Dimension thumbnailSize = new Dimension(200, 200);
    
    /**
     * The number of pixels between each editor thumbnail.
     */
    private int thumbnailGap = 15;
    
    /**
     * Creates a new panel for a bunch circuit editors.
     * 
     * @param soundManager The sound manager to create circuit editors in this panel
     * with.  It might make sense to ask for a GameConfig in the future, once the
     * sound manager belongs to the game config.
     */
    public EditorsPanel(SoundManager soundManager) {
        this.soundManager = soundManager;
    }
    
    /**
     * The circuits this panel displays. (Maybe this should be a small inner
     * class which contains circuits and their related editor instance).
     */
    private List<CircuitStuff> circuits = new ArrayList<CircuitStuff>();
    
    public CircuitEditor addCircuit(Circuit c) {
        CircuitStuff cs = new CircuitStuff();
        cs.circuit = c;
        cs.circuitEditor = new CircuitEditor(c, soundManager);
        cs.circuitEditor.setSize(cs.circuitEditor.getPreferredSize());
        cs.circuitEditor.getLayout().layoutContainer(cs.circuitEditor);
        circuits.add(cs);

        cs.circuit.addCircuitListener(repaintHandler);
        
        setPreferredSize(new Dimension( (thumbnailSize.width + thumbnailGap) * circuits.size(), thumbnailSize.height));
        
        return cs.circuitEditor;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        int x = 0;
        for (CircuitStuff cs : circuits) {
            Dimension editorSize = cs.circuitEditor.getSize();
            Graphics2D thumbGraphics = (Graphics2D) g2.create();
            thumbGraphics.translate(x, 0);
            double s = thumbnailSize.getWidth() / cs.circuitEditor.getWidth();
            thumbGraphics.scale(s, s);
            thumbGraphics.setClip(0, 0, editorSize.width, editorSize.height);
            
            cs.circuitEditor.paint(thumbGraphics);
            
            x += thumbnailSize.width + thumbnailGap;
            thumbGraphics.dispose();
        }
        
    }
    
    /**
     * Throws UnsupportedOperationException when called, because this component
     * doesn't handle children in the typical Swing fashion.  See {@link #addCircuit()}.
     */
    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        throw new UnsupportedOperationException("Child components are not supported. Use addCircuit().");
    }
}
