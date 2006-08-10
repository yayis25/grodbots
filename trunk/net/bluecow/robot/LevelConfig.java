/*
 * Created on Apr 21, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bluecow.robot.sprite.Sprite;
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
        private String id;
        private String label;
        private Sprite sprite;
        private String onEnter;
        private String onExit;
        
        public Switch(Point location, String id, String label, Sprite sprite, String onEnter) {
            this.location = new Point(location);
            this.id = id;
            this.label = label;
            this.sprite = sprite;
            this.onEnter = onEnter;
        }
        
        /**
         * Copy constructor.  Creates a switch instance with all the same properties
         * as the given switch.
         */
        public Switch(Switch copyMe) {
            this.location = new Point(copyMe.location);
            this.id = copyMe.id;
            this.label = copyMe.label;
            this.sprite = copyMe.sprite;
            this.onEnter = copyMe.onEnter;
            this.onExit = copyMe.onExit;
        }

        public void onEnter(Robot robot) throws EvalError {
            if (onEnter == null) return;
            bsh.set("robot", robot);
            bsh.eval(onEnter);
            bsh.set("robot", null);
        }

        public void onExit(Robot robot) throws EvalError {
            if (onExit == null) return;
            bsh.set("robot", robot);
            bsh.eval(onExit);
            bsh.set("robot", null);
        }

        public Point getLocation() {
            return new Point(location);
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getId() {
            return id;
        }
        
        public Sprite getSprite() {
            return sprite;
        }
        
        public String getOnEnter() {
            return onEnter;
        }
        
        @Override
        public String toString() {
            return "Switch@("+location.x+","+location.y+") \""+id+"\": onEnter \""+onEnter+"\" onExit\""+onExit+"\"";
        }
    }

    private String name;
    private List<Robot> robots = new ArrayList<Robot>();
    private Map<Point, Switch> switches = new HashMap<Point, Switch>();
    private Square[][] map = new Square[0][0];
    private Interpreter bsh;
    private int score;
    
    /**
     * A snapshot of this configuration which can be restored at a later time.
     */
    private LevelConfig snapshot;
    
    public LevelConfig() {
        try {
            initInterpreter();
        } catch (EvalError e) {
            throw new RuntimeException("Couldn't add level config to bsh context");
        }
    }

    private void initInterpreter() throws EvalError {
        bsh = new Interpreter();
        bsh.set("level", this);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Adds the given robot to this level config.
     * 
     * @param r The robot to add, which must have a non-null id which is unique
     * among all robots and switches within this level.
     * @throws NullPointerException If r or r.getId() is null
     * @throws IllegalArgumentException if the robot's id is the same as an existing switch or robot id in this level
     */
    public void addRobot(Robot r) {
        if (r == null) throw new NullPointerException("Null robots are not allowed");
        if (r.getId() == null) throw new NullPointerException("Null robot id not allowed");
        robots.add(r);
        try {
            if (bsh.get(r.getId()) != null) {
                throw new IllegalArgumentException("This level already has a scripting object with id \""+r.getId()+"\"");
            }
            bsh.set(r.getId(), r);
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Sets the size of the map for this level filled with null squares.
     * If there was a map before, it will be wiped out.
     */
    public void setSize(int width, int height) {
        setMap(new Square[width][height]);
    }
    
    public void setMap(Square[][] map) {
        this.map = map;
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

    public void addSwitch(Switch s) {
        Point p = new Point(s.getLocation());
        switches.put(p, s);
        try {
            if (bsh.get(s.getId()) != null) {
                throw new IllegalArgumentException("This level already has a scripting object with id \""+s.getId()+"\"");
            }
            bsh.set(s.getId(), s);
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
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
    
    public void increaseScore(int amount) {
        score += amount;
    }
    
    public int getScore() {
        return score;
    }
    
    /**
     * Resets this level's state to its values last time snapshotState() was called.
     */
    public void resetState() {
        if (snapshot == null) throw new IllegalStateException("No snapshot has been made yet.");
        copyState(snapshot, this);
    }
    
    /**
     * Takes a snapshot of this level's state so that it can be restored by a future
     * call to resetState().
     *
     */
    public void snapshotState() {
        snapshot = new LevelConfig();
        copyState(this, snapshot);
    }

    private static void copyState(LevelConfig src, LevelConfig dst) {
        try {
            dst.initInterpreter();
            dst.setMap(src.map.clone()); // Square objects are immutable, so they can be shared
            dst.setName(src.name);
            dst.robots = new ArrayList<Robot>();
            for (Robot r : src.robots) {
                dst.addRobot(r); // was dst.addRobot(new Robot(r));
            }
            dst.score = src.score;
            // don't modify snapshot, so that resetState() will work multiple times
            dst.switches = new HashMap<Point, Switch>();
            for (Map.Entry<Point, Switch> ent : src.switches.entrySet()) {
                dst.addSwitch(dst.new Switch(ent.getValue()));
            }
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
    }
}
