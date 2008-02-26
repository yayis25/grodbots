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
     * The pattern to play when requested to play the "winning" end to the song.
     */
    private int winTunePattern = 2;

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

        @Override
        public void run() {
            while (!terminated) {
                do {
                    framesPlayed = 0;
                    remainingFrames = song_duration;
                    ibxm.set_sequence_index(0, 0);
                    while (remainingFrames > 0) {
                        synchronized (this) {
                            if (!playing) break;
                        }
                        playNextFrames(output_line);
                    }
                } while (playing && isLooping());

                output_line.drain();
                
                for (;;) {
                    synchronized (this) {
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

    public void stopPlaying(String ending) {
        debugf("Stopping with ending %s", ending);
        // TODO implement endings
        playerThread.stopPlaying();
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
    }
    
    public EntryType getType() {
        return EntryType.MOD;
    }
}
