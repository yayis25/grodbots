/*
 * Created on Oct 19, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Closes and disposes the window you give it when its action is performed.
 *
 * @author fuerth
 * @version $Id$
 */
public class DialogCancelAction extends AbstractAction {

    private Window window;

    public DialogCancelAction(Window w) {
        this.window = w;
    }

    public void actionPerformed(ActionEvent e) {
        window.setVisible(false);
        window.dispose();
    }

}