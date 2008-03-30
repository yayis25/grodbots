/*
 * Created on Mar 21, 2008
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
 * SPECIAL, EXEMPLARY, OR CONS  EQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.bluecow.robot.sound;

/**
 * Event class that represents additions to or removals from a SoundManager.
 * 
 * @see SoundManagerEntryEventListener
 */
public class SoundManagerEntryEvent {

    /**
     * The sound manager the entry was added to or removed from.
     */
    private final SoundManager source;

    /**
     * The sound manager entry that was added or removed.
     */
    private final SoundManagerEntry entry;
    
    /**
     * Creates a new event about entry being either added to or removed
     * from the given sound manager.
     */
    public SoundManagerEntryEvent(final SoundManager source, final SoundManagerEntry entry) {
        this.source = source;
        this.entry = entry;
    }

    /**
     * Returns the sound manager entry that was added or removed.
     */
    public SoundManagerEntry getEntry() {
        return entry;
    }

    /**
     * Returns the sound manager the entry was added to or removed from.
     */
    public SoundManager getSource() {
        return source;
    }
    
}
