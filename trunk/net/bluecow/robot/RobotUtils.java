/*
 * Created on Mar 26, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class RobotUtils {
    
    private static Preferences prefs = Preferences.userNodeForPackage(RobotUtils.class);

    private RobotUtils() {
        throw new UnsupportedOperationException("This utility class is not instantiable");
    }
    
    //TODO: seems to only keep the latest item!  should keep MAX_PATHS items

    public static Preferences getPrefs() {
        return prefs;
    }
    
    /**
     * Updates a recent files list stored in the given preferences node. At
     * completion, the preferences key <code>"0"</code> will have
     * <code>f.getPath()</code> as its value. If this path was already in the
     * recent files list, it will have been promoted from its old position to 0.
     * Otherwise, the oldest entry will be bumped from the list, and all others
     * will have new keys <code>(i+1)</code>, where <code>i</code> was the
     * old key name.
     * 
     * @param prefs
     *            The preferences node to perform the update under.
     * @param baseKey
     *            The base key name.
     * @param f
     *            The most recently-accessed file.
     * @throws BackingStoreException 
     */
    public static void updateRecentFiles(Preferences prefs, File f) throws BackingStoreException {
        List<String> paths = new ArrayList<String>();
        for (int i = 0; i < RecentFilesMenu.MAX_RECENT_PATHS; i++) {
            paths.add(null);
        }
        for (String key : prefs.keys()) {
            try {
                int i = Integer.parseInt(key);
                paths.set(i, prefs.get(key, null));
            } catch (NumberFormatException e) {
                System.out.println(
                        "Discarding non-integer key '"+key+
                        "' at node "+prefs.absolutePath());
            } catch (IndexOutOfBoundsException e) {
                System.out.println(
                        "Discarding out-of-bounds key '"+key+
                        "' at node "+prefs.absolutePath()+
                        "(max recent files = "+RecentFilesMenu.MAX_RECENT_PATHS+")");
            }
        }
        
        System.out.println("Old recent files list: "+paths);
        
        String recentPath = f.getPath();
        if (paths.contains(recentPath)) {
            paths.remove(recentPath);
        } else {
            paths.remove(paths.size() - 1);
        }
        paths.add(0, recentPath);
        
        System.out.println("After update: "+paths);
        
        prefs.clear();
        
        int i = 0;
        for (String path : paths) {
            String key = String.valueOf(i++);
            System.out.println("Putting '"+key+"' -> '"+path+"'");
            if (path != null) prefs.put(key, path);
        }
    }
    
    public static void showFileFormatException(FileFormatException ex) {
        JOptionPane.showMessageDialog(null, 
                "Syntax error in project file:\n\n" +
                ex.getMessage() + "\n\n" +
                "at line "+ex.getLineNum()+" column "+ex.getBadCharPos()+": "+ex.getBadLine());
    }

    /**
     * Tries very hard to create a JDialog which is owned by the parent
     * Window of the given component.  However, if the component does not
     * have a Window ancestor, or the component has a Window ancestor that
     * is not a Frame or Dialog, this method instead creates an unparented
     * JDialog which is always-on-top.
     * 
     * @param owningComponent The component that should own this dialog.
     * @param title The title for the dialog.
     * @return A JDialog that is either owned by the Frame or Dialog ancestor of
     * owningComponent, or not owned but set to be alwaysOnTop.
     */
    public static JDialog makeOwnedDialog(Component owningComponent, String title) {
        Window owner = SwingUtilities.getWindowAncestor(owningComponent);
        if (owner instanceof Frame) {
            return new JDialog((Frame) owner, title);
        } else if (owner instanceof Dialog) {
            return new JDialog((Dialog) owner, title);
        } else {
            JDialog d = new JDialog();
            d.setTitle(title);
            d.setAlwaysOnTop(true);
            return d;
        }
    }
    
    public static void tileWindows(List<Window> windows) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Rectangle b = gc.getBounds();
        Insets i = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        
        System.out.println("gc="+gc+" (bounds="+b+", insets="+i+")");
        
        final int startx = b.x + i.left;
        final int starty = b.y + i.top;
        int x = startx;
        int y = starty;
        int nexty = y;
        
        for (Window w : windows) {
            Insets wi = w.getInsets();
            nexty = Math.max(nexty, w.getHeight() + wi.top + wi.bottom);
            if (x + w.getWidth() <= b.width - i.right) {
                // window will fit on this row
                w.setLocation(x, y);
                x = x + w.getWidth() + wi.left + wi.right;
            } else if (x == startx) {
                // window is too wide for the screen! just put it on its own row
                w.setLocation(x, y);
                y += w.getHeight() + wi.top + wi.bottom;
            } else {
                // window won't fit on this row
                x = startx;
                y = nexty;
                w.setLocation(x, y);
            }
            if (y >= b.height - i.bottom) {
                System.out.println("Warning: couldn't tile all windows on screen. There will be overlaps.");
                x = b.x + i.left;
                y = b.y + i.top;
                nexty = y;
            }
        }
    }
}
