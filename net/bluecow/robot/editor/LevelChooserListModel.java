/*
 * Created on Oct 10, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.AbstractListModel;

import net.bluecow.robot.GameConfig;
import net.bluecow.robot.LevelConfig;

public class LevelChooserListModel extends AbstractListModel {

    private final GameConfig gameConfig;

    public LevelChooserListModel(GameConfig gc) {
        this.gameConfig = gc;
        gameConfig.addPropertyChangeListener("levels", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                fireContentsChanged(LevelChooserListModel.this, 0, gameConfig.getLevels().size());
            }
        });
        
        PropertyChangeListener levelNameListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("name".equals(evt.getPropertyName())) {
                    int index = gameConfig.getLevels().indexOf(evt.getSource());
                    if (index >= 0) {
                        fireContentsChanged(LevelChooserListModel.this, index, index);
                    }
                }
            }
        };
        for (LevelConfig level : gc.getLevels()) {
            level.addPropertyChangeListener(levelNameListener);
        }
    }
    
    /**
     * Returns the value of the list element at index, or null if the
     * index is invalid.  This "failsafe" behaviour is customary in Swing
     * model classes, and not something I normally like to do.
     */
    public Object getElementAt(int index) {
        final List<LevelConfig> levels = gameConfig.getLevels();
        if (index < 0 || index >= levels.size()) {
            return null;
        } else {
            return levels.get(index);
        }
    }

    public int getSize() {
        return gameConfig.getLevels().size();
    }
}
