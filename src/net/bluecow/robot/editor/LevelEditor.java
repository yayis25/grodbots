/*
 * Created on Aug 25, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
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
import net.bluecow.robot.Robot;
import net.bluecow.robot.GameConfig.SquareConfig;
import net.bluecow.robot.LevelConfig.Switch;
import net.bluecow.robot.sprite.Sprite;

/**
 * The Level Editor for GrodBots.  This is an extended version of the Playfield
 * class, which is used while playing the game.  This extended version provides
 * several modes of reacting to mouse input:
 * 
 * <ul>
 * 
 * <li><b>Painting:</b> This is the default state.  Mouse click and drag gestures
 * will set the square type for the square under the mouse pointer.  To set which
 * square type the editor paints with, see {@link #setPaintingSquareType(SquareConfig)}.
 * 
 * <li><b>Repositioning:</b> This is a temporary state in which a sprite tracks the
 * mouse pointer until the mouse button is clicked.  After the click, the editor
 * transitions back to the default (painting) state.  To enter the repositioning state,
 * use {@link #repositionSprite(Sprite)}.
 * 
 * </ul>
 *
 * @author fuerth
 * @version $Id:$
 */
public class LevelEditor extends Playfield {

    /**
     * The square type that the user wants to paint with right now.
     */
    private SquareConfig paintingSquare = null;
    
    /**
     * The object that is currently being repositioned by this editor.
     * In order for this sprite to actually track the mouse pointer's position,
     * the editor's state must be set to POSITIONING.
     * <p>
     * TODO turn this into a Sprite once Sprite supports (set|get)Position
     */
    private Object repositioningSprite;

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
    
    /**
     * The set of states this editor can be in for handling mouse input.
     */
    private enum State {
        /**
         * The default state. From this state, it is possible to paint squares
         * by pressing or dragging the mouse pointer.
         */
        DEFAULT,
        
        /**
         * The state we're in when we're positioning some sprite (i.e. a robot
         * or a switch).  From this state, clicking the mouse will anchor the
         * object we're positioning, and then the state will transition back
         * to DEFAULT.
         */
        POSITIONING
    }
    
    /**
     * The current state of this editor.  The state affects how the editor reacts
     * to mouse events.
     */
    private State state = State.DEFAULT;
    
    private MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mouseExited(MouseEvent e) {
            paintingLocation = null;
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            updatePaintingLocation(e.getPoint());
            if (state == State.DEFAULT) {
                // TODO make this a Sprite when Sprites support positioning
                Object movableItem = getSpriteAt(paintingLocationDouble);
                if (movableItem != null) {
                    repositionSprite(movableItem);
                    // state is now POSITIONING
                } else {
                    changeSquareType(paintingLocation, paintingSquare);
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            updatePaintingLocation(e.getPoint());
            if (state == State.POSITIONING) {
                repositioningSprite = null;
                state = State.DEFAULT;
                setCursor(null);
            }
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
            if (state == State.DEFAULT) {
                changeSquareType(paintingLocation, paintingSquare);
            } else if (state == State.POSITIONING) {
                repositionFloatingSprite(paintingLocationDouble);
            }
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

    /**
     * Causes this level editor to switch from its default behaviour (of allowing
     * painting) to a different behaviour in which the given sprite tracks the
     * mouse pointer until the mouse button is clicked (at which point the
     * sprite remains at its present location and this editor switches back
     * to the default behaviour).
     * <p>
     * TODO change arg to Sprite once Sprite supports repositioning
     * 
     * @param s The sprite to reposition
     */
    private void repositionSprite(Object s) {
        repositioningSprite = s;
        state = State.POSITIONING;
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    private void repositionFloatingSprite(Point2D p) {
        // TODO when Sprite interface gets position information, this can be much simplified.
        if (repositioningSprite instanceof LevelConfig.Switch) {
            ((LevelConfig.Switch) repositioningSprite).setPosition((int) p.getX(), (int) p.getY());
        } else if (repositioningSprite instanceof Robot) {
            ((Robot) repositioningSprite).setPosition(p);
        }
    }
    
    /**
     * Tells this editor that subsequent painting operations should use the
     * given square type.
     * 
     * @param sc The square type to paint with
     */
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
    
    /**
     * Returns a sprite on this playfield whose is position is
     * within 1/2 a square of the given point.
     * 
     * @param p The point, in playfield squares (not screen pixels)
     * @return An object near the given point, or null if there are no
     * objects near it.  If there are multiple objects near the given point,
     * the one which paints "on top" will be returned.
     */
    private Object getSpriteAt(Point2D p) {
        // TODO just iterate through the sprites of this playfield once Sprites support positioning
        for (Robot r : getLevel().getRobots()) {
            Point2D p1 = r.getPosition();
            double distance = Math.sqrt(Math.pow(p1.getX() - p.getX(), 2.0) + Math.pow(p1.getY() - p.getY(), 2.0));
            if (distance <= 0.5) {
                return r;
            }
        }
        for (Switch s : getLevel().getSwitches()) {
            Point2D p1 = s.getPosition();
            if (Math.floor(p1.getX()) == Math.floor(p.getX()) &&
                Math.floor(p1.getY()) == Math.floor(p.getY())) {
                return s;
            }
        }
        return null;
    }
}
