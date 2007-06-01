/*
 * Created on Aug 21, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

/**
 * The Direction enum represents a compass direction.  It was originally
 * created to specify label directions relative to the items they
 *
 * @author fuerth
 * @version $Id$
 */
public enum Direction {
    
    NORTH("n"),
    NORTHEAST("ne"),
    EAST("e"),
    SOUTHEAST("se"),
    SOUTH("s"),
    SOUTHWEST("sw"),
    WEST("w"),
    NORTHWEST("nw");

    private String code;
    
    Direction(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    /**
     * Returns the direction associated with the code.
     * 
     * @param code A string of the form "n", "nw", "ne", "s", etc.  Null is not allowed.
     * @return The Direction instance associated with the code.
     * @throws NullPointerException if code is null.
     * @throws IllegalArgumentException if code is not recognised as one of the 8 cardinal
     * directions.
     */
    public static Direction get(String code) {
        code = code.toLowerCase();
        for (Direction dir : values()) {
            if (dir.getCode().equals(code)) {
                return dir;
            }
        }
        throw new IllegalArgumentException("Unknown direction code '"+code+"'");
    }
}
