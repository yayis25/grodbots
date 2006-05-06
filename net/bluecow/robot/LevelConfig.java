/*
 * Created on Apr 21, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * LevelConfig represents the configuration of a particular level.  It is not
 * immutible because the level might need to change during play, for example
 * if a switch is thrown then the square configuration, robot speeds, and anything
 * else might change.
 *
 * @author fuerth
 * @version $Id$
 */
public class LevelConfig {
    
    /**
     * The Switch class represents an effect that can happen to a level
     * when a robot enters or leaves a square.
     *
     * @author fuerth
     * @version $Id$
     */
    public class Switch {
        private Point location;
        private String name;
        private Sprite sprite;
        private String onEnter;
        private String onExit;
        
        public Switch(Point location, String name, Sprite sprite, String onEnter) {
            this.location = new Point(location);
            this.name = name;
            this.sprite = sprite;
            this.onEnter = onEnter;
        }
        
        public void onEnter(Robot robot) {
            System.out.println("Switch "+name+": executing onEnter code: "+onEnter);
            if (onEnter == null) return;
            try {
                bsh.set("robot", robot);
                bsh.eval(onEnter);
                bsh.set("robot", null);
            } catch (EvalError e) {
                throw new RuntimeException(e);
            }
        }

        public void onExit(Robot robot) {
            if (onExit == null) return;
            try {
                bsh.set("robot", robot);
                bsh.eval(onExit);
                bsh.set("robot", null);
            } catch (EvalError e) {
                throw new RuntimeException(e);
            }
        }

        public Point getLocation() {
            return new Point(location);
        }
        
        public String getName() {
            return name;
        }
        
        public Sprite getSprite() {
            return sprite;
        }
        
        public String getOnEnter() {
            return onEnter;
        }
        
        @Override
        public String toString() {
            return "Switch@("+location.x+","+location.y+") \""+name+"\": onEnter \""+onEnter+"\" onExit\""+onExit+"\"";
        }
    }

    private String name;
    private List<Robot> robots = new ArrayList<Robot>();
    private Map<Point, Switch> switches = new HashMap<Point, Switch>();
    private Square[][] map = new Square[0][0];
    private Interpreter bsh;
    
    public LevelConfig() {
        bsh = new Interpreter();
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void addRobot(Robot r) {
        if (r == null) throw new NullPointerException("Null robots are not allowed");
        if (r.getName() == null) throw new NullPointerException("Null robot name not allowed");
        robots.add(r);
        try {
            bsh.set(r.getName(), r);
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Sets the size of the map for this level filled with null squares.
     * If there was a map before, it will be wiped out.
     */
    public void setSize(int width, int height) {
        map = new Square[width][height];
        try {
            bsh.set("map", map);
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
    }
    
    public int getWidth() {
        return map.length;
    }
    
    public int getHeight() {
        return map[0].length;
    }

    public Dimension getSize() {
        return new Dimension(getWidth(), getHeight());
    }

    public void setSquare(int x, int y, Square square) {
        map[x][y] = square;
    }
    
    public Square getSquare(int x, int y) {
        return map[x][y];
    }

    public Square getSquare(float x, float y) {
        return getSquare((int) x, (int) y);
    }

    public Square getSquare(Point2D.Float location) {
        return getSquare((int) location.x, (int) location.y);
    }

    /** Adds a new switch to this level. */
    public void addSwitch(int x, int y, String switchName, String imagePath, String switchCode) throws FileNotFoundException {
        Point p = new Point(x, y);
        switches.put(p, new Switch(p, switchName, SpriteManager.load(imagePath), switchCode));
    }

    /** Returns an unmodifiable list of this level's switches. */
    public Collection<Switch> getSwitches() {
        return Collections.unmodifiableCollection(switches.values());
    }

    /**
     * Returns the switch located on the given map location.
     * 
     * @param location The map location.  The given point object will not be modified.
     * @return The switch located on the given map location, or null if there are no
     * switches there.
     */
    public Switch getSwitch(Point2D.Float location) {
        return switches.get(new Point((int) location.x, (int) location.y));
    }
    
    /** Returns an unmodifiable list of this level's robots. */
    public List<Robot> getRobots() {
        return Collections.unmodifiableList(robots);
    }
    
    /**
     * Returns the actual map that this level uses.  Modifications to the map
     * will be reflected in the level config, but the LevelConfig has no way of
     * noticing these changes.  It would be better to use setSquare(x,y) to
     * modify the map.
     */
    public Square[][] getMap() {
        return map;
    }
}
