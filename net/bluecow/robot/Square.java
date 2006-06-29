package net.bluecow.robot;

import java.util.Collection;

import net.bluecow.robot.GameConfig.SensorConfig;
import net.bluecow.robot.sprite.Sprite;

/**
 * Represents a square in the playfield.
 */
public interface Square {

    /**
     * @return true iff this square can be occupied by the robot.
     */
    public boolean isOccupiable();

    /**
     * Returns the character that should be used to represent this square
     * in an ASCII rendition of a level map.
     */
    public char getMapChar();
    
    /**
     * Returns this square's graphical representation.
     */
    public Sprite getSprite();
    
    /**
     * Returns the types of sensors that are activated by stepping onto this
     * square.
     */
    public Collection<SensorConfig> getSensorTypes();

}
