package net.bluecow.robot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Playfield
 */
public class Playfield extends JPanel {
    private PlayfieldModel pfm;
    private int squareWidth = 25;
    private ImageIcon goalIcon;
    private Robot robot;
    private ImageIcon blackIcon;
    
    private ImageIcon whiteIcon;

    private ImageIcon redIcon;

    private ImageIcon greenIcon;
    
    private ImageIcon blueIcon;
    
    private String winMessage;
    
    private Integer frameCount;
    
    private double robotIconScale = 0.4;

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
       this.robot = robot;
       setupKeyboardActions();
    }
    
    private void setupKeyboardActions() {
        getActionMap().put("grow_grod", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                setRobotIconScale(robotIconScale + 1.0);
            }
        });

        getActionMap().put("shrink_grod", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                setRobotIconScale(robotIconScale - 1.0);
            }
        });
        
        getInputMap().put(KeyStroke.getKeyStroke('='), "grow_grod");
        getInputMap().put(KeyStroke.getKeyStroke('+'), "grow_grod");
        getInputMap().put(KeyStroke.getKeyStroke('-'), "shrink_grod");
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
        ImageIcon icon = robot.getIcon();
        Point2D.Float roboPos = robot.getPosition();
        AffineTransform backupXform = g2.getTransform();
        g2.setTransform(AffineTransform.getTranslateInstance(
                (squareWidth * roboPos.x) - (icon.getIconWidth() * robotIconScale / 2.0),
                (squareWidth * roboPos.y) - (icon.getIconHeight() * robotIconScale / 2.0)));
        AffineTransform iconXform = new AffineTransform();
        iconXform.rotate(robot.getIconHeading(), icon.getIconWidth()*robotIconScale/2.0, icon.getIconHeight()*robotIconScale/2.0);
        iconXform.scale(robotIconScale, robotIconScale);
        g2.drawImage(icon.getImage(), iconXform, null);
        g2.setTransform(backupXform);
        
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

    public double getRobotIconScale() {
        return robotIconScale;
    }

    public void setRobotIconScale(double robotIconScale) {
        this.robotIconScale = robotIconScale;
        repaint();
    }
    
}
