package net.bluecow.robot;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;

/**
 * The robot.
 */
public class Robot {
    
    private PlayfieldModel pfm;
    private Point position;
    private ImageIcon icon;
    
    public Robot(PlayfieldModel pfm, Point initialPosition, ImageIcon icon) {
        this.pfm = pfm;
        this.position = initialPosition;
        this.icon = icon;
    }
    
    public void move() {
        // XXX: this is hardwired for now
        Square s = pfm.getSquare(position.x, position.y); 
        if (s.getType() == Square.RED) {
            moveDown();
        } else if (s.getType() == Square.GREEN) {
            moveRight();
        } else if (s.getType() == Square.BLUE) {
            moveUp();
        }
    }
    
    private void moveLeft() {
        if (position.x > 0
                && pfm.getSquare(position.x-1, position.y).isOccupiable()) {
                Point oldPos = new Point(position);
                position.x -= 1;
                fireMoveEvent(oldPos);
            }
    }

    private void moveRight() {
        if (position.x < pfm.getWidth()
                && pfm.getSquare(position.x+1, position.y).isOccupiable()) {
                Point oldPos = new Point(position);
                position.x += 1;
                fireMoveEvent(oldPos);
            }
    }

    public void moveDown() {
        if (position.y < pfm.getHeight()
                && pfm.getSquare(position.x, position.y+1).isOccupiable()) {
                Point oldPos = new Point(position);
                position.y += 1;
                fireMoveEvent(oldPos);
            }
    }

    public void moveUp() {
        if (position.y > 0
                && pfm.getSquare(position.x, position.y-1).isOccupiable()) {
                Point oldPos = new Point(position);
                position.y -= 1;
                fireMoveEvent(oldPos);
            }
    }
    
    protected List propertyChangeListeners = new ArrayList();
    protected void fireMoveEvent(Point oldPos) {
        PropertyChangeEvent e = new PropertyChangeEvent(this, "position", oldPos, position);
        Iterator it = propertyChangeListeners.iterator();
        while (it.hasNext()) {
            ((PropertyChangeListener) it.next()).propertyChange(e);
        }
    }
    
    // ACCESSORS and MUTATORS
    
    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }
}
