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
