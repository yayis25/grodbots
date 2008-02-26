/*
 * Created on Jan 9, 2008
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

package net.bluecow.robot.sound;

import javax.sound.sampled.LineUnavailableException;

/**
 * The BackgroundMusic interface is analogous to the JavaSound Clip interface,
 * but it is designed to also accomodate playback of background music. The
 * background music can have various endings, which are selected at the time the
 * game engine stops muysic playback.
 * <p>
 * Implementations of this interface are also responsible for remembering
 * information about each clip that's necessary for recreating it. This is
 * useful when, for instance, you're saving the game config to an XML file.
 * 
 * @author fuerth
 * @version $Id:$
 */
public interface SoundManagerEntry {

    public enum EntryType {
        
        /**
         * Represents an entry backed by a JavaSound Clip instance.  The file
         * formats supported for this type of entry depend on which JavaSound
         * modules are installed, but WAV and AIFF are always supported.
         */
        CLIP,
        
        /**
         * Represents an entry backed by a {@link ModMusic} instance.  ModMusic
         * uses the IBXM library for playback, which supports XM, S3M, and
         * ProTracker MOD files.
         */
        MOD;
    }
    
    /**
     * Begins playback of the music clip, looping it at an appropriate point
     * so the music will play indefinitely.
     * 
     * @throws LineUnavailableException if the JavaSound audio system cannot
     * provide the required audio playback line
     */
    public void startPlaying(boolean loop) throws LineUnavailableException;
    
    /**
     * Stops playback of the music with an optional finish-up song.  This can
     * be useful when the music has to end because the player lost a life,
     * reached the goal, or ran out of time (for example). The names of the
     * different endings, and the effect they have while stopping playback,
     * are configured when the song is loaded. You might want to use endings
     * named "die", "win", or "timeup", but the choice is yours.  Within a
     * game, it would be a good idea to come up with a naming convention and
     * stick with it.
     * 
     * @param ending The name of the pre-set ending to jump to and play out
     * before the music stops.  If null, the music will stop right away.
     */
    public void stopPlaying(String ending);
    
    /**
     * Permanently closes this background music instance. Once closed, the music
     * cannot be resumed or restarted. It is important to close every background
     * music instance once you are finished with it, because it may be holding
     * valuable system resources such as threads, open files, and so on.
     */
    public void close();

    /**
     * Returns this clip's unique name.
     */
    public String getId();

    /**
     * The resource this clip can be loaded from (using the resource loader of
     * the sound manager this clip belongs to).
     */
    public String getPath();

    /**
     * Returns the entry type for this SoundManagerEntry.  Valid types are enumerated
     * in {@link EntryType}.
     */
    public EntryType getType();

}
