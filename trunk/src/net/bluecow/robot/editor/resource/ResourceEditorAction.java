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

import java.awt.HeadlessException;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import net.bluecow.robot.resource.ResourceManager;
import net.bluecow.robot.resource.ResourceUtils;

/**
 * Base class for actions that belong to the resource editor.  Provides
 * an easy constructor for setting up the description, resource manager,
 * and GUI component owner for the action.
 *
 * @version $Id:$
 */
public abstract class ResourceEditorAction extends AbstractAction {
    
    /**
     * The component whose containing window will own any dialogs created by this action.
     */
    protected final JComponent owningComponent;
    
    /**
     * The resource manager that this action adds resources to.
     */
    protected final ResourceManager resourceManager;

    /**
     * Initialises this abstract action with the given properties.
     * None of them may be null.
     */
    protected ResourceEditorAction(
            String description,
            ResourceManager resourceManager,
            JComponent owningComponent) {
        
        super(description);
        if (description == null) throw new NullPointerException("Null description");
        
        if (resourceManager == null) throw new NullPointerException("Null resource manager");
        this.resourceManager = resourceManager;

        if (owningComponent == null) throw new NullPointerException("Null owning component");
        this.owningComponent = owningComponent;
    }


    /**
     * Shows a modal dialog prompt for an existing directory in this
     * action's resource manager.
     * 
     * @param defaultTargetDir The default target directory to use.  If null,
     * the root directory will be the default target.
     * @return The resource directory selected by the user, or null if the
     * user cancels the dialog.
     */
    protected String promptForTargetDir(String defaultTargetDir) throws HeadlessException {
        JComboBox cb = new JComboBox(
                new ResourcesComboBoxModel(resourceManager, ResourceUtils.directoryOnlyFilter()));
        if (defaultTargetDir != null) {
            cb.setSelectedItem(defaultTargetDir);
        }
        int choice = JOptionPane.showOptionDialog(
                owningComponent, cb, "Choose a target directory in the project",
                JOptionPane.OK_CANCEL_OPTION, -1, null, null, 0);
        if (choice != JOptionPane.OK_OPTION) return null;
        
        return (String) cb.getSelectedItem();
    }
}
