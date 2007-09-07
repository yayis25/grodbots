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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * The SaveCircuitAction, when invoked, prompts the user for a target file, then
 * saves out a description of all circuits in the collection of robots this action
 * was created with.  All of the circuits are saved to the same file, and the name
 * of each robot is saved to that file so the loaded circuits can be associated with
 * the correct robots.
 *
 * @author fuerth
 * @version $Id:$
 */
class SaveCircuitAction extends AbstractAction {
    
    /**
     * The component that owns dialogs created by this action.
     */
    private final Component owner;
    
    /**
     * The robots whose circuits should be saved out.
     */
    private final Collection<Robot> robots;
    
    public SaveCircuitAction(Component owner, Collection<Robot> robots) {
        super("Save Circuit...");
        putValue(MNEMONIC_KEY, KeyEvent.VK_S);
        this.owner = owner;
        this.robots = robots;
    }
    
    public void actionPerformed(ActionEvent e) {
        Preferences recentFiles = RobotUtils.getPrefs().node("recentCircuitFiles");
        OutputStream out = null;
        try {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save Circuit Description File");
            fc.setCurrentDirectory(new File(recentFiles.get("0", System.getProperty("user.home"))));
            int choice = fc.showSaveDialog(owner);
            if (choice == JFileChooser.APPROVE_OPTION) {
                out = new FileOutputStream(fc.getSelectedFile());
                CircuitStore.save(out, robots);
                RobotUtils.updateRecentFiles(recentFiles, fc.getSelectedFile());
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(owner, "Save Failed: "+ex.getMessage());
            ex.printStackTrace();
        } catch (BackingStoreException ex) {
            System.err.println("Couldn't update user prefs");
            ex.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException e1) {
                System.err.println("Bad luck.. couldn't close output file!");
                e1.printStackTrace();
            }
        }
    }
}