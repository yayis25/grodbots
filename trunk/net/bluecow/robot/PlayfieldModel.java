package net.bluecow.robot;

public class PlayfieldModel {
    private Square[][] map;
    
    public PlayfieldModel(Square[][] map) {
        this.map = map;
    }
    
    public Square[][] getMap() {
        return map;
    }
    
    public Square getSquare(int x, int y) {
        return map[x][y];
    }
    
    public int getWidth() {
        return map.length;
    }

    public int getHeight() {
        return map[0].length;
    }
}
