/*
 * Created on May 10, 2008
 *
 * Copyright (c) 2008, Jonathan Fuerth
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

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.bluecow.version.Version;

/**
 * Action to show a dialog with some info about the game.
 */
public class AboutAction extends AbstractAction {

    /**
     * The version number for the game. This is a dumb place to put it. If you
     * end up referencing this variable from outside the AboutDialog, think
     * about moving it somewhere more reasonable.
     */
    public static final Version VERSION = new Version("1.0beta");
    
    /**
     * The component that will own the popup dialog when it gets displayed.
     */
    private final Component owner;
    
    /**
     * Creates a new action for displaying the about dialog with the given
     * owning component.
     */
    public AboutAction(Component owner) {
        super("About...");
        this.owner = owner;
    }
    
    public void actionPerformed(ActionEvent e) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><h1>GrodBots</h1>");
        sb.append("<p>Version ").append(VERSION);
        sb.append("<p>By Jonathan Fuerth");
        sb.append("<dl><dt>Programming<dd>Jonathan Fuerth");
        sb.append("    <dt>Graphics<dd>Gordon Gere");
        sb.append("    <dt>Level design for <i>White Means Dance</i><dd>Dallas Gow");
        sb.append("    <dt>All other levels<dd>Jonathan Fuerth");
        sb.append("    <dt>Sound Effects<dd>Andrew Kilpatrick");
        sb.append("    <dt>Music<dd>Jonathan Fuerth");
        sb.append("</dl>");
        sb.append("<p>Special Thanks To:");
        sb.append("<p>Pat Neimier for BeanShell (defines robot and switch behaviours)");
        sb.append("<p>mumart for ibxm (XM music module playback)");
        sb.append("<p>And the testers: Dallas, Gordon, Andrew, Dan, Derek");
        
        JOptionPane.showMessageDialog(owner, sb.toString());
    }
}
