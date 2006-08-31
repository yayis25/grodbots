/*
 * Created on Aug 28, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataListener;

import net.bluecow.robot.GameConfig;
import net.bluecow.robot.GameConfig.SquareConfig;

public class SquareChooserListModel extends AbstractListModel {

    /**
     * The GameConfigListener listens to the game config for updates to the
     * list of square types, and fires a list model event when necessary.
     */
    public class GameConfigListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            squares.clear();
            fireIntervalRemoved(SquareChooserListModel.this, 0, getSize());
            squares.addAll(gameConfig.getSquareTypes());
            fireIntervalAdded(SquareChooserListModel.this, 0, getSize());
        }

    }

    private GameConfig gameConfig;

    private List<SquareConfig> squares;
    
    public SquareChooserListModel(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
        squares = new ArrayList<SquareConfig>();
        squares.addAll(gameConfig.getSquareTypes());
        gameConfig.addPropertyChangeListener("squareTypes", new GameConfigListener());
    }
    
    public Object getElementAt(int index) {
        return squares.get(index);
    }

    public int getSize() {
        return squares.size();
    }

}
