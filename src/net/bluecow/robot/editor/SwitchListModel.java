/*
 * Created on Oct 12, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.AbstractListModel;

import net.bluecow.robot.LevelConfig;
import net.bluecow.robot.LevelConfig.Switch;

public class SwitchListModel extends AbstractListModel {

    private final LevelConfig level;
    
    public SwitchListModel(LevelConfig levelConfig) {
        this.level = levelConfig;
        level.addPropertyChangeListener("switches", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                List<Switch> switches = level.getSwitches();
                fireContentsChanged(SwitchListModel.this, 0, switches.size());
            }
        });
    }

    public Object getElementAt(int index) {
        List<Switch> switches = level.getSwitches();
        if (index < 0 || index > switches.size()) {
            return null;
        } else {
            return switches.get(index);
        }
    }

    public int getSize() {
        return level.getSwitches().size();
    }

}
