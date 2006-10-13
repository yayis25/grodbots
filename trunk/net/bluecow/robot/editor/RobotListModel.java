/*
 * Created on Oct 12, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.util.List;

import javax.swing.AbstractListModel;

import net.bluecow.robot.LevelConfig;
import net.bluecow.robot.Robot;

public class RobotListModel extends AbstractListModel {

    private LevelConfig level;
    
    public RobotListModel(LevelConfig level) {
        // XXX need a listener to track updates to the level's robot list
        this.level = level;
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
