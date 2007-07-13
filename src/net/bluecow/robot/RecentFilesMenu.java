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
 * Created on Mar 26, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.io.File;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class RecentFilesMenu extends JMenu {
    
    public static final int MAX_RECENT_PATHS = 5;

    private class ItemUpdater implements PreferenceChangeListener {
        public void preferenceChange(PreferenceChangeEvent evt) {
            refreshItemsFromPrefs();
        }
    }
    
    private Preferences prefNode;
    private Action loadAction;
    
    public RecentFilesMenu(String label, Action loadAction, Preferences recentFilesPrefNode) {
        super(label);
        this.loadAction = loadAction;
        this.prefNode = recentFilesPrefNode;
        recentFilesPrefNode.addPreferenceChangeListener(new ItemUpdater());
        refreshItemsFromPrefs();
    }

    private void refreshItemsFromPrefs() {
        removeAll();
        for (int i = 0; i < MAX_RECENT_PATHS; i++) {
            String path;
            if ( (path = prefNode.get(String.valueOf(i), null)) != null) {
                JMenuItem item = new JMenuItem(loadAction);
                item.setActionCommand(path);
                item.setText(new File(path).getName());
                add(item);
            }
        }
    }
}
