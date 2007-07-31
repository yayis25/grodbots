/*
 * Created on Jul 30, 2007
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

package net.bluecow.robot.editor.resource;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import net.bluecow.robot.RobotUtils;
import net.bluecow.robot.resource.ResourceManager;

/**
 * The CreateDirectoryAction creates a new directory in the resource
 * manager when it is invoked.  It presents several dialogs to get
 * the location and name from the user, which sucks.  One day, I'll
 * make a better interface that just creates the dir and lets you
 * rename it in the tree.
 *
 * @author fuerth
 * @version $Id:$
 */
public class CreateDirectoryAction extends ResourceEditorAction {

    public CreateDirectoryAction(ResourceManager resourceManager,
                                 JComponent owningComponent) {
        super("New Directory...", resourceManager, owningComponent);
    }

    public void actionPerformed(ActionEvent e) {
        String targetDir = promptForTargetDir(e.getActionCommand());
        if (targetDir == null) return;
        
        String newDirName = JOptionPane.showInputDialog(
                owningComponent, "What name will the new directory have?");
        
        try {
            resourceManager.createDirectory(targetDir, newDirName);
        } catch (Exception ex) {
            RobotUtils.showException("Couldn't create directory", ex);
        }
    }

}
