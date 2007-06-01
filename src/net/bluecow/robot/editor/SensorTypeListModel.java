/*
 * Created on Aug 30, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import net.bluecow.robot.GameConfig;
import net.bluecow.robot.GameConfig.SensorConfig;

public class SensorTypeListModel extends AbstractListModel {

    /**
     * The GameConfigListener listens to the game config for updates to the
     * list of square types, and fires a list model event when necessary.
     */
    public class GameConfigListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            sensorTypes.clear();
            fireIntervalRemoved(SensorTypeListModel.this, 0, getSize());
            sensorTypes.addAll(gameConfig.getSensorTypes());
            fireIntervalAdded(SensorTypeListModel.this, 0, getSize());
        }

    }

    private final GameConfigListener listener = new GameConfigListener();
    
    private final GameConfig gameConfig;

    private final List<SensorConfig> sensorTypes;
    
    public SensorTypeListModel(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
        sensorTypes = new ArrayList<SensorConfig>();
        sensorTypes.addAll(gameConfig.getSensorTypes());
        gameConfig.addPropertyChangeListener("sensorTypes", listener);
    }
    
    public Object getElementAt(int index) {
        return sensorTypes.get(index);
    }

    public int getSize() {
        return sensorTypes.size();
    }

}
