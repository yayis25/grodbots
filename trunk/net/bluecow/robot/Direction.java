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
    NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST;

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
        if (code.equals("n")) return NORTH;
        if (code.equals("ne")) return NORTHEAST;
        if (code.equals("e")) return EAST;
        if (code.equals("se")) return SOUTHEAST;
        if (code.equals("s")) return SOUTH;
        if (code.equals("sw")) return SOUTHWEST;
        if (code.equals("w")) return WEST;
        if (code.equals("nw")) return NORTHWEST;
        throw new IllegalArgumentException("Unknown direction code '"+code+"'");
    }
}
