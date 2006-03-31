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

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Playfield
 */
public class Playfield extends JPanel {
    
    private class RoboStuff {
        private Robot robot;
        private double iconScale = 0.4;
        private Composite composite;
        
        public RoboStuff(Robot robot, double iconScale, Composite composite) {
            this.robot = robot;
            this.iconScale = iconScale;
            this.composite = composite;
        }

        public double getIconScale() {
            return iconScale;
        }

        public Robot getRobot() {
            return robot;
        }

        public Composite getComposite() {
            return composite;
        }
    }
    
    private PlayfieldModel pfm;
    
    private int squareWidth = 25;
    
    private ImageIcon goalIcon;

    private ImageIcon blackIcon;
    
    private ImageIcon whiteIcon;

    private ImageIcon redIcon;

    private ImageIcon greenIcon;
    
    private ImageIcon blueIcon;
    
    private String winMessage;
    
    private Integer frameCount;
    
    private List<RoboStuff> robots = new ArrayList<RoboStuff>();
    
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
        setupKeyboardActions();
    }
    
    /**
     * Creates a new playfield with the specified map.
     * 
     * @param map The map.
     */
    public Playfield(PlayfieldModel model, Robot robot) {
       this.pfm = model;
       setupKeyboardActions();
       addRobot(robot, AlphaComposite.SrcOver);
    }
    
    /**
     * Adds the given robot to this playfield.  The robot will be drawn with
     * the specified composite operation.
     */
    public final void addRobot(Robot robot, Composite drawComposite) {
        robots.add(new RoboStuff(robot, 0.4, drawComposite));
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
        Square[][] squares = pfm.getMap();
        for (int i = 0; i < squares.length; i++) {
            for (int j = 0; j < squares[0].length; j++) {
                Rectangle r = new Rectangle(i*squareWidth, j*squareWidth, squareWidth, squareWidth);
                if (squares[i][j].getType() == Square.EMPTY) {
                    whiteIcon.paintIcon(this, g2, r.x, r.y);
                } else if (squares[i][j].getType() == Square.WALL) {
                    blackIcon.paintIcon(this, g2, r.x, r.y);
                } else if (squares[i][j].getType() == Square.RED) {
                    redIcon.paintIcon(this, g2, r.x, r.y);
                } else if (squares[i][j].getType() == Square.GREEN) {
                    greenIcon.paintIcon(this, g2, r.x, r.y);
                } else if (squares[i][j].getType() == Square.BLUE) {
                    blueIcon.paintIcon(this, g2, r.x, r.y);
                } else if (squares[i][j].getType() == Square.GOAL) {
                    whiteIcon.paintIcon(this, g2, r.x, r.y);
                    goalIcon.paintIcon(this, g2, r.x+1, r.y+1);
                } else {
                    g2.setColor(Color.red);
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.setColor(Color.white);
                    g2.drawString("BAD: "+squares[i][j].getType(), r.x, r.y+10);
                }
                
            }
        }
        
        Composite backupComposite = g2.getComposite();
        for (RoboStuff rs : robots) {
            Robot robot = rs.getRobot();
            double iconScale = rs.getIconScale();
            g2.setComposite(rs.getComposite());
            
            ImageIcon icon = robot.getIcon();
            Point2D.Float roboPos = robot.getPosition();
            AffineTransform backupXform = g2.getTransform();

            g2.translate(
                    (squareWidth * roboPos.x) - (icon.getIconWidth() * iconScale / 2.0),
                    (squareWidth * roboPos.y) - (icon.getIconHeight() * iconScale / 2.0));
            
            AffineTransform iconXform = new AffineTransform();
            iconXform.rotate(robot.getIconHeading(), icon.getIconWidth()*iconScale/2.0, icon.getIconHeight()*iconScale/2.0);
            iconXform.scale(iconScale, iconScale);
            g2.drawImage(icon.getImage(), iconXform, null);
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

    public ImageIcon getBlackIcon() {
        return blackIcon;
    }

    public void setBlackIcon(ImageIcon blackIcon) {
        this.blackIcon = blackIcon;
    }

    public ImageIcon getBlueIcon() {
        return blueIcon;
    }

    public void setBlueIcon(ImageIcon blueIcon) {
        this.blueIcon = blueIcon;
    }

    public ImageIcon getGreenIcon() {
        return greenIcon;
    }

    public void setGreenIcon(ImageIcon greenIcon) {
        this.greenIcon = greenIcon;
    }

    public ImageIcon getRedIcon() {
        return redIcon;
    }

    public void setRedIcon(ImageIcon redIcon) {
        this.redIcon = redIcon;
    }

    public ImageIcon getWhiteIcon() {
        return whiteIcon;
    }

    public void setWhiteIcon(ImageIcon whiteIcon) {
        this.whiteIcon = whiteIcon;
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
    
    public PlayfieldModel getModel() {
        return pfm;
    }

    public void setWinMessage(String m) {
        winMessage = m;
        repaint();
    }
    
    public void setFrameCount(Integer c) {
        frameCount = c;
    }
}
