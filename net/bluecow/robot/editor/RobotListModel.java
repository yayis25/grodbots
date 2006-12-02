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
import net.bluecow.robot.Robot;

public class RobotListModel extends AbstractListModel {

    private final LevelConfig level;
    
    public RobotListModel(LevelConfig level) {
        this.level = level;
        level.addPropertyChangeListener("robots", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                fireContentsChanged(RobotListModel.this, 0, RobotListModel.this.level.getRobots().size());
            }
        });
    }

    public Object getElementAt(int index) {
        final List<Robot> robots = level.getRobots();
        if (index < 0 || index > robots.size()) {
            return null;
        } else {
            return robots.get(index);
        }
    }

    public int getSize() {
        return level.getRobots().size();
    }

}
