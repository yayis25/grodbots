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
