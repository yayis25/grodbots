package net.bluecow.robot;

/**
 * Represents a square in the playfield.
 */
public class Square {
    public static final char EMPTY = ' ';
    public static final char WALL  = 'X';
    public static final char RED   = 'R';
    public static final char GREEN = 'G';
    public static final char BLUE  = 'B';
    public static final char GOAL  = '@';
    
    private char type;
    
    public Square(char type) {
        this.type = type;
    }

    /**
     * Returns the Square Type.
     */
    public char getType() {
        return type;
    }

    /**
     * @return true iff this square can be occupied by the robot.
     */
    public boolean isOccupiable() {
        return getType() != WALL;
    }

    /**
     * Returns true iff this square is the goal.
     */
    public boolean isGoal() {
        return getType() == GOAL;
    }
}
