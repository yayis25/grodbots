/*
 * Copyright (c) 2007, Jonathan Fuerth
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Jonathan Fuerth nor the names of other
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.bluecow.robot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import net.bluecow.robot.GameConfig.SquareConfig;
import net.bluecow.robot.LevelConfig.Switch;
import net.bluecow.robot.fx.Effect;
import net.bluecow.robot.sprite.AnimatedSprite;
import net.bluecow.robot.sprite.Sprite;
import net.bluecow.robot.sprite.SpriteLoadException;
import net.bluecow.robot.sprite.SpriteManager;

/**
 * Playfield
 */
public class Playfield extends JPanel {
    
    /**
     * Controls debugging features of this class, including whether or not
     * it prints debug information to the console.
     */
    private static final boolean debugOn = false;
    
    /**
     * Prints the given string plus a newline to stdout, if debugOn==true. 
     */
    private static void debug(String msg) {
        if (debugOn) System.out.println(msg);
    }
    
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
    
    /**
     * The config for the whole game (currently used only to find out
     * the total score).
     */
    private GameConfig game;
    
    /**
     * The level this playfield is currently displaying.  Must belong to {@link #game}.
     */
    private LevelConfig level;
    
    private int squareWidth = 25;
    
    private String winMessage;
    
    private Integer frameCount;
    
    /**
     * All of the effects that are currently active. To start a new effect, just
     * add it to this list. When effects are finished, the nextFrame() method
     * will remove them from this list.
     */
    private List<Effect> effects = new ArrayList<Effect>();
    
    /**
     * All the robots in this playfield.  Includes ghosts as well as the robots
     * that are actually part of the current level.
     */
    private List<RoboStuff> robots;

    /**
     * Controls whether or not labels will be displayed by fading the opacity
     * toward zero when false and toward one when true.
     */
    private boolean labellingOn = true;
    
    /**
     * The opacity of a label.  Fades up and down in nextFrame() according to whether
     * or not labellingOn is set.
     */
    private float labelOpacity = 1.0f;
    
    /**
     * The amount that the label opacity fades up or down per frame.
     */
    private float labelFadeStep = 0.1f;

    /**
     * Controls whether or not the level description will be displayed by fading the opacity
     * toward zero when false and toward one when true.
     */
    private boolean descriptionOn = true;

    /**
     * The opacity of the level description.  Fades up and down in nextFrame()
     * according to whether or not labellingOn is set.
     */
    private float descriptionOpacity = 1.0f;
    
    /**
     * Controls whether or not a mouse click on this component toggles the
     * {@link #descriptionOn} flag.
     */
    private boolean clickToToggleDescription = true;

    /**
     * The "hot zone" for the button that goes to the previous description page.
     * Gets initialized to a reasonable value in the constructor.
     */
    private Rectangle prevDescriptionPageRegion;
    
    /**
     * The visual appearance of the "previous page" button.  If sprites supported
     * location as well as size, we wouldn't need the rectangle in addition
     * to this sprite.
     */
    private Sprite prevDescriptionPageSprite;
    
    /**
     * The "hot zone" for the button that goes to the next description page.
     * Gets initialized to a reasonable value in the constructor.
     */
    private Rectangle nextDescriptionPageRegion;
    
    /**
     * The visual appearance of the "next page" button.  If sprites supported
     * location as well as size, we wouldn't need the rectangle in addition
     * to this sprite.
     */
    private Sprite nextDescriptionPageSprite;

    /**
     * The colour that drawLabel() will use to paint the box underneath labels.
     */
    private Color boxColor = new Color(.5f, .5f, .5f, .5f);

    /**
     * The colour that drawLabel() will use to paint the text of labels.
     */
    private Color labelColor = Color.WHITE;

    /**
     * The number of milliseconds to delay between frames when the async repaint
     * manager is repainting this playfield.
     */
    private int frameDelay = 50;

    /**
     * The location, in squares, of the spotlight.
     */
    private Point2D spotlightLocation;
    
    /**
     * The radius, in squares, of the spotlight.
     */
    private double spotlightRadius;
    
    /**
     * The current page number of the HTML description text to display.  See
     * {@link #getDescriptionPage(int)}.
     */
    private int descriptionPageNumber;
    
    /**
     * Creates a new playfield with the specified map.
     * 
     * @param map The map.
     * @throws SpriteLoadException If the skin resources can't be loaded.
     */
    public Playfield(GameConfig game, LevelConfig level) throws SpriteLoadException {
        debug("Creating new Playfield "+System.identityHashCode(this)+" for level "+level.getName());
        setGame(game);
        setLevel(level);
        setupKeyboardActions();
        addMouseListener(new MouseAdapter() {
            boolean descriptionState = isDescriptionOn();
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (isDescriptionOn() && prevDescriptionPageRegion != null && prevDescriptionPageRegion.contains(p)) {
                    switchToPrevPage();
                } else if (isDescriptionOn() && nextDescriptionPageRegion != null && nextDescriptionPageRegion.contains(p)) {
                    switchToNextPage();
                } else if (isClickToToggleDescription()) {
                    descriptionState = !descriptionState;
                    setDescriptionOn(descriptionState);
                }
            }
        });
        
        setBackground(Color.BLACK);
        
        // set up the default location for the description page flipper buttons
        prevDescriptionPageSprite = SpriteManager.load(game.getResourceLoader(), "ROBO-INF/skin/prev_page_button.png");
        nextDescriptionPageSprite = SpriteManager.load(game.getResourceLoader(), "ROBO-INF/skin/next_page_button.png");
        int x = (level.getWidth() * getSquareWidth())
                - (int) ((prevDescriptionPageSprite.getWidth() + nextDescriptionPageSprite.getWidth()) * 1.5);
        int y = getSquareWidth();
        prevDescriptionPageRegion = new Rectangle(x, y, prevDescriptionPageSprite.getWidth(), prevDescriptionPageSprite.getHeight());
        x += prevDescriptionPageRegion.width;
        nextDescriptionPageRegion = new Rectangle(x, y, nextDescriptionPageSprite.getWidth(), nextDescriptionPageSprite.getHeight());
    }

    public final void setGame(GameConfig game) {
        this.game = game;
    }
    
    /**
     * Sets the value of level to the given level, and resets the list of robostuff
     * to contain only the robots described in the level.
     *
     * @param level
     */
    public final void setLevel(LevelConfig level) {
        robots = new ArrayList<RoboStuff>();
        this.level = level;
        for (Robot r : level.getRobots()) {
            addRobot(r);
        }
        
        effects.clear();
        
        level.addPropertyChangeListener("map", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getSource() == Playfield.this.level) {
                    revalidate();
                }
            }
        });
    }
    
    /**
     * Adds a robot which will be rendered using the default
     * composite (fully opaque).
     * 
     * @param robot The robot to add.
     */
    public final void addRobot(Robot robot) {
        addRobot(robot, null);
    }
    
    /**
     * Adds the given robot to this playfield.  The robot does not have to be
     * part of the level associated with this playfield (for instance, it could
     * be a ghost).
     * 
     * @param robot The robot to add.
     * @param drawComposite The robot will be drawn with
     * this composite operation.  Null means normal compositing.
     */
    public final void addRobot(Robot robot, Composite drawComposite) {
        robots.add(new RoboStuff(robot, drawComposite));
        repaint();
    }
    
    /**
     * Removes the given robot from this playfield.
     */
    public void removeRobot(Robot robot) {
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
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2 = (Graphics2D) g.create();
        FontMetrics fm = getFontMetrics(getFont());
        
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
            Point p = s.getPosition();
            s.getSprite().paint(g2, p.x*squareWidth, p.y*squareWidth);
            if (!s.isEnabled()) {
                g2.setColor(Color.RED);
                int x = p.x*squareWidth;
                int y = p.y*squareWidth;
                g2.drawLine(x, y, x+squareWidth, y+squareWidth);
                g2.drawLine(x, y+squareWidth, x+squareWidth, y);
            }
        }
        
        Composite backupComposite = g2.getComposite();
        for (RoboStuff rs : robots) {
            Robot robot = rs.getRobot();
            if (rs.getComposite() != null) {
                g2.setComposite(rs.getComposite());
            } else {
                g2.setComposite(AlphaComposite.SrcOver);
            }
            
            Sprite sprite = robot.getSprite();
            Point2D roboPos = robot.getPosition();
            AffineTransform backupXform = g2.getTransform();

            g2.translate(
                    (squareWidth * roboPos.getX()) - (sprite.getWidth() / 2.0),
                    (squareWidth * roboPos.getY()) - (sprite.getHeight() / 2.0));
            
            AffineTransform iconXform = new AffineTransform();
            iconXform.rotate(robot.getIconHeading(), sprite.getWidth()/2.0, sprite.getHeight()/2.0);
            g2.transform(iconXform);
            sprite.paint(g2, 0, 0);
            g2.setTransform(backupXform);
        }
        g2.setComposite(backupComposite);
        
        for (Effect e : effects) {
            Graphics2D effectsGraphics = (Graphics2D) g2.create();
            e.paint(effectsGraphics);
            effectsGraphics.dispose();
        }
        
        if (spotlightLocation != null) {
            // XXX I wanted to dim everything but the robot, but couldn't
            //     get it working.  This is just a temporary workaround.
            //     I think what would solve it would be to draw everything
            //     into a bufferedimage with an alpha channel and then blit
            //     it onto the component at the end of this method.  It would
            //     make sense to turn off double buffering on this component
            //     in that case.
            Graphics2D gg = (Graphics2D) g2.create();
            gg.setStroke(new BasicStroke(7));
            gg.setColor(Color.RED);
            gg.drawOval(
                    (int) (spotlightLocation.getX()*squareWidth - spotlightRadius*squareWidth),
                    (int) (spotlightLocation.getY()*squareWidth - spotlightRadius*squareWidth),
                    (int) (spotlightRadius*squareWidth*2),
                    (int) (spotlightRadius*squareWidth*2));
        }
        
        if (frameCount != null) {
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

        if (labelOpacity > 0.0) {
            for (RoboStuff rs : robots) {
                Robot robot = rs.getRobot();
                if (robot.isLabelEnabled()) {
                    drawLabel(g2, fm, robot, robot.getPosition());
                }
            }
            
            for (Switch s : level.getSwitches()) {
                if (s.isLabelEnabled()) {
                    drawLabel(g2, fm, s, s.getPosition());
                }
            }
        }
        
        if (winMessage != null) {
            Font oldFont = g2.getFont();
            g2.setFont(oldFont.deriveFont(50f));
            g2.setColor(Color.BLACK);
            g2.drawString(winMessage, 20, getHeight()/2);
            g2.setColor(Color.RED);
            g2.drawString(winMessage, 15, getHeight()/2-5);
            g2.setFont(oldFont);
        }
        
        final List<String> pages = level.getDescriptionPages();
        if ( (pages != null) && (pages.size() > getDescriptionPageNumber()) && (descriptionOpacity > 0.0f) ) {
            final String descText = pages.get(getDescriptionPageNumber());
            backupComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, descriptionOpacity));
            Rectangle labelBounds = new Rectangle(
                    (int) (getWidth() * 0.05), (int) (getHeight() * 0.05),
                    (int) (getWidth() * 0.9), (int) (getHeight() * 0.9));
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(
                    labelBounds.x, labelBounds.y,
                    labelBounds.width, labelBounds.height,
                    10, 10);
            
            String htmlDescription =
                "<html>" +
                "<head><style>p {margin-top: 5px}</style></head>" +
                "<body><h1>"+level.getName()+"</h1>" +
                "<p>" + descText +
                "</body></html>";
            
            JLabel descriptionLabel = new JLabel(htmlDescription);
            descriptionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            descriptionLabel.setForeground(Color.WHITE);
            descriptionLabel.setBackground(Color.RED);
            descriptionLabel.setOpaque(false);
            descriptionLabel.setVerticalAlignment(SwingConstants.TOP);
            descriptionLabel.setBounds(0, 0, labelBounds.width, labelBounds.height);
            Graphics labelGraphics = g2.create(
                    labelBounds.x, labelBounds.y,
                    labelBounds.width, labelBounds.height);
            descriptionLabel.paint(labelGraphics);
            labelGraphics.dispose();

            
            // Page flipper
            if (pages.size() > 1) {
                g2.setColor(Color.RED);
                if (prevDescriptionPageSprite != null) {
                    prevDescriptionPageSprite.paint(
                            g2, prevDescriptionPageRegion.x, prevDescriptionPageRegion.y);
                }
                if (nextDescriptionPageSprite != null) {
                    nextDescriptionPageSprite.paint(
                            g2, nextDescriptionPageRegion.x, nextDescriptionPageRegion.y);
                }

                g2.setColor(Color.WHITE);
                
                g2.drawString("Page "+ (getDescriptionPageNumber() + 1) +
                        "/" + level.getDescriptionPages().size(),
                        prevDescriptionPageRegion.x,
                        prevDescriptionPageRegion.y + prevDescriptionPageRegion.height + fm.getHeight());
            }
            
            g2.setComposite(backupComposite);
        }
    }

    private int getDescriptionPageNumber() {
        return descriptionPageNumber;
    }

    /**
     * Adds the given effect, which will be painted every frame until
     * it says it is finished.  Once it is finished, it will automatically
     * be removed from this playfield.
     */
    public void addEffect(Effect e) {
        debug("Adding effect "+e);
        effects.add(e);
    }
    
    /**
     * Tells all the sprites and effects to get ready for the next frame.
     * 
     * @param inPlay Whether or not this next frame happened while the
     * game was in play. If so, the robot sprites will look like they took
     * a step. Progression of other sprite animations will continue regardless
     * of this parameter.
     */
    private void nextFrame(boolean inPlay) {
        for (SquareConfig sc : game.getSquareTypes()) {
            sc.getSprite().nextFrame();
        }
        
        for (LevelConfig.Switch s : level.getSwitches()) {
            s.getSprite().nextFrame();
        }

        for (RoboStuff rs : robots) {
            // XXX I don't like this for two reasons:
            // 1. The robot should probably be doing this itself
            // 2. I don't think there should be an instanceof check here
            Robot robot = rs.getRobot();
            Sprite sprite = robot.getSprite();
            if (robot.isMoving()) {
                if (sprite instanceof AnimatedSprite) {
                    ((AnimatedSprite) sprite).setCurrentSequence("march");
                }
                if (inPlay) {
                    sprite.nextFrame();
                }
            } else {
                if (sprite instanceof AnimatedSprite) {
                    ((AnimatedSprite) sprite).setCurrentSequence("idle");
                }
            }
        }

        for (Iterator<Effect> effectsIt = effects.iterator(); effectsIt.hasNext(); ) {
            Effect e = effectsIt.next();
            
            if (e.isFinished()) {
                debug("---- REMOVING EFFECT "+e);
                effectsIt.remove();
            } else {
                debug("Next Frame for effect "+e);
                e.nextFrame();
            }
        }
        
        if (labellingOn) {
            labelOpacity = (float) Math.min(1.0, labelOpacity + labelFadeStep);
        } else {
            labelOpacity = (float) Math.max(0.0, labelOpacity - labelFadeStep);
        }

        if (descriptionOn) {
            descriptionOpacity = (float) Math.min(1.0, descriptionOpacity + labelFadeStep);
        } else {
            descriptionOpacity = (float) Math.max(0.0, descriptionOpacity - labelFadeStep);
        }
    }

    /**
     * Draws a label for the centre of the square at the given position.
     */
    private void drawLabel(Graphics2D g2, FontMetrics fm, Labelable labelable, Point position) {
        drawLabel(g2, fm, labelable, new Point2D.Float(position.x + 0.5f, position.y + 0.5f));
    }
    
    /**
     * Draws a label over a background box with an arrow to given square position.
     * 
     * <p>See also {@link #squareWidth}, {@link #boxColor}, and {@link #labelColor}.
     * 
     * @param position The map position.  This is not a screen coordinate; it's
     * a map location.  For example, the point (x,y) = (3.5, 2.5) is at the screen
     * position (3.5*squareWidth, 2.5*squareWidth).
     */
    private void drawLabel(Graphics2D g2, FontMetrics fm, Labelable labelable, Point2D position) {
        String label = labelable.getLabel();
        if (label == null) return;
        int x = (int) (position.getX() * squareWidth);
        int y = (int) (position.getY() * squareWidth);
        Composite backupComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, labelOpacity));
        g2.setColor(boxColor);
        GeneralPath arrowPath = new GeneralPath();
        
        final int arrowLength = 10;  // the distance from the tip of the arrow to its base

        // this arrow points north
        arrowPath.moveTo(0, 0);
        arrowPath.lineTo(-10, 5);
        arrowPath.lineTo(-7, 5);
        arrowPath.lineTo(-7, arrowLength*2);  // the *2 is to make the stem long enough to
        arrowPath.lineTo(7, arrowLength*2);   // properly meet the diagonal boxes.  It makes
        arrowPath.lineTo(7, 5);               // no difference to the n, s, e, w cases.
        arrowPath.lineTo(10, 5);
        arrowPath.lineTo(0, 0);
        Area arrowBox = new Area(arrowPath);
        
        Dimension boxSize = new Dimension(
                fm.stringWidth(label) + fm.getHeight()*2,
                fm.getHeight()*2);
        RoundRectangle2D.Double box = 
            new RoundRectangle2D.Double(0, 0, boxSize.width, boxSize.height, 4, 4);
        
        // three cases: corner, top/bottom, left/right
        Direction dir = labelable.getLabelDirection();
        if (dir == Direction.EAST) {
            arrowBox.transform(AffineTransform.getRotateInstance(-Math.PI / 2.0));
            arrowBox.transform(AffineTransform.getTranslateInstance(x, y));
            box.x = x + arrowLength;
            box.y = y - box.height / 2;
            arrowBox.add(new Area(box));
        } else if (dir == Direction.WEST) {
            arrowBox.transform(AffineTransform.getRotateInstance(Math.PI / 2.0));
            arrowBox.transform(AffineTransform.getTranslateInstance(x, y));
            box.x = x - arrowLength - box.width;
            box.y = y - box.height / 2;
            arrowBox.add(new Area(box));
        } else if (dir == Direction.NORTH) {
            arrowBox.transform(AffineTransform.getRotateInstance(Math.PI));
            arrowBox.transform(AffineTransform.getTranslateInstance(x, y));
            box.x = x - box.width / 2;
            box.y = y - box.height - arrowLength;
            arrowBox.add(new Area(box));
        } else if (dir == Direction.SOUTH) {
            arrowBox.transform(AffineTransform.getRotateInstance(0.0));
            arrowBox.transform(AffineTransform.getTranslateInstance(x, y));
            box.x = x - box.width / 2;
            box.y = y + arrowLength;
            arrowBox.add(new Area(box));
        } else if (dir == Direction.NORTHEAST) {
            arrowBox.transform(AffineTransform.getRotateInstance(-3.0 * Math.PI / 4.0));
            arrowBox.transform(AffineTransform.getTranslateInstance(x, y));
            box.x = x + arrowLength/3;
            box.y = y - box.height - arrowLength/3;
            arrowBox.add(new Area(box));
        } else if (dir == Direction.SOUTHEAST) {
            arrowBox.transform(AffineTransform.getRotateInstance(-Math.PI / 4.0));
            arrowBox.transform(AffineTransform.getTranslateInstance(x, y));
            box.x = x + arrowLength/3;
            box.y = y + arrowLength/3;
            arrowBox.add(new Area(box));
        } else if (dir == Direction.NORTHWEST) {
            arrowBox.transform(AffineTransform.getRotateInstance(3.0 * Math.PI / 4.0));
            arrowBox.transform(AffineTransform.getTranslateInstance(x, y));
            box.x = x - box.width - arrowLength/3;
            box.y = y - box.height - arrowLength/3;
            arrowBox.add(new Area(box));
        } else if (dir == Direction.SOUTHWEST) {
            arrowBox.transform(AffineTransform.getRotateInstance(Math.PI / 4.0));
            arrowBox.transform(AffineTransform.getTranslateInstance(x, y));
            box.x = x - box.width - arrowLength/3;
            box.y = y + arrowLength/3;
            arrowBox.add(new Area(box));
        } else {
            throw new IllegalStateException("Unknown label direction "+dir);
        }
        
        g2.fill(arrowBox);
        
        g2.setColor(labelColor);
        g2.draw(arrowBox);
        g2.drawString(label, (int) (box.x + fm.getHeight()), (int) (box.y + fm.getHeight()/2 + fm.getAscent()));
        g2.setComposite(backupComposite);
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
    
    /**
     * Converts the given Point2D (in square coordinates) to the equivalent Point
     * on this playfield in pixel coordinates.
     * 
     * @param sp The point to convert.
     * @return a new Point object that is as close as possible to the given square coordinate.
     */
    public Point squarePositionToPoint(Point2D sp) {
        return new Point(
                (int) (sp.getX() * getSquareWidth()),
                (int) (sp.getY() * getSquareWidth()));
    }
    
    public void setWinMessage(String m) {
        winMessage = m;
        repaint();
    }
    
    public void setFrameCount(Integer c) {
        debug("===="+System.identityHashCode(this)+" SetFrameCount Next Frame");
        boolean inPlay = (frameCount != c);
        frameCount = c;
        nextFrame(inPlay);
    }
    
    /**
     * Returns the LevelConfig that determines this playfield's configuration.
     */
    public LevelConfig getLevel() {
        return level;
    }
    
    public boolean isLabellingOn() {
        return labellingOn;
    }
    
    public void setLabellingOn(boolean labellingOn) {
        this.labellingOn = labellingOn;
    }
    
    /**
     * This flag controls the asynchronous versus synchronous repaint mode of
     * the playfield.  When it is in synchronous mode, outside code has to trigger
     * each repaint when it wants a new frame.  When in asynchronous mode, the
     * playfield will repaint itself periodically if necessary (for example, because
     * the labels are still in the process of fading in or out).
     * 
     * <p>As a rule of thumb, this flag should be set <tt>true</tt> when the game
     * is under the control of the Swing UI, and <tt>false</tt> when the game loop
     * is controlling all the repaints.
     */
    public void setAsyncRepaint(boolean asyncRepaint) {
        repaintManager.setEnabled(asyncRepaint);
    }

    private AsyncRepaintManager repaintManager = new AsyncRepaintManager(frameDelay);
    
    private class AsyncRepaintManager implements ActionListener, AncestorListener {
        private boolean enabled = true;
        private final Timer timer;
        
        AsyncRepaintManager(int delay) {
            Playfield.this.addAncestorListener(this);
            timer = new Timer(delay, this);
            timer.start();
        }

        public synchronized void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public synchronized void actionPerformed(ActionEvent e) {
            if (enabled) {
                debug("===="+System.identityHashCode(Playfield.this)+" Async Next Frame");
                nextFrame(false);
                repaint();
            }
        }

        
        // AncestorListener implementation:
        //  kills the timer when this component goes away
        //  and reactivates it when this component is reattached

        public void ancestorAdded(AncestorEvent event) {
            if (!timer.isRunning()) timer.start();
        }

        public void ancestorRemoved(AncestorEvent event) {
            timer.stop();
        }

        public void ancestorMoved(AncestorEvent event) {
            // don't care
        }
    }

    public Point2D getSpotlightLocation() {
        return spotlightLocation;
    }

    public void setSpotlightLocation(Point2D spotlightLocation) {
        this.spotlightLocation = spotlightLocation;
    }

    public double getSpotlightRadius() {
        return spotlightRadius;
    }

    public void setSpotlightRadius(double spotlightRadius) {
        this.spotlightRadius = spotlightRadius;
    }

    public boolean isDescriptionOn() {
        return descriptionOn;
    }

    public void setDescriptionOn(boolean v) {
        this.descriptionOn = v;
    }

    public boolean isClickToToggleDescription() {
        return clickToToggleDescription;
    }

    public void setClickToToggleDescription(boolean clickToToggleDescription) {
        this.clickToToggleDescription = clickToToggleDescription;
    }

    /**
     * Switches the current description page to the one before the current
     * one, if there is such a page.  Otherwise, leaves the current page
     * unaffected.
     */
    private void switchToPrevPage() {
        if (getDescriptionPageNumber() > 0) {
            descriptionPageNumber--;
        }
    }

    /**
     * Switches the current description page to the one after the current
     * one, if there is such a page.  Otherwise, leaves the current page
     * unaffected.
     */
    private void switchToNextPage() {
        if ( (getDescriptionPageNumber() + 1) < level.getDescriptionPages().size()) {
            descriptionPageNumber++;
        }
    }
}
