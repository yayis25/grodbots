/*
 * Created on Aug 23, 2007
 *
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 * Manages the user interface for the game.  The user interface components themselves
 * (the playfield and all the circuit editors) live in a JPanel, and are laid out with
 * the playfield taking up most of the left half of the panel, and the current editor
 * taking up the right half.  If there is more than one circuit associated with the
 * current level, small non-interactive views of circuit editors for every circuit will
 * be arranged across the bottom of the panel. Clicking one makes it the current active
 * editor.
 *
 * @author fuerth
 * @version $Id:$
 */
public class GameUI {

    /**
     * This is the panel that all the UI components live in.  Its contents and layout
     * are managed by this class.
     */
    private final JPanel panel;
    
    /**
     * The level this game ui manages.
     */
    private final LevelConfig level;
    
    /**
     * The playfield that the map and robots are in.
     */
    private final Playfield playfield;
    
    /**
     * Component that displays the current score (level and overall).
     */
    private final ScoreBar scoreBar;
    
    /**
     * The current circuit editor of interest.  This one is big and the user can interact
     * with it (unless it's locked of course).
     */
    private CircuitEditor currentEditor;
    
    /**
     * Small previews of all circuit editors associated with the current level.  These are
     * not interactive, but clicking on one will make it the current editor.
     */
    private final EditorsPanel editorsPanel;

    /**
     * Handles start, stop, pause, and reset of the level.
     */
    private GameStateHandler gameStateHandler;

    /**
     * The action that can save the robot circuits for this level.
     */
    private final SaveCircuitAction saveCircuitAction;
    
    /**
     * The action that can load the robot circuits for this level.
     */
    private final LoadCircuitAction loadCircuitAction;
    
    private final LoadGhostAction loadGhostAction;

    /**
     * Action that loads new levels. If invoked, this GameUI will get totally replaced with
     * a new one.
     */
    private final LoadLevelsAction loadLevelsAction;

    /**
     * The action that quits the game.
     */
    private QuitAction quitAction = new QuitAction();

    /**
     * Creates a new game UI for the given panel.
     * 
     * @param level
     */
    public GameUI(Main session, LevelConfig level, SoundManager sm, Action nextLevelAction) {
        this.level = level;
        panel = new JPanel(new BorderLayout());
        editorsPanel = new EditorsPanel(sm);
        scoreBar = new ScoreBar(session.getGameConfig(), level);
        
        playfield = new Playfield(session.getGameConfig(), level);
        playfield.setLabellingOn(true);
        Map<Robot,CircuitEditor> robots = new LinkedHashMap<Robot,CircuitEditor>();
        for (Robot robot : level.getRobots()) {
            CircuitEditor ce = editorsPanel.addCircuit(robot.getCircuit());
            robots.put(robot, ce);
            
            // XXX temporary. editors panel will manage current editor selection
            if (currentEditor == null) {
                currentEditor = new CircuitEditor(robot.getCircuit(), sm);
            }
        }
        final GameLoop gameLoop = new GameLoop(robots.keySet(), level, playfield);

        saveCircuitAction = new SaveCircuitAction(panel, robots.keySet());
        loadCircuitAction = new LoadCircuitAction(panel, robots.keySet());
        loadLevelsAction = new LoadLevelsAction(session);
        
        loadGhostAction = new LoadGhostAction(session);
        loadGhostAction.setGameLoop(gameLoop);
        loadGhostAction.setPlayfield(playfield);
        loadGhostAction.setRobots(robots.keySet());

        gameStateHandler = new GameStateHandler(gameLoop, sm, robots);

        // have to handle next level events ourselves; all others are handled
        // by the GameStateHandler.
        gameStateHandler.getNextLevelButton().addActionListener(nextLevelAction);
        
        final JButton saveCircuitButton = new JButton();
        saveCircuitButton.setAction(saveCircuitAction);

        final JButton loadCircuitButton = new JButton();
        loadCircuitButton.setAction(loadCircuitAction);

        final JButton loadLevelsButton = new JButton();
        loadLevelsButton.setAction(loadLevelsAction);
        
        JPanel stateButtonsPanel = new JPanel(new FlowLayout());
        stateButtonsPanel.add(gameStateHandler.getStartButton());
        stateButtonsPanel.add(gameStateHandler.getStepButton());
        stateButtonsPanel.add(gameStateHandler.getResetButton());
        stateButtonsPanel.add(gameStateHandler.getNextLevelButton());
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(stateButtonsPanel, BorderLayout.NORTH);

        JPanel floaterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        floaterPanel.add(playfield);
        
        JComponent pffcp = new JPanel(new BorderLayout());
        pffcp.add(new JLabel(level.getName(), JLabel.CENTER), BorderLayout.NORTH);
        pffcp.add(floaterPanel, BorderLayout.CENTER);
        pffcp.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(scoreBar, BorderLayout.NORTH);
        panel.add(pffcp, BorderLayout.CENTER);
        panel.add(editorsPanel, BorderLayout.SOUTH);
        panel.add(currentEditor, BorderLayout.EAST);
        // TODO add the game state buttons
    }
    
    /**
     * Makes sure this level has stopped running, and locks in the score.  It is
     * mandatory to call this close() method when finished with the game UI for
     * each level.
     */
    void close() {
        // We have to reset the game state to start with, in case the
        // current level is still running right now.  However, the reset
        // also puts the level's score back to 0.  Since total score
        // is calculated as the sum of the level scores, we need to preserve
        // the level score as it was at the time we left each level.
        // Hence, the following:

        level.lockInScore();

        if (gameStateHandler != null) {
            gameStateHandler.setState(GameState.RESET);
        }
    }
    
    /**
     * Creates and returns a menu bar that is applicable to this game UI's level.
     * @return
     */
    public JMenuBar getMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu menu;
        JMenuItem item;
        mb.add(menu = new JMenu("File"));
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(item = new JMenuItem(loadLevelsAction));
        menu.add(item = new RecentFilesMenu("Open Recent Levels", loadLevelsAction, RobotUtils.getPrefs().node("recentGameFiles")));
        item.setMnemonic(KeyEvent.VK_T);
        menu.add(item = new JMenuItem(loadCircuitAction));
        menu.add(item = new RecentFilesMenu("Open Recent Circuit", loadCircuitAction, RobotUtils.getPrefs().node("recentCircuitFiles")));
        item.setMnemonic(KeyEvent.VK_R);
        menu.add(item = new JMenuItem(saveCircuitAction));
        menu.add(item = new JMenuItem(quitAction));
        
        mb.add(menu = new JMenu("Ghost"));
        menu.setMnemonic(KeyEvent.VK_G);
        menu.add(item = new JMenuItem(loadGhostAction));
        menu.add(item = new RecentFilesMenu("Open Recent Ghost", loadGhostAction, RobotUtils.getPrefs().node("recentGhostFiles")));
        item.setMnemonic(KeyEvent.VK_R);
        
        return mb;
    }
    
    /**
     * Returns the JPanel that contains the game's user interface components.
     */
    public JPanel getPanel() {
        return panel;
    }
}
