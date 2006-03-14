package net.bluecow.robot;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;

/**
 * The PlayfieldModel represents the robot's environment and its parameters for
 * a particular level of the game.
 * 
 * <p>Instances of this class are immutable.
 *
 * @author fuerth
 * @version $Id$
 */
public class PlayfieldModel {
    
    /**
     * The map of squares that the robot lives in.
     */
    private Square[][] map;
    
    /**
     * The name of this level.
     */
    private String name;
    
    /**
     * The robot's starting position in this level.
     */
    private Point2D.Float startPosition;
    
    /**
     * The size of a robot step, in squares.
     */
    private float stepSize;
    
    /**
     * Creates a new playfield with the given parameters.
     */
    public PlayfieldModel(Square[][] map, String name, Float startPosition, float stepSize) {
        this.map = map;
        this.name = name;
        this.startPosition = startPosition;
        this.stepSize = stepSize;
    }
    
    public Square[][] getMap() {
        return map.clone();
    }
    
    public Square getSquare(int x, int y) {
        return map[x][y];
    }

    public Square getSquare(float x, float y) {
        return getSquare((int) x, (int) y);
    }

    public int getWidth() {
        return map.length;
    }

    public int getHeight() {
        return map[0].length;
    }

    public String getName() {
        return name;
    }

    public Point2D.Float getStartPosition() {
        return new Point2D.Float(startPosition.x, startPosition.y);
    }

    public float getStepSize() {
        return stepSize;
    }
}
