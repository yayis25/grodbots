/*
 * Created on Mar 19, 2008
 *
 * Copyright (c) 2008, Jonathan Fuerth
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

package net.bluecow.robot.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import net.bluecow.robot.sound.SoundManager;
import net.bluecow.robot.sound.SoundManagerEntry;
import net.bluecow.robot.sound.SoundManagerEntryEvent;
import net.bluecow.robot.sound.SoundManagerEntryEventListener;

/**
 * An adapter class that presents a SoundManager as a Swing Combo Box Model.
 * Changes to the sound manager's list of entries will be reflected in this
 * model.
 */
public class SoundManagerEntryComboBoxModel extends AbstractListModel implements ComboBoxModel {

    /**
     * Handles notifications about Sound Manager entry additions and removals, primarily
     * by refiring the events according to the {@link ComboBoxModel} contract. 
     */
    private final SoundManagerEntryEventListener soundManagerEntryEventHandler =
        new SoundManagerEntryEventListener() {
        
        public void soundManagerEntryAdded(SoundManagerEntryEvent e) {
            
            // simple insertion sort keeps entries list in order
            // if there were going to be thousands of SME's added, this would perform abysmally
            int idx = 0;
            while (idx < entries.size() &&
                    (entries.get(idx).getId().compareToIgnoreCase(e.getEntry().getId()) < 0)) {
                idx++;
            }
            entries.add(idx, e.getEntry());
            
            fireIntervalAdded(SoundManagerEntryComboBoxModel.this, idx, idx);
        }
        
        public void soundManagerEntryRemoved(SoundManagerEntryEvent e) {
            int idx = entries.indexOf(e.getEntry());
            entries.remove(idx);
            fireIntervalRemoved(SoundManagerEntryComboBoxModel.this, idx, idx);
        }
        
    };
    
    private final SoundManager sm;
    
    private SoundManagerEntry selectedItem;
    
    /**
     * Ordered list of entries in the sound manager. Kept in sync with the sound
     * manager's actual contents and maintained in alphabetical order by the
     * sound manager entry event handler.
     */
    private List<SoundManagerEntry> entries = new LinkedList<SoundManagerEntry>();
    
    /**
     * Creates a new combo box model of the given sound manager. This model will
     * register a listener with the sound manager; it is imperative that you
     * call {@link #cleanup()} when you are finished with this model.
     * 
     * @param sm
     *            The sound manager whose entries this combo box model models.
     */
    public SoundManagerEntryComboBoxModel(final SoundManager sm) {
        this.sm = sm;
        sm.addSoundManagerEntryListener(soundManagerEntryEventHandler);
        List<SoundManagerEntry> initialEntries = new ArrayList<SoundManagerEntry>(sm.getClips());
        Collections.sort(initialEntries, new Comparator<SoundManagerEntry>() {
            public int compare(SoundManagerEntry o1, SoundManagerEntry o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        entries.addAll(initialEntries);
    }

    public SoundManagerEntry getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(Object anItem) {
        selectedItem = (SoundManagerEntry) anItem;
    }

    public SoundManagerEntry getElementAt(int index) {
        // nasty out-of-bounds failsafe required by swing 
        if (index < 0 || index >= entries.size()) {
            return null;
        } else {
            return entries.get(index);
        }
    }

    public int getSize() {
        return entries.size();
    }
    
    /**
     * Relases resources consumed by this combo box model (for instance, cleanup
     * involves removing an entry from the Sound Manager's listener list). It is
     * important to call this method when you are finished with this model.
     */
    public void cleanup() {
        sm.removeSoundManagerEntryEventListener(soundManagerEntryEventHandler);
    }
}
