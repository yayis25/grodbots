package net.bluecow.robot;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.bluecow.robot.GameConfig.GateConfig;
import net.bluecow.robot.GameConfig.SensorConfig;
import net.bluecow.robot.gate.Gate;

/**
 * The robot.
 */
public class Robot {
    
    private static final int MOVING_UP = 1 << 0;
    private static final int MOVING_DOWN = 1 << 1;
    private static final int MOVING_LEFT = 1 << 2;
    private static final int MOVING_RIGHT = 1 << 3;
	
    /**
     * A bitmask of the directions this robot is currently moving.
     */
    private int movingDirection = 0;
    
    /**
     * A count of how many frames in a row this robot has been moving
     * in the same direction.
     */
    private int movingFrame = 0;
    
	private LevelConfig level;
	private Point2D.Float position;
	private Sprite sprite;
	private float stepSize;
    
	// Inputs that make the robot do stuff
	private RobotInput upInput = new RobotInput("Up");
	private RobotInput downInput = new RobotInput("Down");
	private RobotInput leftInput = new RobotInput("Left");
	private RobotInput rightInput = new RobotInput("Right");
    private RobotInputsGate robotInputsGate 
    				= new RobotInputsGate(new RobotInput[] {upInput, downInput, leftInput, rightInput});
	
	/** A collection of outputs that report the robot's current state and surroundings. */
	private Map<SensorConfig,RobotSensorOutput> outputs;

    /** Indicates whether or not this robot has reached its goal. */
    private boolean goalReached;
    
    /** The position that this robot starts out on. */
    private Point2D.Float startPosition;
    
    /** This robot's name */
    private String name;
	
    public Robot(String name, LevelConfig level, List<SensorConfig> sensorList, String spritePath, Point2D.Float startPosition, float stepSize) throws FileNotFoundException {
        this(name, level, sensorList, SpriteManager.load(spritePath), startPosition, stepSize);
    }

    public Robot(String name, LevelConfig level, List<SensorConfig> sensorList, Sprite sprite, Point2D.Float startPosition, float stepSize) {
        this.name = name;
        this.level = level;
        this.sprite = sprite;
        this.startPosition = startPosition;
        setPosition(startPosition);
        this.stepSize = stepSize;
        
        System.out.println("Creating robot with sensor list: "+sensorList);
        outputs = new LinkedHashMap<SensorConfig, RobotSensorOutput>();
        for (SensorConfig sensor : sensorList) {
            outputs.put(sensor, new RobotSensorOutput(sensor));
        }
	}
	
    public void move() {
        int direction = 0;
	    if (upInput.getState() == true) {
	        moveUp();
            direction |= MOVING_UP;
	    }
	    if (downInput.getState() == true) {
	        moveDown();
            direction |= MOVING_DOWN;
	    }
	    if (leftInput.getState() == true) {
	        moveLeft();
            direction |= MOVING_LEFT;
	    }
	    if (rightInput.getState() == true) {
	        moveRight();
            direction |= MOVING_RIGHT;
	    }
        
        if (direction == movingDirection) {
            movingFrame++;
        } else {
            movingDirection = direction;
            movingFrame = 0;
        }
	}
	
    /**
     * Updates all the sensor outputs to correspond with the square this robot
     * is currently occupying.
     */
	public void updateSensors() {
	    Square s = level.getSquare(position.x, position.y);
        Collection squareSensors = s.getSensorTypes();
        for (Map.Entry<SensorConfig, RobotSensorOutput> entry : outputs.entrySet()) {
            entry.getValue().setState(squareSensors.contains(entry.getKey()));
        }
	}
	
	private void moveLeft() {
		if (position.x > 0
				&& level.getSquare(position.x-stepSize, position.y).isOccupiable()) {
			position.x -= stepSize;
		}
	}
	
	private void moveRight() {
		if (position.x < level.getWidth()
				&& level.getSquare(position.x+stepSize, position.y).isOccupiable()) {
			position.x += stepSize;
		}
	}
	
	private void moveDown() {
        boolean atBottom = position.y >= level.getHeight();
        boolean obstacleInTheWay = !level.getSquare(position.x, position.y+stepSize).isOccupiable();
        if ( (!atBottom)	&& (!obstacleInTheWay)) {
			position.y += stepSize;
		}
	}
	
	private void moveUp() {
		if (position.y > 0
				&& level.getSquare(position.x, position.y-stepSize).isOccupiable()) {
			position.y -= stepSize;
		}
	}
		
	/**
	 * The RobotSensorOutput class represents an environmental sensor
	 * attached to the robot.  It can change state as the robot's
	 * immediate surroundings change.  For example, when the robot
	 * moves from a red square to a green square, the red output will
	 * change to false and the green output will change to true.
	 */
	class RobotSensorOutput implements Gate {
		private boolean state;
		private SensorConfig config;

		public RobotSensorOutput(SensorConfig config) {
			this.config = config;
		}
		
		public boolean getOutputState() {
			return state;
		}
		
		/** Sets the state of this output.  Only methods in Robot should call this. */
		public void setState(boolean v) {
			if (v != state) {
				state = v;
			}
		}
				
		public Gate.Input[] getInputs() {
			return new Gate.Input[0];
		}
		
		public String getLabel() {
			return config.getId();
		}

        public void evaluateInput() {
            // doesn't do anything because this gate's state gets set elsewhere
            // XXX: this could check which colour of square the robot is over, and update the state accordingly
        }
        public void latchOutput() {
            // nothing to do
        }
        
        public void reset() {
            setState(false);
        }
	}
	
	/**
	 * The RobotInput class represents an input to the robot which
	 * will cause it to perform some action.  For example, the upInput
	 * and downInput instances cause the robot to move up or down.
	 */
	private class RobotInput implements Gate.Input {
	    private Gate inputGate;
	    private String label;
		
	    public RobotInput(String label) {
	        this.label = label;
	    }
	    
		public void connect(Gate g) {
			inputGate = g;
		}
		
		public boolean getState() {
		    if (inputGate != null) {
		        return inputGate.getOutputState();
		    } else {
		        return false;
		    }
		}

        public Gate getConnectedGate() {
            return inputGate;
        }
        
        /**
         * Returns the robotInputsGate instance.
         */
        public Gate getGate() {
            return robotInputsGate;
        }
        
        public String getLabel() {
            return label;
        }
	}
	
	/**
	 * Returns the input that will cause the robot to move UP when the
	 * input state is TRUE.
	 */
	public Gate.Input getUpInput() {
		return upInput;
	}
	
	/**
	 * Returns the input that will cause the robot to move DOWN when the
	 * input state is TRUE.
	 */
	public Gate.Input getDownInput() {
		return downInput;
	}
	
	/**
	 * Returns the input that will cause the robot to move LEFT when the
	 * input state is TRUE.
	 */
	public Gate.Input getLeftInput() {
		return leftInput;
	}
	
	/**
	 * Returns the input that will cause the robot to move RIGHT when the
	 * input state is TRUE.
	 */
	public Gate.Input getRightInput() {
		return rightInput;
	}
	
	/**
	 * The RobotInputsGate is basically a place to hold the collection
	 * of this robot's inputs.  Its output state is meaningless.
	 */
	public class RobotInputsGate implements Gate {

	    private RobotInput[] inputs;

	    /**
	     * Creates the instance of this robot's input gate.
	     * 
	     * @param inputs All the inputs that this robot has.
	     */
	    public RobotInputsGate(RobotInput[] inputs) {
	        super();
	        this.inputs = inputs;
	    }
	    
	    public String getLabel() {
	        return "Robot inputs";
	    }

	    /**
	     * Always returns false.
	     */
	    public boolean getOutputState() {
	        return false;
	    }

	    public Input[] getInputs() {
	        return inputs;
	    }

        public void evaluateInput() {
            // this gate always outputs false
        }
        
        public void latchOutput() {
            // no op
        }
        
        public void reset() {
            // no op
        }
	}

	// ACCESSORS and MUTATORS

    /**
     * Tells the icon's appropriate heading (in radians) based in its current direction
     * of travel.
     */
    public double getIconHeading() {
        double theta;
        if (movingDirection == MOVING_UP) {
            theta = 0.0;
        } else if (movingDirection == (MOVING_UP | MOVING_RIGHT)) {
            theta = 0.25 * Math.PI;
        } else if (movingDirection == MOVING_RIGHT) {
            theta = 0.5 * Math.PI;
        } else if (movingDirection == (MOVING_DOWN | MOVING_RIGHT)) {
            theta = 0.75 * Math.PI;
        } else if (movingDirection == MOVING_DOWN) {
            theta = 1.0 * Math.PI;
        } else if (movingDirection == (MOVING_DOWN | MOVING_LEFT)) {
            theta = 1.25 * Math.PI;
        } else if (movingDirection == MOVING_LEFT) {
            theta = 1.5 * Math.PI;
        } else if (movingDirection == (MOVING_UP | MOVING_LEFT)) {
            theta = 1.75 * Math.PI;
        } else {
            // Robot is working against itself! Just spin around wildly
            theta = Math.random() * Math.PI * 2.0;
        }
        return theta;
    }
    
    public String getName() {
        return name;
    }
    
	public Sprite getSprite() {
		return sprite;
	}

	public Point2D.Float getPosition() {
		return new Point2D.Float(position.x, position.y);
	}
	
    /**
     * Sets this robot's position by making a copy of the given point.
     * 
     * <p>This method is final because it is called from the constructor.
     */
	public final void setPosition(Point2D.Float position) {
		this.position = new Point2D.Float(position.x, position.y);
	}

    /** Sets this robot's position. */
    public void setPosition(double x, double y) {
        setPosition(new Point2D.Float((float) x, (float) y));
    }
    
	public RobotInput[] getInputs() {
	    return robotInputsGate.inputs;
	}
	
	public RobotSensorOutput[] getOutputs() {
        System.out.println("Converting outputs map to set.  Map = "+outputs+"; set = "+outputs.entrySet());
		return outputs.values().toArray(new RobotSensorOutput[outputs.size()]);
	}

    public Gate getInputsGate() {
        return robotInputsGate;
    }

    public void addGateAllowance(GateConfig gate, int count) {
        System.err.println("Robot.addGateAllowance is not implemented");
    }

    public boolean isGoalReached() {
        return goalReached;
    }
    
    public void setGoalReached(boolean v) {
        goalReached = v;
    }

    public void resetState() {
        setGoalReached(false);
        setPosition(startPosition);
    }
}
