package net.bluecow.robot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Playfield
 */
public class Playfield extends JPanel {
    private PlayfieldModel pfm;
    private int squareWidth = 30;
    private ImageIcon goalIcon;
    private Robot robot;
    
    /**
     * Creates a playfield of spaces (mainly for testing).
     * 
     * @param x Width (in squares)
     * @param y Height (in squares)
     */
    public Playfield(int x, int y) {
        Square[][] squares = new Square[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                squares[i][j] = new Square(Square.EMPTY);
            }
        }
        pfm = new PlayfieldModel(squares, "Test Playfield", new Point2D.Float(0.5f, 0.5f), 0.1f);
    }
    
    /**
     * Creates a new playfield with the specified map.
     * 
     * @param map The map.
     */
    public Playfield(PlayfieldModel model, Robot robot) {
       this.pfm = model;
       this.robot = robot;
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Square[][] squares = pfm.getMap();
        for (int i = 0; i < squares.length; i++) {
            for (int j = 0; j < squares[0].length; j++) {
                Rectangle r = new Rectangle(i*squareWidth, j*squareWidth, squareWidth, squareWidth);
                if (squares[i][j].getType() == Square.EMPTY) {
                    Color c = Color.white;
                    g2.setColor(c);
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.setColor(c.darker());
                    g2.draw(r);
                } else if (squares[i][j].getType() == Square.WALL) {
                    Color c = Color.darkGray;
                    g2.setColor(c);
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.setColor(Color.lightGray);
                    g2.draw(r);
                } else if (squares[i][j].getType() == Square.RED) {
                    Color c = new Color(255, 160, 160);
                    g2.setColor(c);
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.setColor(c.darker());
                    g2.draw(r);
                } else if (squares[i][j].getType() == Square.GREEN) {
                    Color c = new Color(160, 255, 160);
                    g2.setColor(c);
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.setColor(c.darker());
                    g2.draw(r);
                } else if (squares[i][j].getType() == Square.BLUE) {
                    Color c = new Color(160, 160, 255);
                    g2.setColor(c);
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.setColor(c.darker());
                    g2.draw(r);
                } else if (squares[i][j].getType() == Square.GOAL) {
                    goalIcon.paintIcon(this, g2, r.x+1, r.y+1);
                    g2.setColor(Color.darkGray);
                    g2.draw(r);
                } else {
                    g2.setColor(Color.red);
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.setColor(Color.white);
                    g2.drawString("BAD: "+squares[i][j].getType(), r.x, r.y+10);
                }
                
            }
        }
        Point2D.Float roboPos = robot.getPosition();
        robot.getIcon().paintIcon(this, g2,
                (int) (squareWidth * roboPos.x) - robot.getIcon().getIconWidth() / 2,
                (int) (squareWidth * roboPos.y) - robot.getIcon().getIconHeight() / 2);
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(pfm.getWidth() * getSquareWidth(),
                			 pfm.getHeight() * getSquareWidth());
    }
    
    // ACCESSORS AND MUTATORS
    
    public ImageIcon getGoalIcon() {
        return goalIcon;
    }

    public void setGoalIcon(ImageIcon goalIcon) {
        this.goalIcon = goalIcon;
    }

    public int getSquareWidth() {
        return squareWidth;
    }

    public void setSquareWidth(int squareWidth) {
        this.squareWidth = squareWidth;
    }
    
    public Square getSquareAt(Point p) {
        return pfm.getSquare(p.x, p.y);
    }

    public Square getSquareAt(Point2D.Float p) {
        return pfm.getSquare(p.x, p.y);
    }
}
