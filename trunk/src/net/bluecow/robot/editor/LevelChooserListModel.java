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
 * Created on Oct 10, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import net.bluecow.robot.GameConfig;
import net.bluecow.robot.LevelConfig;

public class LevelChooserListModel extends AbstractListModel implements ComboBoxModel {

    private final GameConfig gameConfig;

    /**
     * The currently-selected level.
     */
    private LevelConfig selectedItem;

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

    public LevelConfig getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(Object anItem) {
        selectedItem = (LevelConfig) anItem;
    }
}
