package net.bluecow.robot;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

/**
 * Playfield
 */
public class Playfield extends JPanel {
    
    private class RoboStuff {
        private Robot robot;
        private Composite composite;
        
        public RoboStuff(Robot robot, Composite composite) {
            this.robot = robot;
            this.composite = composite;
        }

        public Robot getRobot() {
            return robot;
        }

        public Composite getComposite() {
            return composite;
        }
    }
    
    private LevelConfig level;
    
    private int squareWidth = 25;
    
    private String winMessage;
    
    private Integer frameCount;
    
    private List<RoboStuff> robots = new ArrayList<RoboStuff>();
    
    /**
     * Creates a new playfield with the specified map.
     * 
     * @param map The map.
     */
    public Playfield(LevelConfig level) {
        this.level = level;
        setupKeyboardActions();
        for (Robot r : level.getRobots()) {
            addRobot(r, AlphaComposite.SrcOver);
        }
    }
    
    /**
     * Adds the given robot to this playfield.  The robot will be drawn with
     * the specified composite operation.
     */
    public final void addRobot(Robot robot, Composite drawComposite) {
        robots.add(new RoboStuff(robot, drawComposite));
        repaint();
    }
    
    /**
     * Removes the given robot from this playfield.
     */
    public final void removeRobot(Robot robot) {
        for (Iterator<RoboStuff> it = robots.iterator(); it.hasNext(); ) {
            if (it.next().getRobot() == robot) {
                it.remove();
            }
        }
        repaint();
    }
    
    private void setupKeyboardActions() {
        // no actions right now
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Square[][] squares = level.getMap();
        for (int i = 0; i < squares.length; i++) {
            for (int j = 0; j < squares[0].length; j++) {
                Rectangle r = new Rectangle(i*squareWidth, j*squareWidth, squareWidth, squareWidth);
                if (squares[i][j] != null) {
                    squares[i][j].getSprite().paint(g2, r.x, r.y);
                } else {
                    g2.setColor(Color.red);
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.setColor(Color.white);
                    g2.drawString("null", r.x, r.y+10);
                }
                
            }
        }
        
        for (LevelConfig.Switch s : level.getSwitches()) {
            Point p = s.getLocation();
            s.getSprite().paint(g2, p.x*squareWidth, p.y*squareWidth);
        }
        
        Composite backupComposite = g2.getComposite();
        for (RoboStuff rs : robots) {
            Robot robot = rs.getRobot();
            g2.setComposite(rs.getComposite());

            Sprite sprite = robot.getSprite();
            Point2D.Float roboPos = robot.getPosition();
            AffineTransform backupXform = g2.getTransform();

            g2.translate(
                    (squareWidth * roboPos.x) - (sprite.getWidth() / 2.0),
                    (squareWidth * roboPos.y) - (sprite.getHeight() / 2.0));
            
            AffineTransform iconXform = new AffineTransform();
            iconXform.rotate(robot.getIconHeading(), sprite.getWidth()/2.0, sprite.getHeight()/2.0);
            g2.transform(iconXform);
            sprite.paint(g2, 0, 0);
            g2.setTransform(backupXform);
        }
        g2.setComposite(backupComposite);
        
        if (frameCount != null) {
            FontMetrics fm = getFontMetrics(getFont());
            String fc = String.format("%4d", frameCount);
            int width = fm.stringWidth(fc);
            int height = fm.getHeight();
            int x = getWidth() - width - 3;
            int y = 3;
            g2.setColor(Color.BLACK);
            g2.fillRect(x, y, width, height);
            g2.setColor(Color.WHITE);
            g2.drawString(fc, x, y + height - fm.getDescent());
        }
        
        if (winMessage != null) {
            g2.setFont(g2.getFont().deriveFont(50f));
            g2.setColor(Color.BLACK);
            g2.drawString(winMessage, 20, getHeight()/2);
            g2.setColor(Color.RED);
            g2.drawString(winMessage, 15, getHeight()/2-5);
        }
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(level.getWidth() * getSquareWidth(),
                			 level.getHeight() * getSquareWidth());
    }
    
    // ACCESSORS AND MUTATORS
    
    public int getSquareWidth() {
        return squareWidth;
    }

    public void setSquareWidth(int squareWidth) {
        this.squareWidth = squareWidth;
    }
    
    public Square getSquareAt(Point p) {
        return level.getSquare(p.x, p.y);
    }

    public Square getSquareAt(Point2D.Float p) {
        return level.getSquare(p.x, p.y);
    }
    
    public void setWinMessage(String m) {
        winMessage = m;
        repaint();
    }
    
    public void setFrameCount(Integer c) {
        frameCount = c;
    }

    /**
     * Returns the LevelConfig that determines this playfield's configuration.
     */
    public LevelConfig getLevel() {
        return level;
    }
}
