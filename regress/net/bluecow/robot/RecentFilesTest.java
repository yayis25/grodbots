/*
 * Created on Mar 29, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.robot;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

public class RecentFilesTest extends TestCase {

    Preferences prefs;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        prefs = Preferences.userRoot().node("net/bluecow/robot/test");
    }

    @Override
    protected void tearDown() throws Exception {
        prefs.removeNode();
        super.tearDown();
    }
    
    public void testAddNewPath() throws BackingStoreException {
        File homedir = new File(System.getProperty("user.home"));
        assertEquals(0, prefs.childrenNames().length);
        
        RobotUtils.updateRecentFiles(prefs, homedir);
        assertEquals(homedir, new File(prefs.get("0", "default")));
        
        File newfile = new File(homedir, "foo");
        RobotUtils.updateRecentFiles(prefs, newfile);
        assertEquals(newfile, new File(prefs.get("0", "default")));

        newfile = new File(homedir, "bar");
        RobotUtils.updateRecentFiles(prefs, newfile);
        assertEquals(newfile, new File(prefs.get("0", "default")));
    }
    
    public void testPromoteOldPath() throws BackingStoreException {
        File homedir = new File(System.getProperty("user.home"));
        assertEquals(0, prefs.childrenNames().length);
        
        RobotUtils.updateRecentFiles(prefs, homedir);
        RobotUtils.updateRecentFiles(prefs, new File(homedir, "foo"));
        RobotUtils.updateRecentFiles(prefs, new File(homedir, "bar"));

        assertEquals(homedir, new File(prefs.get("2", "default")));
        assertEquals(new File(homedir, "foo"), new File(prefs.get("1", "default")));
        assertEquals(new File(homedir, "bar"), new File(prefs.get("0", "default")));

        // this is already in the list; it should float up to the top
        RobotUtils.updateRecentFiles(prefs, new File(homedir, "foo"));
        assertEquals(homedir, new File(prefs.get("2", "default")));
        assertEquals(new File(homedir, "bar"), new File(prefs.get("1", "default")));
        assertEquals(new File(homedir, "foo"), new File(prefs.get("0", "default")));
    }
}
