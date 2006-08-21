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
import java.util.List;

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
        private boolean enabled = true;
        
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
            this.enabled = copyMe.enabled;
        }

        /**
         * Invokes this switch's onEnter script.  You should call this every
         * time a robot enters the square occupied by this switch.
         * 
         * @param robot The robot that just entered this switch
         * @throws EvalError if there is a scripting error
         */
        public void onEnter(Robot robot) throws EvalError {
            if (onEnter == null) return;
            if (!enabled) return;
            bsh.set("robot", robot);
            for (String name : bsh.getNameSpace().getVariableNames()) {
                System.out.println("  "+name+": "+bsh.get(name));;
            }
            bsh.eval(onEnter);
            bsh.set("robot", null);
        }

        /**
         * Invokes this switch's onExit script.  You should call this every
         * time a robot exits the square occupied by this switch.
         * 
         * @param robot The robot that just left this switch
         * @throws EvalError if there is a scripting error
         */
        public void onExit(Robot robot) throws EvalError {
            if (onExit == null) return;
            if (!enabled) return;
            bsh.set("robot", robot);
            bsh.eval(onExit);
            bsh.set("robot", null);
        }

        /**
         * Returns a copy of the point that determines this switch's location.
         */
        public Point getLocation() {
            return new Point(location);
        }

        /**
         * Moves this switch to the given (x,y) location.
         * 
         * @param x the X coordinate
         * @param y the Y coordinate
         */
        public void setLocation(int x, int y) {
            this.location = new Point(x, y);
        }

        /**
         * Moves this switch to the given (x,y) location.
         * 
         * @param p The new location.  A copy of p will be made; you are free to
         * modify p after calling this method without side effects.
         */
        public void setLocation(Point p) {
            setLocation(p.x, p.y);
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

        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        @Override
        public String toString() {
            return "Switch@("+location.x+","+location.y+") \""+id+"\": "+(enabled?"en":"dis")+"abled; onEnter \""+onEnter+"\"; onExit\""+onExit+"\"";
        }
    }

    private String name;
    private List<Robot> robots = new ArrayList<Robot>();
    
    /**
     * All the switches in this level.
     * <p>
     * Implementation note: This can't be a map of points to switches 
     * because the bsh scripts are allowed to modify the switch locations
     * (and the key in the map wouldn't update accordingly).
     */
    private Collection<Switch> switches = new ArrayList<Switch>();

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
            System.out.println("Added robot '"+r.getId()+"'");
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
        try {
            if (bsh.get(s.getId()) != null) {
                throw new IllegalArgumentException("Level \""+name+"\" already has a scripting object with id \""+s.getId()+"\"");
            }
            bsh.set(s.getId(), s);
            System.out.println("added switch '"+s.getId()+"'");
            switches.add(s);
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
    }

    /** Returns an unmodifiable list of this level's switches. */
    public Collection<Switch> getSwitches() {
        return Collections.unmodifiableCollection(switches);
    }

    /**
     * Returns the switch located on the given map location.
     * 
     * @param location The map location.  The given point object will not be modified.
     * @return The switch located on the given map location, or null if there are no
     * switches there.
     */
    public Switch getSwitch(Point2D.Float location) {
        Point point = new Point((int) location.x, (int) location.y);
        for (Switch s : switches) {
            if (s.getLocation().equals(point)) {
                return s;
            }
        }
        return null;
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
            
            // need to use addRobot() for each robot to get them into the bsh interpreter
            dst.robots = new ArrayList<Robot>();
            for (Robot r : src.robots) {
                dst.addRobot(r); // was dst.addRobot(new Robot(r));
            }
            dst.score = src.score;

            // addSwitch() adds the switch to the BSH interpreter
            dst.switches = new ArrayList<Switch>();
            for (Switch s : src.switches) {
                dst.addSwitch(dst.new Switch(s));
            }
            // don't modify snapshot, so that resetState() will work multiple times
        } catch (EvalError e) {
            throw new RuntimeException(e);
        }
    }
}
