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
     * Get the Square Type
     */
    public char getType() {
        return type;
    }


    /**
     * @return
     */
    public boolean isOccupiable() {
        return getType() != WALL;
    }
}
