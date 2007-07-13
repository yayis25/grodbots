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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

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
    
    /**
     * Presents a modal dialog with a nicely-formatted error message which reflects
     * the given FileFormatException.
     */
    public static void showFileFormatException(FileFormatException ex) {
        JOptionPane.showMessageDialog(null, 
                "Syntax error in project file:\n\n" +
                ex.getMessage() + "\n\n" +
                "at line "+ex.getLineNum()+" column "+ex.getBadCharPos()+": "+ex.getBadLine());
    }

    /**
     * Presents a modal dialog displaying the given message and information
     * about the given exception.  Also prints a stack trace to stderr.
     */
    public static void showException(String message, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, message+"\n\n"+ex+"\n");
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
    
    /**
     * Attempts to tile the given list of windows on the main display, starting
     * with the first window in the list in the top left-hand corner of the
     * display.  This implementation doesn't behave well when there are more windows
     * than can be distributed across the display in this manner, or when there is a
     * window in the list which is taller or wider than the display.
     *   
     * @param windows
     */
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

            // Note, this logic doesn't cover all possible cases. For instance,
            // windows that are wider than the screen, or the possibility of
            // having to start a row below the bottom the screen.
            
            if (x + w.getWidth() <= b.width - i.right) {
                // window will fit on this row
                w.setLocation(x, y);
                x = x + w.getWidth() + wi.left + wi.right;
            } else {
                // window won't fit on this row
                x = startx;
                y = nexty;
                w.setLocation(x, y);
            }
        }
    }

    /**
     * Returns a file filter that accepts only pathnames ending with ".rlp"
     */
    public static FileFilter createLevelPackFilter() {
        return new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.toString().endsWith(".rlp");
            }

            @Override
            public String getDescription() {
                return "Robot Level Packs";
            }
        };
    }

    /**
     * Creates a String representation of the given list where the String
     * value of each item in the list is on its own line.
     * <p>
     * Note: Presently, no checking is done to ensure the String representations
     * of the list items themselves are free of newline characters, so consider
     * this transformation non-invertible.  It's intended for diagnostics and
     * debugging.
     *  
     * @param list The list to list.
     * @return A String containing the String representations of all the items
     * in the given list, in order, separated by the newline character '\n'. 
     */
    public static String listOnSeparateLines(List<? extends Object> list) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (Object item : list) {
            if (!first) {
                sb.append('\n');
            } else {
                first = false;
            }
            sb.append(item);
        }
        return sb.toString();
    }

}
