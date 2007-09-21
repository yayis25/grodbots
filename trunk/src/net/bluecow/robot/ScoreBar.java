/*
 * Created on Sep 7, 2007
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

public class ScoreBar extends JPanel {
    
    /**
     * When this field is true, the paintComponent() method will paint the
     * "overall score" label. True by default.
     */
    private boolean paintingOverallScore = true;

    /**
     * When this field is true, the paintComponent() method will paint the
     * "score for this level" label. True by default.
     */
    private boolean paintingLevelScore = true;
    
    /**
     * The config for the whole game (currently used only to find out
     * the total score).
     */
    private GameConfig game;
    
    /**
     * The level this playfield is currently displaying.  Must belong to {@link #game}.
     */
    private LevelConfig level;
    
    /**
     * @param game
     * @param level
     */
    public ScoreBar(GameConfig game, LevelConfig level) {
        this.game = game;
        this.level = level;
        
        // the width is arbitrary, and the height is double the font height because
        // font height on its own isn't enough (?)
        setPreferredSize(new Dimension(200, getFont().getSize() * 2));
        
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        setOpaque(true);

        //XXX need to remove listener when this component is no longer in use
        level.addPropertyChangeListener("score", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(getForeground());
        
        FontMetrics fm = getFontMetrics(getFont());

        if (paintingOverallScore) {
            String score = String.format("Overall Score: %06d", game.getScore());
            int x = getWidth() / 2;
            int y = fm.getHeight();
            g2.setColor(Color.WHITE);
            g2.drawString(score, x, y);
        }

        if (paintingLevelScore) {
            String levelScore = String.format("This Level's Score: %06d", level.getScore());
            int x = 0;
            int y = fm.getHeight();
            g2.setColor(Color.WHITE);
            g2.drawString(levelScore, x, y);
        }
    }
    
    public boolean isPaintingLevelScore() {
        return paintingLevelScore;
    }

    public void setPaintingLevelScore(boolean paintingLevelScore) {
        this.paintingLevelScore = paintingLevelScore;
    }

    public boolean isPaintingOverallScore() {
        return paintingOverallScore;
    }

    public void setPaintingOverallScore(boolean paintingOverallScore) {
        this.paintingOverallScore = paintingOverallScore;
    }

}
