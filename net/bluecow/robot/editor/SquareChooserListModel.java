/*
 * Created on Aug 28, 2006
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
import net.bluecow.robot.GameConfig.SquareConfig;

public class SquareChooserListModel extends AbstractListModel {

    /**
     * The GameConfigListener listens to the game config for updates to the
     * list of square types, and fires a list model event when necessary.
     */
    public class GameConfigListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            //System.out.println("Game property changed. Clearing square list...");
            squares.clear();
            fireIntervalRemoved(SquareChooserListModel.this, 0, getSize());
            //System.out.println("Repopulating square list...");
            squares.addAll(gameConfig.getSquareTypes());
            fireIntervalAdded(SquareChooserListModel.this, 0, getSize());
            //System.out.println("Done");
        }

    }

    private GameConfigListener listener = new GameConfigListener();
    
    private GameConfig gameConfig;

    private List<SquareConfig> squares;
    
    public SquareChooserListModel(GameConfig gameConfig) {
        setGame(gameConfig);
    }
    
    /**
     * Returns the element at the given index, or null if the index
     * is out of range.
     */
    public Object getElementAt(int index) {
        // we need this special treatment because JList may not be notified
        // of list item removals before other listeners have been notified,
        // and those listeners might ask the JList for the selected item, which
        // will cause out-of-bounds access to this list.
        if (index < 0 || index > squares.size()) {
            return null;
        } else {
            return squares.get(index);
        }
    }

    public int getSize() {
        return squares.size();
    }

    public void setGame(GameConfig gameConfig) {
        if (this.gameConfig != null) {
            this.gameConfig.removePropertyChangeListener(listener);
        }
        this.gameConfig = gameConfig;
        squares = new ArrayList<SquareConfig>();
        squares.addAll(gameConfig.getSquareTypes());
        gameConfig.addPropertyChangeListener("squareTypes", listener);
        System.out.println("setGame(): Refreshed square types to "+squares);
        fireContentsChanged(this, 0, getSize());
    }
}
