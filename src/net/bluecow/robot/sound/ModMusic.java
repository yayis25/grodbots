/*
 * Created on Jan 30, 2008
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

import ibxm.FastTracker2;
import ibxm.IBXM;
import ibxm.Module;
import ibxm.ProTracker;
import ibxm.ScreamTracker3;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import net.bluecow.robot.resource.ResourceLoader;
import net.bluecow.robot.resource.SystemResourceLoader;

/**
 * ModMusic is a BackgroundMusic implementation that uses the IBXM mod player
 * library to play background music from a MOD, XM, or S3M music module.
 * <p>
 * The code that makes up this implementation was originally copied directly
 * from the IBXM Player class, which was obtained under the BSD license. It has
 * since been rearranged to work as a SoundManagerEntry implementation.
 * 
 * @author fuerth
 * @version $Id:$
 */
public class ModMusic extends AbstractSoundManagerEntry {

    /**
     * Controls the debugging features of this class.
     */
    private static final boolean debugOn = true;
    
    /**
     * The time this class was loaded (for debugging purposes).
     */
    private static final long startTime = System.currentTimeMillis();
    
    /**
     * Prints the given message to System.out if debugOn is true.
     */
    private static void debug(String msg) {
        if (debugOn) System.out.println((System.currentTimeMillis() - startTime) + " " + msg);
    }

    /**
     * Prints the given printf-formatted message, followed by a newline,
     * to the console if debugOn == true.
     */
    private static void debugf(String fmt, Object ... args) {
        if (debugOn) debug(String.format(fmt, args));
    }
    
    /**
     * The SongPosition class represents a specific playback position in a mod.
     * It specifies a specific pattern within the sequence of patterns in the mod,
     * and a line number within that pattern.
     */
    public static class SongPosition {
        
        /**
         * The name given to this SongPosition.
         */
        private final String name;
        
        /**
         * The position within the mod's sequence. The first sequence index is
         * 0.
         */
        private final int sequenceIndex;

        /**
         * The offset (number of lines from the top) within the pattern
         * specified by {@link #sequenceIndex}.
         */
        private final int offset;

        /**
         * The amount of time, in milliseconds, to continue playing from the given
         * position.
         */
        private final long duration;
        
        /**
         * Creates a new SongPosition object with the given settings. Once
         * created, the settings for this instance cannot be changed.
         * 
         * @param sequenceIndex
         *            The position within the mod's sequence. The first sequence
         *            index is 0.
         * @param offset
         *            The offset (number of lines from the top) within the
         *            pattern specified by sequenceIndex.
         */
        public SongPosition(String name, int sequenceIndex, int offset, long duration) {
            this.name = name;
            this.sequenceIndex = sequenceIndex;
            this.offset = offset;
            this.duration = duration;
        }

        /**
         * The name given to this SongPosition.
         */
        public String getName() {
            return name;
        }
        
        /**
         * The offset (number of lines from the top) within the pattern
         * specified by {@link #getSequenceIndex()}.
         */
        public int getOffset() {
            return offset;
        }

        /**
         * The position within the mod's sequence. The first sequence index is
         * 0.
         */
        public int getSequenceIndex() {
            return sequenceIndex;
        }
        
        /**
         * The amount of time, in milliseconds, to continue playing after
         * jumping to the specified pattern and offset.
         */
        public long getDuration() {
            return duration;
        }
    }
    
    /**
     * The module loaded in the constructor. This is the music module that
     * will be played by this ModMusic instance.
     */
    private final Module module;

    /**
     * The "interpreter" that produces the playback samples from the module.
     */
    private final IBXM ibxm;

    /**
     * The length of the module in frames.  This value determines when to loop back
     * to the beginning of the module during playback.
     */
    private final int song_duration;

    /**
     * Maps ending names to the corresponding pattern number within the MOD.
     */
    private final Map<String, SongPosition> endings = new HashMap<String, SongPosition>();
    
    private int framesPlayed;

    private int remainingFrames;

    private PlayerThread playerThread;

    private SourceDataLine output_line;
    
    /**
     * Whether or not the music should loop to provide continuous
     * playback. If true, it will; if false, the music will play once
     * through then stop on its own.
     */
    private boolean looping;
    
    
    public ModMusic(ResourceLoader resourceLoader, String name, String modResourcePath) throws IOException, LineUnavailableException {
        super(name, modResourcePath);
        DataInputStream data_input_stream;
        byte[] xm_header, s3m_header, mod_header;
        data_input_stream = new DataInputStream( resourceLoader.getResourceAsStream(modResourcePath) );
        
        xm_header = new byte[ 60 ];
        data_input_stream.readFully( xm_header );
        if( FastTracker2.is_xm( xm_header ) ) {
            module = FastTracker2.load_xm( xm_header, data_input_stream );
        } else {
            s3m_header = new byte[ 96 ];
            System.arraycopy( xm_header, 0, s3m_header, 0, 60 );
            data_input_stream.readFully( s3m_header, 60, 36 );
            if( ScreamTracker3.is_s3m( s3m_header ) ) {
                module = ScreamTracker3.load_s3m( s3m_header, data_input_stream );
            } else {
                mod_header = new byte[ 1084 ];
                System.arraycopy( s3m_header, 0, mod_header, 0, 96 );
                data_input_stream.readFully( mod_header, 96, 988 );
                module = ProTracker.load_mod( mod_header, data_input_stream );
            }
        }
        data_input_stream.close();
        
        ibxm = new IBXM( 48000 );
        ibxm.set_module( module );
        song_duration = ibxm.calculate_song_duration();

        AudioFormat output_format = new AudioFormat( 48000, 16, 2, true, false );
        output_line = AudioSystem.getSourceDataLine( output_format );
        output_line.open();
        output_line.start();

        playerThread = new PlayerThread();
        playerThread.start();
    }
    
    private class PlayerThread extends Thread {
        
        /**
         * Whether this thread should currently be playing music. When true,
         * playback proceeds; when false, playback is paused. Use the
         * {@link #startPlaying(boolean)} and {@link #stopPlaying()} methods to
         * manipulate this variable, because they properly handle
         * synchronization between threads.
         */
        private boolean playing = false;
        
        /**
         * When true, this thread will terminate at its earliest opportunity.
         * Once terminated, it cannot be restarted. Use the {@link #terminate()}
         * method to set this flag, because it properly handles synchronization
         * between threads.
         */
        private boolean terminated = false;

        /**
         * The point in time (according to {@link System#currentTimeMillis()}) to
         * stop playback, if playback is in progress. Once playback has stopped
         * due to this point in time having passed, this value will be reset to
         * Long.MAX_VALUE, which will prevent immediate retriggering (unless the
         * date is past August 17, 292278994).
         */
        private long stopTime = Long.MAX_VALUE;
        
        @Override
        public void run() {
            while (!terminated) {
                do {
                    framesPlayed = 0;
                    remainingFrames = song_duration;
                    ibxm.set_sequence_index(0, 0);
                    while (remainingFrames > 0) {
                        synchronized (this) {
                            checkStopTime();
                            if (!playing) break;
                        }
                        playNextFrames(output_line);
                    }
                } while (playing && isLooping());

                output_line.drain();
                
                for (;;) {
                    synchronized (this) {
                        checkStopTime();
                        if (playing || terminated) break;
                        // if not playing and not terminated, sleep again!
                    }
                    try {
                        debugf("Player thread sleeping for 10 seconds. playing=%b, looping=%b", playing, isLooping());
                        sleep(10000);
                    } catch (InterruptedException ex) {
                        debug("Player thread interrupted in sleep");
                    }
                }
            }
            
            debug("Player thread terminated");
        }
        
        public synchronized void stopPlaying() {
            playing = false;
            setLooping(false);
            // no need to interrupt in this case
        }
        
        public synchronized void startPlaying(boolean loop) {
            setLooping(loop);
            playing = true;
            interrupt();
        }
        
        /**
         * Halts playback and permanently stops this thread.
         */
        public synchronized void terminate() {
            stopPlaying();
            terminated = true;
            interrupt();
        }
        
        /**
         * Compares {@link #stopTime} to the current system time, and calls
         * {@link #stopPlaying()} if the current time is at or after stopTime.
         * In that case, stopTime is also reset to Long.MAX_VALUE.
         */
        private void checkStopTime() {
            if (System.currentTimeMillis() >= stopTime) {
                stopTime = Long.MAX_VALUE;
                stopPlaying();
            }
        }
        
        public synchronized void setStopTime(long time) {
            stopTime = time;
        }
    }
    
    public void startPlaying(boolean loop) throws LineUnavailableException {
        playerThread.startPlaying(loop);
    }

    /**
     * Plays the next few frames of the module, starting from the current playback
     * position. This will typically be less than a second of audio; to maintain
     * smooth playback, this method has to be called frequently (normally done by
     * the player thread).
     * <p>
     * This method uses and updates the values of the {@link #framesPlayed} and
     * {@link #remainingFrames} variables.
     * 
     * @param output_line The JavaSound line to play the samples on. It must be open
     * and running already.
     */
    private void playNextFrames(SourceDataLine output_line) {
        byte[] output_buffer = new byte[ 1024 * 4 ];
        int frames = 1024;
        if( frames > remainingFrames ) {
            frames = remainingFrames;
        }
        ibxm.get_audio( output_buffer, frames );
        output_line.write( output_buffer, 0, frames * 4 );
        remainingFrames -= frames;
        framesPlayed += frames;
        debug("Played frame " + framesPlayed + "/" + song_duration);
    }

    /**
     * Stops playback either immediately, or after jumping to a pre-configured
     * position in the mod and continuing playback for a pre-configured number
     * of milliseconds.
     * <p>
     * <b>Hints on using the endings system with mods:</b> Ideally, you could
     * set aside a whole pattern for each special ending to your mod (for
     * example, one for winning, one for dying, one for finding a special exit,
     * and so on). However, the mod playback library used by ModMusic only
     * allows jumping to a certain index in the playback sequence. This makes
     * any patterns in your mod that are not in the playback sequence
     * inaccessible. The recommended workaround for this problem is to set aside
     * sequence index 0 for all of your endings. On the first row in that
     * pattern, use effect <code>B01</code> to cause an immediate jump to
     * sequence index 1. This leaves the rest of the pattern for various
     * endings. Between each ending tune, use effect <code>F00</code>
     * (speed/tempo 0) to ensure playback halts before reaching the next ending
     * tune. Then simply use sequenceIndex 0 for all endings, and various
     * offsets and durations as appropriate. Since each ending is guarded by a
     * <code>F00</code>, you can afford to overestimate by a few seconds on
     * your durations.
     * 
     * @param ending
     *            The ending to play. Endings are configured using the
     *            {@link #addEnding(String, int, int, long)} method, which is
     *            normally done when first setting up all the
     *            SoundManagerEntries.
     */
    public void stopPlaying(String ending) {
        debugf("Stopping with ending %s", ending);
        SongPosition songPosition = endings.get(ending);
        if (ending != null && songPosition != null) {
            debugf("Resetting playback position: seq=%d offset=%d duration=%d",
                    songPosition.getSequenceIndex(), songPosition.getOffset(), songPosition.getDuration());
            ibxm.set_sequence_index(songPosition.getSequenceIndex(), songPosition.getOffset());
            output_line.flush();
            playerThread.setStopTime(System.currentTimeMillis() + songPosition.getDuration());
        } else {
            playerThread.stopPlaying();
        }
    }
    
    public boolean isLooping() {
        return looping;
    }
    
    public void setLooping(boolean looping) {
        this.looping = looping;
    }
    
    public static void main(String[] args) throws Exception {
        ResourceLoader rl = new SystemResourceLoader();
        
        ModMusic mm = new ModMusic(rl, "march_01", "builtin/ROBO-INF/music/grod_march_01.xm");

        SoundManager sm = new SoundManager(rl);
        sm.addEntry("x", EntryType.CLIP, "builtin/ROBO-INF/sounds/delete_gate.wav");
        
        mm.startPlaying(true);
        
        debug("Main thread sleeping...");
        Thread.sleep(2000);

        debug("Proving you can play a clip over music");
        sm.play("x");

        debug("Main thread sleeping...");
        Thread.sleep(6000);

        debug("Main thread sleeping...");
        Thread.sleep(2000);

        debug("Main thread requesting stop...");
//        mm.stopPlaying("win");
        mm.stopPlaying(null);

        mm.close();

        debug("Main thread exiting");
    }

    public void close() {
        output_line.close();
        playerThread.terminate();
    }
    
    public EntryType getType() {
        return EntryType.MOD;
    }

    /**
     * Creates a new SongPosition with the given parameters, and adds it to this
     * ModMusic's set of available endings. See {@link #stopPlaying(String)} for
     * details and hints on how to set up a mod to take advantage of this
     * system.
     * 
     * @param name
     *            The name this ending will be known by. If this ModMusic
     *            already has an ending of that name, the new one will replace
     *            it.
     * @param sequenceIndex
     *            The place in the playback sequence this ending jumps to. The
     *            first pattern in the playback sequence is numbered 0.
     * @param offset
     *            The number of lines into the pattern specified by
     *            sequenceIndex to jump to.
     * @param duration
     *            The number of milliseconds playback should continue after
     *            {@link #stopPlaying(String)} has been called for this ending.
     * @return The new SongPosition object created as a result of this call.
     */
    public SongPosition addEnding(String name, int sequenceIndex, int offset, long duration) {
        return endings.put(name, new SongPosition(name, sequenceIndex, offset, duration));
    }
    
    /**
     * Returns a read-only view of the current endings configured for this
     * ModMusic instance.
     */
    public Collection<SongPosition> getEndings() {
        return Collections.unmodifiableCollection(endings.values());
    }
}
