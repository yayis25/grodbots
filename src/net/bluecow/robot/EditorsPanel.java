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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

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
     * The FrameDecoration class takes care of drawing the decoration that indicates
     * which editor is the current editor.  Its current implementation is to draw
     * a thick "L" shape at each corner.
     *
     * @author fuerth
     * @version $Id:$
     */
    private class FrameDecoration {
        
        /**
         * The bounds of the item that is to be framed.
         */
        private Rectangle bounds;
        
        /**
         * The amount to adjust the frame size by.  0 means natural size;
         * negative numbers make the frame smaller and positive numbers make
         * it larger.
         */
        private int adj = 0;
        
        /**
         * The colour to paint the decoration in.
         */
        private Color color = Color.RED;

        /**
         * The stroke style to paint the decoration in.
         */
        private Stroke stroke;
        
        /**
         * The length to draw each line segment.
         */
        private int segmentLength = 40;
        
        /**
         * Creates a new frame decoration object for the given rectangular region.
         * 
         * @param bounds The region to decorate.
         */
        public FrameDecoration(Rectangle bounds) {
            super();
            this.bounds = bounds;
            int strokeWidth = 12;
            stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            adj = strokeWidth / 2;
        }
        
        public void paint(Graphics2D g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(color);
            g2.setStroke(stroke);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            
            GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 12);

            Rectangle b = new Rectangle(bounds);
            b.x += adj;
            b.y += adj;
            b.width -= adj * 2;
            b.height -= adj * 2;
            
            // top left
            p.moveTo(b.x, b.y + segmentLength);
            p.lineTo(b.x, b.y);
            p.lineTo(b.x + segmentLength, b.y);
            
            // top right
            p.moveTo(b.x + b.width - segmentLength, b.y);
            p.lineTo(b.x + b.width, b.y);
            p.lineTo(b.x + b.width, b.y + segmentLength);
            
            // bottom right
            p.moveTo(b.x + b.width, b.y + b.height - segmentLength);
            p.lineTo(b.x + b.width, b.y + b.height);
            p.lineTo(b.x + b.width - segmentLength, b.y + b.height);

            // bottom left
            p.moveTo(b.x + segmentLength, b.y + b.height);
            p.lineTo(b.x, b.y + b.height);
            p.lineTo(b.x, b.y + b.height - segmentLength);

            debug("Drawing frame with adjustment=" + adj);
            g2.draw(p);
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public void setBounds(Rectangle bounds) {
            this.bounds = bounds;
        }

        public int getSizeAdjustment() {
            return adj;
        }

        public void setSizeAdjustment(int sizeAdjustment) {
            this.adj = sizeAdjustment;
        }
    }
    
    private void animateFrameDecoration(final FrameDecoration fd) {

        final int nframes = 10;
        final Timer timer = new Timer(10, null);

        ActionListener timerHandler = new ActionListener() {
            
            private int frame = 0;
            
            public void actionPerformed(ActionEvent e) {
                if (frame < nframes/2) {
                    fd.setSizeAdjustment(fd.getSizeAdjustment() - 1);
                } else {
                    fd.setSizeAdjustment(fd.getSizeAdjustment() + 1);
                }
                frame++;
                repaint();
                if (frame == nframes) {
                    timer.stop();
                }
            }
        };
        
        timer.addActionListener(timerHandler);
        timer.start();
    }
    
    private class SelectEditorAction extends AbstractAction {
        
        private final int index;
        
        public SelectEditorAction(int index) {
            super("Select Editor " + index);
            this.index = index;
        }

        public void actionPerformed(ActionEvent e) {
            debug("SelectEditorAction: selecting editor " + index);
            setSelectedCircuit(circuits.get(index).circuit);
        }
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
    
    private MouseListener mouseHandler = new MouseListener() {

        public void mousePressed(MouseEvent e) {
            Circuit c = getCircuitAt(e.getPoint());
            if (c != null) {
                setSelectedCircuit(c);
            }
        }
        
        public void mouseReleased(MouseEvent e) { }
        public void mouseClicked(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
    };
    
    /**
     * Deals with layout and positioning problems that happen when this component
     * is resized.  If we were using real child components instead of drawing everything
     * ourselves, it would be better to handle this with a layout manager.
     */
    private ComponentListener resizeHandler = new ComponentAdapter() {

        public void componentResized(ComponentEvent e) {
            if (selectedCircuit != null) {
                selectedEditorDecoration = new FrameDecoration(getEditorBounds(selectedCircuit.circuitEditor));
            }
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
     * The height of the footer in this component (circuit names are
     * rendered within the footer).
     */
    private int footerHeight = 50;
    
    /**
     * The bundle of information about the circuit that is currently selected.
     */
    private CircuitStuff selectedCircuit;
    
    /**
     * Utility object that paints a decoration around the selected circuit editor.
     */
    private FrameDecoration selectedEditorDecoration;
    
    /**
     * Creates a new panel for a bunch circuit editors.
     * 
     * @param soundManager The sound manager to create circuit editors in this panel
     * with.  It might make sense to ask for a GameConfig in the future, once the
     * sound manager belongs to the game config.
     */
    public EditorsPanel(SoundManager soundManager) {
        this.soundManager = soundManager;
        addMouseListener(mouseHandler);
        addComponentListener(resizeHandler);
        setForeground(Color.WHITE);
    }
    
    /**
     * The circuits this panel displays. (Maybe this should be a small inner
     * class which contains circuits and their related editor instance).
     */
    private List<CircuitStuff> circuits = new ArrayList<CircuitStuff>();
    
    /**
     * Adds the given circuit to this panel, creating a circuit editor component
     * for it. If no circuit is currently selected (for instance, if the panel
     * was empty before adding the circuit), this method will also make the
     * given circuit the selected one.
     * 
     * @param c
     *            The circuit to add
     * @return The circuit editor that was created for the given circuit. It is
     *         used for painting the thumbnail, but it is not a child component
     *         of this panel. You can use it as you would a circuit editor you
     *         created directly.
     */
    public CircuitEditor addCircuit(Circuit c) {
        CircuitStuff cs = new CircuitStuff();
        cs.circuit = c;
        cs.circuitEditor = new CircuitEditor(c, soundManager);
        cs.circuitEditor.setSize(cs.circuitEditor.getPreferredSize());
        cs.circuitEditor.getLayout().layoutContainer(cs.circuitEditor);
        circuits.add(cs);
        int index = circuits.size() - 1;
        
        cs.circuit.addCircuitListener(repaintHandler);
        
        setPreferredSize(new Dimension(
                (thumbnailSize.width + thumbnailGap) * circuits.size(),
                thumbnailSize.height + footerHeight));
        
        if (selectedCircuit == null) {
            setSelectedCircuit(cs.circuit);
        }
        
        if (index < 9) {
            SelectEditorAction action = new SelectEditorAction(index);
            getActionMap().put(action.getValue(Action.NAME), action);
            char keyChar = String.valueOf(index + 1).charAt(0);
            debug("Binding action for " + action.getValue(Action.NAME) + " to keystroke " + keyChar);
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyChar), action.getValue(Action.NAME));
        }
        
        return cs.circuitEditor;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        debug("Painting EditorsPanel...");
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(getForeground());
        
        FontMetrics fm = getFontMetrics(getFont());
        
        int i = 0;
        for (CircuitStuff cs : circuits) {
            Rectangle thumbBounds = getEditorBounds(i);
            Dimension editorSize = cs.circuitEditor.getSize();
            Graphics2D thumbGraphics = (Graphics2D) g2.create();
            thumbGraphics.translate(thumbBounds.x, thumbBounds.y);
            double s = thumbBounds.getWidth() / cs.circuitEditor.getWidth();
            thumbGraphics.scale(s, s);
            thumbGraphics.setClip(0, 0, editorSize.width, editorSize.height);
            
            cs.circuitEditor.paint(thumbGraphics);
            
            thumbGraphics.dispose();

            double textWidth = fm.getStringBounds(cs.circuit.getName(), g2).getWidth();
            g2.drawString(cs.circuit.getName(),
                    thumbBounds.x + thumbnailSize.width / 2 - (int) textWidth / 2,
                    thumbBounds.y + thumbnailSize.height + footerHeight / 2 - fm.getHeight() / 2);
            
            i++;
        }

        if (selectedEditorDecoration != null) {
            selectedEditorDecoration.paint(g2);
        }

    }
    
    /**
     * Changes the selected circuit to <tt>c</tt>. A property change event
     * for \"selectedCircuit\" will be fired if the new value is different from
     * the previous one.
     * 
     * @param c The circuit to select. Null is not allowed.
     */
    public void setSelectedCircuit(Circuit c) {
        Circuit oldValue = selectedCircuit == null ? null : selectedCircuit.circuit;
        selectedCircuit = getCircuitStuff(c);
        firePropertyChange("selectedCircuit", oldValue, c);
        selectedEditorDecoration = new FrameDecoration(getEditorBounds(selectedCircuit.circuitEditor));
        animateFrameDecoration(selectedEditorDecoration);
        repaint();
        debug("setSelectedCircuit(" + c + ")");
    }
    
    /**
     * Returns the editor for the currently-selected circuit.
     * @return
     */
    public Circuit getSelectedCircuit() {
        return selectedCircuit == null ? null : selectedCircuit.circuit;
    }

    /**
     * Returns the editor for the currently-selected circuit.
     */
    public CircuitEditor getSelectedEditor() {
        return selectedCircuit == null ? null : selectedCircuit.circuitEditor;
    }
    
    /**
     * Finds the editor thumbnail at the given point and returns the circuit
     * associated with it.  If there is no editor at the given point,
     * returns null.
     */
    public Circuit getCircuitAt(Point p) {
        int startX = getEditorBounds(0).x;
        int index = (p.x - startX) / (thumbnailSize.width + thumbnailGap);
        if (index < circuits.size()) {
            return circuits.get(index).circuit;
        } else {
            return null;
        }
    }

    /**
     * Retrieves the CircuitStuff instance that contains the given circuit.
     * 
     * @param c The circuit whose stuff to look for.  Must not be null.
     * @return the CircuitStuff related to the given circuit, or null if that
     * circuit doesn't have any CircuitStuff for this editors panel.
     */
    private CircuitStuff getCircuitStuff(Circuit c) {
        if (c == null) throw new NullPointerException("Null circuit not allowed");
        for (CircuitStuff cs : circuits) {
            if (cs.circuit == c) return cs;
        }
        return null;
    }
    
    /**
     * Throws UnsupportedOperationException when called, because this component
     * doesn't handle children in the typical Swing fashion.  See {@link #addCircuit()}.
     */
    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        throw new UnsupportedOperationException("Child components are not supported. Use addCircuit().");
    }

    /**
     * Returns the bounds of the given circuit editor within this editor panel,
     * or null if the given circuit editor is not represented in this panel.
     * 
     * @param editor The editor whose bounds to retrieve
     * @return A new Rectangle instance with the given editor's bounds.  It is
     * safe for calling code to modify the returned rectangle (there will be
     * no side effects)
     */
    public Rectangle getEditorBounds(CircuitEditor editor) {
        int index = 0;
        for (CircuitStuff cs : circuits) {
            if (cs.circuitEditor == editor) break;
            index++;
        }
        
        if (index == circuits.size()) {
            return null;
        } else {
            return getEditorBounds(index);
        }
    }
    
    /**
     * Calculates and returns the bounding box for the index<super>th</super>
     * editor thumbnail.
     * 
     * @param index Which editor's bounds to calculate. 0 is the leftmost editor.
     * @return A new Rectangle instance with the given editor's bounds.  It is
     * safe for calling code to modify the returned rectangle (there will be
     * no side effects)
     */
    public Rectangle getEditorBounds(int index) {
        if (index >= circuits.size()) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of range" +
                    " (circuits.size() == " + circuits.size() + ")");
        }
        int totalWidth = circuits.size() * (thumbnailSize.width + thumbnailGap);
        int startX = getWidth() / 2 - totalWidth / 2; 
        return new Rectangle(
                startX + (thumbnailSize.width + thumbnailGap) * index,
                0,
                thumbnailSize.width,
                thumbnailSize.height);
    }
}
