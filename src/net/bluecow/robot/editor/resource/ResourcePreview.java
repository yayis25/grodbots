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
 * Created on Apr 18, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor.resource;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import net.bluecow.robot.resource.ResourceManager;
import net.bluecow.robot.sprite.Sprite;
import net.bluecow.robot.sprite.SpriteManager;

/**
 * ResourcePreview is a GUI component which can display a visual preview
 * of project resources.
 *
 * @author fuerth
 * @version $Id$
 */
public class ResourcePreview extends JPanel implements TreeSelectionListener {
    
    private final ResourceManager resourceManager;
    
    /**
     * If we are previewing a sprite, it will be referenced here.
     */
    private Sprite previewSprite = null;
    
    private ActionListener spriteAnimateListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (previewSprite != null) {
                previewSprite.nextFrame();
            }
            repaint();
        }
    };
    
    private Timer timer = new Timer(100, spriteAnimateListener);
    
    public ResourcePreview(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        createEmptyPreview();
    }

    /**
     * Sets up the panel in some default way that indicates to the user there
     * is nothing to preview.
     */
    private void createEmptyPreview() {
        add(new JLabel("Nothing selected"));
    }

    /**
     * This is the TreeSelectionListener method.  When a tree item is
     * selected, the panel will get updated with a preview of the selected
     * resource.
     */
    public void valueChanged(TreeSelectionEvent e) {
        removeAll();
        previewSprite = null;
        timer.stop();
        JTree tree = (JTree) e.getSource();
        TreePath[] paths = tree.getSelectionPaths();
        try {
            if (paths.length == 0) {
                createEmptyPreview();
            } else if (paths.length > 1) {
                add(new JLabel(paths.length + " items selected"));
            } else {
                String path = paths[0].getLastPathComponent().toString();
                
                // XXX I'd rather do this by looking at magic numbers (ideally, SpriteManager would
                //     have a method for examining a resource and saying if it's a sprite or not)
                if (path.endsWith(".rsf") || path.endsWith(".png")) {
                    Sprite s = SpriteManager.load(resourceManager, path);
                    previewSprite = s;
                    timer.start();
                } else {
                    add(new JLabel(path));
                }
            }
        } catch (Exception ex) {
            add(new JLabel("Preview Failed: " + ex.toString()));
        }
        revalidate();
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(getForeground());
        if (previewSprite != null) {
            previewSprite.paint(g2, 0, 0);
        }
    }
}
