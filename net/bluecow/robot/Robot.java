package net.bluecow.robot;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The robot.
 */
public class Robot {
    
    private Playfield pf;
    private Point position;
    
    public Robot(Playfield pf, Point initialPosition) {
        this.pf = pf;
        this.position = initialPosition;
    }
    
    public void move() {
        // XXX: this is hardwired for now
        Square s = pf.getSquare(position.x, position.y); 
        if (s.getType() == Square.RED) {
            moveDown();
        } else if (s.getType() == Square.GREEN) {
            moveRight();
        }
    }
    
    private void moveRight() {
        if (position.x < pf.getFieldWidth()
                && pf.getSquare(position.x+1, position.y).isOccupiable()) {
                Point oldPos = new Point(position);
                position.x += 1;
                fireMoveEvent(oldPos);
            }
    }

    public void moveDown() {
        if (position.y < pf.getFieldHeight()
                && pf.getSquare(position.x, position.y+1).isOccupiable()) {
                Point oldPos = new Point(position);
                position.y += 1;
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
}
