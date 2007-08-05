/*
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
/*
 * Created on Apr 20, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor.resource;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import net.bluecow.robot.RobotUtils;
import net.bluecow.robot.resource.ResourceManager;

public class CreateResourceAction extends ResourceEditorAction {

    public CreateResourceAction(ResourceManager resourceManager, JComponent owningComponent) {
        super("Create Resource...", resourceManager, owningComponent);
    }
    
    public void actionPerformed(ActionEvent e) {
        
        
        // need to get target path for resource
        String defaultTargetDir = e.getActionCommand();
        String targetdir = promptForTargetDir(defaultTargetDir);
        
        Preferences prefs = RobotUtils.getPrefs().node("recentResourceFiles");
        String mostRecentResource = prefs.get("0", null);
        
        JFileChooser fc = new JFileChooser();
        if (mostRecentResource != null) {
            File f = new File(mostRecentResource);
            if (f.exists()) {
                fc.setCurrentDirectory(f);
            }
        }
        
        int choice = fc.showOpenDialog(owningComponent);
        if (choice != JFileChooser.APPROVE_OPTION) return;
        File inFile = fc.getSelectedFile();
        InputStream in = null;
        OutputStream out = null;
        
        try {
            in = new BufferedInputStream(new FileInputStream(inFile));
            out = resourceManager.openForWrite(targetdir + inFile.getName(), true);

            byte[] buf = new byte[8192];
            int count;
            while ( (count = in.read(buf)) > 0 ) {
                out.write(buf, 0, count);
            }
        } catch (IOException ex) {
            RobotUtils.showException("Couldn't create resource", ex);
        } finally {
            try { in.close(); } catch (IOException ex) { ex.printStackTrace(); }
            try { out.flush(); } catch (IOException ex) { ex.printStackTrace(); }
            try { out.close(); } catch (IOException ex) { ex.printStackTrace(); }
        }
    }

}
