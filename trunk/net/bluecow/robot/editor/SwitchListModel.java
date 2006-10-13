/*
 * Created on Oct 12, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import net.bluecow.robot.LevelConfig;
import net.bluecow.robot.LevelConfig.Switch;

public class SwitchListModel extends AbstractListModel {

    private List<Switch> switches;
    
    public SwitchListModel(LevelConfig level) {
        // XXX this won't work if something other than this list model adds a switch to the level
        this.switches = new ArrayList<Switch>(level.getSwitches());
    }

    public Object getElementAt(int index) {
        if (index < 0 || index > switches.size()) {
            return null;
        } else {
            return switches.get(index);
        }
    }

    public int getSize() {
        return switches.size();
    }

}
