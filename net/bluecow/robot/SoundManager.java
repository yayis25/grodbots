/*
 * Created on Mar 16, 2006
 *
 * This code belongs to Jonathan Fuerth.
 */
package net.bluecow.robot;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * The SoundManager handles loading, playing, looping, and stopping
 * audio in the robot game.
 *
 * <p>
 * Since this API is for a game, it makes sure audio failures are non-
 * critical.  For example, if a clip fails to load (and addClip throws an
 * exception), it is still safe to attempt to play, loop, and stop the
 * clip which has not been loaded.  These operations simply have no effect
 * except to log the failure to the system console.
 * 
 * @author fuerth
 * @version $Id$
 */
public class SoundManager {
    
    /**
     * Maps names (which are supplied by the client code) to audio clips.
     */
    Map<String, Clip> clips = new HashMap<String, Clip>();
    
    /**
     * Adds a clip to the library, so that you can later use it with
     * {@link #play(String)}, {@link #loop(String)}, and {@link #stop(String)}.
     * 
     * @param name The name of the clip you want to add
     * @param data The URL where the clip can be loaded from.  WAV and AIFF
     * formats work; others require additional JavaSound service providers.
     * 
     * @throws RuntimeException if the sound clip cannot be loaded for any reason.
     */
    public void addClip(String name, URL data) {
        try {
            Line.Info linfo = new Line.Info(Clip.class);
            Line line;
            line = AudioSystem.getLine(linfo);
            Clip clip = (Clip) line;
            //clip.addLineListener(this);
            AudioInputStream ais = AudioSystem.getAudioInputStream(data);
            clip.open(ais);
            clips.put(name, clip);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't load clip: "+e.getMessage());
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't load clip: "+e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't load clip: "+e.getMessage());
        }
    }
    
    /**
     * Plays a clip, or if it is already playing, restarts it from the beginning.
     */
    public void play(String name) {
        Clip c = clips.get(name);
        if (c == null) {
            System.out.println("Can't play clip '"+name+"' because it doesn't exist");
        }
        c.setFramePosition(0);
        c.start();
    }

    /**
     * Stops a clip.  If the clip was not playing, calling this method has no effect.
     */
    public void stop(String name) {
        Clip c = clips.get(name);
        if (c == null) {
            System.out.println("Can't stop clip '"+name+"' because it doesn't exist");
        }
        c.stop();
    }

    /**
     * Starts looping a clip from the beginning.  The clip will continue looping
     * until you stop it with {@link #stop(String)}.
     */
    public void loop(String name) {
        Clip c = clips.get(name);
        if (c == null) {
            System.out.println("Can't loop clip '"+name+"' because it doesn't exist");
        }
        c.setFramePosition(0);
        c.loop(Clip.LOOP_CONTINUOUSLY);
    }

}
