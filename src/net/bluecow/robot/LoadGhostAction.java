/*
 * Created on Aug 25, 2007
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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * TODO this action needs a lot of work to integrate with the new single-window game ui.
 *
 * @author fuerth
 * @version $Id:$
 */
class LoadGhostAction extends AbstractAction {
            
    /**
     * The session this action belongs to.
     */
    private final Main session;

    private GameLoop gameLoop;

    private Playfield playfield;
    
    private Collection<Robot> robots;
    
    public LoadGhostAction(Main main) {
        super("Open Circuit Into New Ghost...");
        session = main;
        putValue(MNEMONIC_KEY, KeyEvent.VK_O);
    }
    
    public void actionPerformed(ActionEvent e) {
        InputStream in = null;
        Preferences recentFiles = RobotUtils.getPrefs().node("recentGhostFiles");
        try {
            File f = new File(e.getActionCommand() == null ? "!@#$%^&*" : e.getActionCommand());
            if ( ! (f.isFile() && f.canRead()) ) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Open Circuit Description File For Ghost");
                fc.setCurrentDirectory(new File(recentFiles.get("0", System.getProperty("user.home"))));
                int choice = fc.showOpenDialog(session.playfieldFrame);
                if (choice != JFileChooser.APPROVE_OPTION) return;
                f = fc.getSelectedFile();
            }
            in = new FileInputStream(f);
            LevelConfig ghostLevel = new LevelConfig(session.config.getLevels().get(session.levelNumber));
            
            /*
             * remove existing robots.. should have a method in LevelConfig for creating this
             * type of copy. 
             */
            for (Robot robot : new ArrayList<Robot>(ghostLevel.getRobots())) {
                ghostLevel.removeRobot(robot);
            }
            
            List<Robot> ghosts = new ArrayList<Robot>();
            for (Robot robot : robots) {
                Robot ghost = new Robot(robot, ghostLevel);
                ghosts.add(ghost);
                ghostLevel.addRobot(ghost);
            }
            CircuitStore.load(in, ghosts);
            for (Robot ghost : ghosts) {
                if (ghost.getLevel() != ghostLevel) {
                    throw new IllegalStateException("Ghost's level is not the ghostLevel");
                }
                if (!ghostLevel.getRobots().contains(ghost)) {
                    throw new IllegalStateException("ghostLevel doesn't contain ghost "+ghost);
                }
                CircuitEditor ghostCE = new CircuitEditor(ghost.getCircuit(), session.getGameConfig().getSoundManager());
                ghost.getCircuit().setLocked(true);
                JFrame ghostFrame = new JFrame("Ghost of "+ghost.getLabel()+" from "+f.getName());
                ghostFrame.addWindowListener(new BuffyTheGhostKiller(ghost));
                ghostFrame.setContentPane(ghostCE);
                ghostFrame.pack();
                ghostFrame.setLocationRelativeTo(session.playfieldFrame);
                ghostFrame.setVisible(true);
                session.windowsToClose.add(ghostFrame);
            }
            
            ghostLevel.snapshotState();
            gameLoop.addGhostLevel(ghostLevel);
            
            RobotUtils.updateRecentFiles(recentFiles, f);
            
        } catch (FileFormatException ex) {
            RobotUtils.showFileFormatException(ex);
        } catch (BackingStoreException ex) {
            System.out.println("Couldn't update user prefs");
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(session.playfieldFrame, "Load Failed: "+ex.getMessage());
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e1) {
                System.out.println("Bad luck.. couldn't close ghost input file!");
                e1.printStackTrace();
            }
        }
    }
    
    public void setGameLoop(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
    }

    public void setPlayfield(Playfield playfield) {
        this.playfield = playfield;
    }

    public void setRobots(Collection<Robot> robots) {
        this.robots = robots;
    }
    
    private class BuffyTheGhostKiller extends WindowAdapter {
        
        private Robot ghostToKill;
        
        public BuffyTheGhostKiller(Robot ghostToKill) {
            this.ghostToKill = ghostToKill;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            playfield.removeRobot(ghostToKill);
            gameLoop.removeRobot(ghostToKill);
            e.getWindow().dispose();
        }
    }
}