package net.bluecow.robot;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import net.bluecow.robot.gate.Gate;

/**
 * The robot.
 */
public class Robot {
    
    private PlayfieldModel pfm;
    private Point position;
    private ImageIcon icon;

    // Inputs that make the robot do stuff
    private RobotInput upInput = new RobotInput();
    private RobotInput downInput = new RobotInput();
    private RobotInput leftInput = new RobotInput();
    private RobotInput rightInput = new RobotInput();
    private RobotInput[] inputs = new RobotInput[] {upInput, downInput, leftInput, rightInput};

    // Outputs that report the robot's current state and surroundings
    private RobotSensorOutput redSensorOutput = new RobotSensorOutput();
    private RobotSensorOutput greenSensorOutput = new RobotSensorOutput();
    private RobotSensorOutput blueSensorOutput = new RobotSensorOutput();
    private RobotSensorOutput[] outputs = new RobotSensorOutput[] {redSensorOutput, greenSensorOutput, blueSensorOutput};

    public Robot(PlayfieldModel pfm, Point initialPosition, ImageIcon icon) {
        this.pfm = pfm;
        this.position = initialPosition;
        this.icon = icon;
    }
    
    public void move() {
        Square s = pfm.getSquare(position.x, position.y); 
	redSensorOutput.setState(s.getType() == Square.RED);
	greenSensorOutput.setState(s.getType() == Square.GREEN);
	blueSensorOutput.setState(s.getType() == Square.BLUE);
    }
    
    private void moveLeft() {
        if (position.x > 0
                && pfm.getSquare(position.x-1, position.y).isOccupiable()) {
                Point oldPos = new Point(position);
                position.x -= 1;
                fireMoveEvent(oldPos);
            }
    }

    private void moveRight() {
        if (position.x < pfm.getWidth()
                && pfm.getSquare(position.x+1, position.y).isOccupiable()) {
                Point oldPos = new Point(position);
                position.x += 1;
                fireMoveEvent(oldPos);
            }
    }

    public void moveDown() {
        if (position.y < pfm.getHeight()
                && pfm.getSquare(position.x, position.y+1).isOccupiable()) {
                Point oldPos = new Point(position);
                position.y += 1;
                fireMoveEvent(oldPos);
            }
    }

    public void moveUp() {
        if (position.y > 0
                && pfm.getSquare(position.x, position.y-1).isOccupiable()) {
                Point oldPos = new Point(position);
                position.y -= 1;
                fireMoveEvent(oldPos);
            }
    }

    /**
     * The inputGateListener listens to the robot's various inputs,
     * and causes it to do things when they change state (such as move
     * around the maze).
     */
    private ChangeListener inputGateListener = new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		Gate.Input i = (Gate.Input) e.getSource();

		if (i == upInput) {
		    moveUp();
		} else if (i == downInput) {
		    moveDown();
		} else if (i == leftInput) {
		    moveLeft();
		} else if (i == rightInput) {
		    moveRight();
		}
	    }
	};

    /**
     * The RobotSensorOutput class represents an environmental sensor
     * attached to the robot.  It can change state as the robot's
     * immediate surroundings change.  For example, when the robot
     * moves from a red square to a green square, the red output will
     * change to false and the green output will change to true.
     */
    private class RobotSensorOutput implements Gate {
	private boolean state;

	public boolean getOutputState() {
	    return state;
	}

	/** Sets the state of this output.  Only methods in Robot should call this. */
	public void setState(boolean v) {
	    if (v != state) {
		state = v;
		fireChangeEvent();
	    }
	}

	private List changeListeners = new ArrayList();

	public void addChangeListener(ChangeListener l) {
	    changeListeners.add(l);
	}

	public void removeChangeListener(ChangeListener l) {
	    changeListeners.remove(l);
	}

	private void fireChangeEvent() {
	    ChangeEvent evt = new ChangeEvent(this);
	    Iterator it = changeListeners.iterator();
	    while (it.hasNext()) {
		((ChangeListener) it.next()).stateChanged(evt);
	    }
	}
    }

    /**
     * The RobotInput class represents an input to the robot which
     * will cause it to perform some action.  For example, the upInput
     * and downInput instances cause the robot to move up or down.
     */
    private class RobotInput implements Gate.Input {
	private Gate inputGate;
	
	public void connect(Gate g) {
	    if (inputGate != null) {
		inputGate.removeChangeListener(inputGateListener);
	    }
	    g.addChangeListener(inputGateListener);
	    inputGate = g;
	    inputGateListener.stateChanged(new ChangeEvent(inputGate));
	}

	public boolean getState() {
	    if (inputGate == null) {
		return false;
	    } else {
		return inputGate.getOutputState();
	    }
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
    
    protected List propertyChangeListeners = new ArrayList();
    protected void fireMoveEvent(Point oldPos) {
        PropertyChangeEvent e = new PropertyChangeEvent(this, "position", oldPos, position);
        Iterator it = propertyChangeListeners.iterator();
        while (it.hasNext()) {
            ((PropertyChangeListener) it.next()).propertyChange(e);
        }
    }
    
    // ACCESSORS and MUTATORS
    
    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public RobotInput[] getInputs() {
	return inputs;
    }

    public RobotSensorOutput[] getOutputs() {
	return outputs;
    }
}
