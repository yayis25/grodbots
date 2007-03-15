/*
 * Created on Mar 6, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

/**
 * The GameStateHandler class encapsulates the correct game state logic
 * for the game to properly start, pause, step, reset, and proceed to the
 * next level.  All of these functions are enabled and disabled when appropriate,
 * and almost all of the state transitions and their appropriate side effects
 * are handled automatically.  The one exception is moving to the next level:
 * Since the game state handler instance is permanently bound to a single
 * Level/GameLoop/Playfield combination, it is impossible for an object
 * of this class to change to a different level.  The client code that creates
 * instances of this class must assign an action to the nextLevelButton in
 * order for it to have any effect.
 *
 * @author fuerth
 * @version $Id$
 */
public class GameStateHandler implements ActionListener {

    /**
     * A helper class that assists with resetting the game loop.  The
     * game loop doesn't allow resets while it is running, and this class
     * is designed to make a reset possible whether or not the game loop
     * is running.
     */
    private static class GameLoopResetter implements PropertyChangeListener {

        private GameLoop gl;
        private GameStateHandler stateHandler;
        private Collection<CircuitEditor> editors;
        private GameState nextState;

        public GameLoopResetter(GameLoop gl, GameStateHandler stateHandler, Collection<CircuitEditor> editors, GameState nextState) {
            this.gl = gl;
            this.stateHandler = stateHandler;
            this.editors = editors;
            this.nextState = nextState;
            if (gl.isRunning()) {
                gl.addPropertyChangeListener(this);
                gl.setStopRequested(true);
            } else {
                finishReset();
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("running") && evt.getNewValue().equals(false)) {
                gl.removePropertyChangeListener(this);
                finishReset();
            }
        }

        private void finishReset() {
            gl.resetState();
            gl.getPlayfield().setWinMessage(null);
            for (CircuitEditor ce : editors) {
                ce.getCircuit().setLocked(false);
            }
            stateHandler.setState(nextState);
        }
    }

    private GameState state = GameState.NOT_STARTED;

    private final GameLoop loop;

    private final Playfield playfield;
    
    private final SoundManager sm;
    
    private final Map<Robot,CircuitEditor> robots;

    /**
     * The button that will start the level running.  This
     * GameStateHandler will react to the button press automatically. You
     * don't need to react to this button being pressed in your own code.
     */
    private final JButton startButton = new JButton("Start");
    
    /**
     * The button that causes a single step in the level.  You
     * don't need to react to this button being pressed in your own code.
     */
    private final JButton stepButton = new JButton("Step");
    
    /**
     * The button that causes the level, game loop, and playfield to reset.
     * to their initial states.  You don't need to react to this button
     * being pressed in your own code.
     */
    private final JButton resetButton = new JButton("Reset");
    
    /**
     * The button that becomes enabled when the player has successfully
     * completed the level at least one time. This button is different
     * from the others: It doesn't have any default behaviour.  Your client
     * code has to add an ActionListener to it which does the appropriate
     * thing.
     */
    private final JButton nextLevelButton = new JButton("Next Level");

    /**
     * Creates a game state handler for the given GameLoop instance.
     * The instance created will only work for the given GameLoop, which
     * means an instance of this class will only work for a single level.
     * <p>
     * It is important to note that the nextLevel button is different from
     * the others: You have to handle presses of this button yourself.
     * The GameStateHandler will enable it and disable it as appropriate,
     * but cannot affect which level is currently being played since the
     * GameLoop and Playfield are treated as immutable properties.
     * 
     * @param loop The GameLoop to manage state for.
     * @param sm The game's sound manager (certain state transitions
     * will emit sound effects).
     * @param resetButton The button that will reset the GameLoop and Playfield
     * to their initial states.
     * @param robots The robots involved in this level.
     */
    public GameStateHandler(GameLoop gameLoop, SoundManager sm,
                            Map<Robot,CircuitEditor> robots) {
        this.loop = gameLoop;
        this.playfield = loop.getPlayfield();
        this.sm = sm;
        this.robots = robots;

        startButton.addActionListener(this);
        stepButton.addActionListener(this);
        resetButton.addActionListener(this);
        nextLevelButton.addActionListener(this);

        nextLevelButton.setEnabled(false);
        
        loop.addPropertyChangeListener("goalReached", new PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                System.out.println("Property change! goalReached "+evt.getOldValue()+" -> "+evt.getNewValue()+" (running="+loop.isRunning()+"; goalReached="+loop.isGoalReached()+")");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (loop.isGoalReached()) {
                            setState(GameState.WON);
                        }
                    }
                });
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();
        if (source == startButton) {
            if (state == GameState.RUNNING) {
                setState(GameState.PAUSED);
            } else {
                setState(GameState.RUNNING);
            }
        } else if (source == stepButton) {
            setState(GameState.STEP);
        } else if (source == resetButton) {
            setState(GameState.RESET);
        } else if (source == nextLevelButton) {
            // we leave this action up to clients.
        } else {
            throw new UnsupportedOperationException(
                    "Game State Handler received unexpected actionevent from "+source);
        }
    }

    public void setState(GameState newState) {
        System.out.printf("Switch state %s -> %s\n", state, newState);
        if (newState == GameState.RESET) {
            state = newState;
            new GameLoopResetter(loop, this, robots.values(), GameState.NOT_STARTED);
            // the rest of the work is deferred until the loop is really stopped
        } else if (newState == GameState.PAUSED) {
            state = newState;
            loop.setStopRequested(true);
            lockEditors(true);
            startButton.setText("Resume");
            stepButton.setText("Step");
            resetButton.setText("Reset");
            playfield.setLabellingOn(false);
        } else if (newState == GameState.NOT_STARTED) {
            state = newState;
            lockEditors(false);
            startButton.setText("Start");
            stepButton.setText("Step");
            resetButton.setText("Reset");
            playfield.setLabellingOn(true);
        } else if (newState == GameState.RUNNING) {
            if (state == GameState.WON) {
                state = GameState.RESET;
                new GameLoopResetter(loop, this, robots.values(), GameState.RUNNING);
            } else {
                state = newState;
                lockEditors(true);
                loop.setStopRequested(false);
                new Thread(loop).start();
                startButton.setText("Pause");
                stepButton.setText("Step");
                resetButton.setText("Reset");
                playfield.setLabellingOn(false);
            }
        } else if (newState == GameState.STEP) {
            if (state == GameState.WON) {
                state = GameState.RESET;
                new GameLoopResetter(loop, this, robots.values(), GameState.STEP);
            } else if (state == GameState.RUNNING) {
                state = newState;
                lockEditors(true);
                loop.setStopRequested(true);
                setState(GameState.PAUSED);
            } else {
                state = newState;
                lockEditors(true);
                loop.setStopRequested(false);
                loop.singleStep();
                setState(GameState.PAUSED);
            }
        } else if (newState == GameState.WON) {
            state = newState;
            lockEditors(true);
            playfield.setWinMessage("You Win!");
            sm.play("win");
            startButton.setText("Restart");
            stepButton.setText("Restep");
            resetButton.setText("Reset");
            nextLevelButton.setEnabled(true);
            playfield.setLabellingOn(false);
        }
    }

    /** Locks or unlocks all editors in the robots map. */
    private void lockEditors(boolean locked) {
        for (CircuitEditor ce : robots.values()) {
            ce.getCircuit().setLocked(locked);
        }
    }

    public JButton getNextLevelButton() {
        return nextLevelButton;
    }

    public JButton getResetButton() {
        return resetButton;
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getStepButton() {
        return stepButton;
    }
    
    
}