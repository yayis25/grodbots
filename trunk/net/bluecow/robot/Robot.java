package net.bluecow.robot;

import java.awt.geom.Point2D;

import javax.swing.ImageIcon;

import net.bluecow.robot.gate.Gate;

/**
 * The robot.
 */
public class Robot {
	
	private PlayfieldModel pfm;
	private Point2D.Float position;
	private ImageIcon icon;
	
	// Inputs that make the robot do stuff
	private RobotInput upInput = new RobotInput("Up");
	private RobotInput downInput = new RobotInput("Down");
	private RobotInput leftInput = new RobotInput("Left");
	private RobotInput rightInput = new RobotInput("Right");
    private RobotInputsGate robotInputsGate 
    				= new RobotInputsGate(new RobotInput[] {upInput, downInput, leftInput, rightInput});
	
	// Outputs that report the robot's current state and surroundings
	private RobotSensorOutput redSensorOutput = new RobotSensorOutput("Red");
	private RobotSensorOutput greenSensorOutput = new RobotSensorOutput("Green");
	private RobotSensorOutput blueSensorOutput = new RobotSensorOutput("Blue");
	private RobotSensorOutput[] outputs = new RobotSensorOutput[] {redSensorOutput, greenSensorOutput, blueSensorOutput};
	
	public Robot(PlayfieldModel pfm, ImageIcon icon) {
        this.pfm = pfm;
        this.position = pfm.getStartPosition();
        this.icon = icon;
	}
	
	public void move() {
	    if (upInput.getState() == true) {
	        moveUp();
	    }
	    if (downInput.getState() == true) {
	        moveDown();
	    }
	    if (leftInput.getState() == true) {
	        moveLeft();
	    }
	    if (rightInput.getState() == true) {
	        moveRight();
	    }
	}
	
	public void updateSensors() {
		Square s = pfm.getSquare(position.x, position.y); 
		redSensorOutput.setState(s.getType() == Square.RED);
		greenSensorOutput.setState(s.getType() == Square.GREEN);
		blueSensorOutput.setState(s.getType() == Square.BLUE);
	}
	
	private void moveLeft() {
		if (position.x > 0
				&& pfm.getSquare(position.x-pfm.getStepSize(), position.y).isOccupiable()) {
			position.x -= pfm.getStepSize();
		}
	}
	
	private void moveRight() {
		if (position.x < pfm.getWidth()
				&& pfm.getSquare(position.x+pfm.getStepSize(), position.y).isOccupiable()) {
			position.x += pfm.getStepSize();
		}
	}
	
	private void moveDown() {
        boolean atBottom = position.y >= pfm.getHeight();
        boolean obstacleInTheWay = !pfm.getSquare(position.x, position.y+pfm.getStepSize()).isOccupiable();
        if ( (!atBottom)	&& (!obstacleInTheWay)) {
			position.y += pfm.getStepSize();
		}
	}
	
	private void moveUp() {
		if (position.y > 0
				&& pfm.getSquare(position.x, position.y-pfm.getStepSize()).isOccupiable()) {
			position.y -= pfm.getStepSize();
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
		private String label;

		public RobotSensorOutput(String label) {
			this.label = label;
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
			return label;
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
	
	public ImageIcon getIcon() {
		return icon;
	}
	
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}
	
	public Point2D.Float getPosition() {
		return new Point2D.Float(position.x, position.y);
	}
	
	public void setPosition(Point2D.Float position) {
		this.position = new Point2D.Float(position.x, position.y);
	}
	
	public RobotInput[] getInputs() {
	    return robotInputsGate.inputs;
	}
	
	public RobotSensorOutput[] getOutputs() {
		return outputs;
	}

    public Gate getInputsGate() {
        return robotInputsGate;
    }
}
