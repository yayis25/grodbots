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
