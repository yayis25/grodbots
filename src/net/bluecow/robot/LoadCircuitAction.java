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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * The LoadCircuitAction, when invoked, prompts the user for a file which contains
 * circuit descriptions, then replaces all the circuits for the collection of robots
 * it was created with with those in the circuit file.  Matching of circuit descriptions
 * to robots is performed by robot name.
 *
 * @author fuerth
 * @version $Id:$
 */
class LoadCircuitAction extends AbstractAction {

    /**
     * The component that owns dialogs created by this action.
     */
    private final Component owner;

    /**
     * The robots that the circuit should be loaded into.
     */
    private final Collection<Robot> robots;
    
    public LoadCircuitAction(Component owner, Collection<Robot> robots) {
        super("Open Circuit...");
        putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        this.robots = robots;
        this.owner = owner;
    }
    
    public void actionPerformed(ActionEvent e) {
        InputStream in = null;
        Preferences recentFiles = RobotUtils.getPrefs().node("recentCircuitFiles");
        try {
            File f = new File(e.getActionCommand() == null ? "!@#$%^&*" : e.getActionCommand());
            if ( ! (f.isFile() && f.canRead()) ) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Open Circuit Description File");
                fc.setCurrentDirectory(new File(recentFiles.get("0", System.getProperty("user.home"))));
                int choice = fc.showOpenDialog(owner);
                if (choice != JFileChooser.APPROVE_OPTION) return;
                f = fc.getSelectedFile();
            }
            in = new FileInputStream(f);
            for (Robot r : robots) {
                r.getCircuit().removeAllGates();
            }
            CircuitStore.load(in, robots);
            RobotUtils.updateRecentFiles(recentFiles, f);
        } catch (BackingStoreException ex) {
            System.out.println("Couldn't update user prefs");
            ex.printStackTrace();
        } catch (FileFormatException ex) {
            RobotUtils.showFileFormatException(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(owner, "Load Failed: "+ex.getMessage());
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException ex) {
                System.out.println("Bad luck.. couldn't close input file!");
                ex.printStackTrace();
            }
        }
    }
}