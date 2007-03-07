/*
 * Created on Aug 25, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;

import net.bluecow.robot.GameConfig;
import net.bluecow.robot.LevelConfig;
import net.bluecow.robot.Playfield;
import net.bluecow.robot.GameConfig.SquareConfig;

public class LevelEditor extends Playfield {

    /**
     * The square type that the user wants to paint with right now.
     */
    private SquareConfig paintingSquare = null;
    
    /**
     * The exact location of the pointer in square coordinates.  Useful for placing objects
     * such as robots, which don't occupy one specific square location.
     */
    private Point2D.Double paintingLocationDouble = null;

    /**
     * The square coordinate that the mouse is over.  Useful for placing tiles, switches,
     * and other stuff that occupies exactly one square on the map.  For finer detail,
     * see paintingLocationDouble.
     */
    private Point paintingLocation = null;
    
    private MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mouseExited(MouseEvent e) {
            paintingLocation = null;
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            updatePaintingLocation(e.getPoint());
            changeSquareType(paintingLocation, paintingSquare);
        }

    };

    private MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
            updatePaintingLocation(e.getPoint());
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            updatePaintingLocation(e.getPoint());
            changeSquareType(paintingLocation, paintingSquare);
        }
    };
    
    public LevelEditor(GameConfig game, LevelConfig level) {
        super(game, level);
        setPaintingLevelScore(false);
        setPaintingOverallScore(false);
        setHeaderHeight(0);
        setDescriptionOn(false);
        setClickToToggleDescription(false);
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseMotionAdapter);
    }
    
    private void changeSquareType(Point paintingLocation, SquareConfig paintingSquare) {
        if (paintingLocation != null && paintingSquare != null) {
            getLevel().setSquare(paintingLocation.x, paintingLocation.y, paintingSquare);
        }
    }

    public void setPaintingSquareType(SquareConfig sc) {
        paintingSquare = sc;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        
        if (paintingLocation != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            int x = paintingLocation.x * getSquareWidth();
            int y = paintingLocation.y * getSquareWidth();
            if (paintingSquare != null) {
                paintingSquare.getSprite().paint(g2, x, y);
            }
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRect(x, y, getSquareWidth(), getSquareWidth());
        }
    }
    
    /**
     * Updates the paintingLocation and paintingLocationDouble variables with the
     * current mouse location, as given by the point p.
     * 
     * @param p The mouse's location in component pixel coordinates (exactly as
     * reported by mouse events and mouse motion events).
     */
    private void updatePaintingLocation(Point p) {
        double x = p.getX();
        double y = p.getY();
        double squareWidth = getSquareWidth();
        if (x < 0 || x >= getSquareWidth() * getLevel().getWidth()
                || y < 0 || y >= getSquareWidth() * getLevel().getHeight()) {
            paintingLocation = null;
            paintingLocationDouble = null;
        } else {
            paintingLocationDouble = new Point2D.Double(x / squareWidth, y / squareWidth);
            paintingLocation = new Point((int) (x / squareWidth), (int) (y / squareWidth));
        }
    }
}
