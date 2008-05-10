/*
 * Created on Apr 8, 2008
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

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.bluecow.robot.sound.SoundManager;

/**
 * The SoundManagerPanel manages a collection of GUI components that
 * lets users maintain a SoundManager's entries.
 * <p>
 * Note, his class isn't finished yet!
 */
public class SoundManagerPanel {

    /**
     * The table that visualizes the sound manager's entries.
     */
    private final JTable entryTable;
    
    /**
     * The Sound Manager this panel is looking at/modifying
     */
    private final SoundManager sm;
    
    /**
     * Creates a new panel for maintaining the given sound manager.
     */
    public SoundManagerPanel(SoundManager sm) {
        this.sm = sm;
        this.entryTable = new JTable(new SoundManagerEntryTableModel(sm));
        // TODO ensure the table model detaches from the sound manager when the panel is no longer in use
    }
    
    public JComponent getPanel() {
        return new JScrollPane(entryTable);
        // TODO add +/- buttons and wrap in panel
    }
}
