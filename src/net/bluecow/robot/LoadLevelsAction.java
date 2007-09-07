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
import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.bluecow.robot.resource.ResourceLoader;
import net.bluecow.robot.resource.ZipFileResourceLoader;

/**
 * The LoadLevelsAction replaces the game config of the given session with
 * one loaded from a "rlp" (robot level pack) file chosen by the user.
 *
 * @author fuerth
 * @version $Id:$
 */
class LoadLevelsAction extends AbstractAction {
    
    /**
     * The game session this action loads a level config for.
     */
    private final Main session;
    
    JFileChooser fc;
    
    public LoadLevelsAction(Main main) {
        super("Open Levels...");
        session = main;
        putValue(MNEMONIC_KEY, KeyEvent.VK_L);
        fc = new JFileChooser();
        fc.setDialogTitle("Choose a Robot Levels File");
    }
    
    public void actionPerformed(ActionEvent e) {
        Preferences recentFiles = RobotUtils.getPrefs().node("recentGameFiles");
        
        File f = new File(e.getActionCommand() == null ? "!@#$%^&*" : e.getActionCommand());
        try {
            if ( ! (f.isFile() && f.canRead()) ) {
                fc.setCurrentDirectory(new File(recentFiles.get("0", System.getProperty("user.home"))));
                int choice = fc.showOpenDialog(null);
                if (choice != JFileChooser.APPROVE_OPTION) return;
                f = fc.getSelectedFile();
            }
            RobotUtils.updateRecentFiles(recentFiles, f);
            ResourceLoader resourceLoader = new ZipFileResourceLoader(f);
            session.loadGameConfig(resourceLoader);
            session.setLevel(0);
        } catch (BackingStoreException ex) {
            System.out.println("Couldn't update user prefs");
            ex.printStackTrace();
        } catch (FileFormatException ex) {
            RobotUtils.showFileFormatException(ex);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Couldn't load the levels:\n\n"
                    +ex.getMessage()+"\n\n"
                    +"A stack trace is available on the Java Console.",
                    "Load Error", JOptionPane.ERROR_MESSAGE, null);
        }
    }
}