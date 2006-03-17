/*
 * Created on Mar 16, 2006
 *
 * This code belongs to SQL Power Group Inc.
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
 * @author fuerth
 * @version $Id$
 */
public class SoundManager {
    
    /**
     * Maps names (which are supplied by the client code) to audio clips.
     */
    Map<String, Clip> clips = new HashMap<String, Clip>();
    
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
    
    public void play(String name) {
        Clip c = clips.get(name);
        if (c == null) {
            System.out.println("Can't play clip '"+name+"' because it doesn't exist");
        }
        c.setFramePosition(0);
        c.start();
    }
    
    public void stop(String name) {
        Clip c = clips.get(name);
        if (c == null) {
            System.out.println("Can't stop clip '"+name+"' because it doesn't exist");
        }
        c.stop();
    }

    public void loop(String name) {
        Clip c = clips.get(name);
        if (c == null) {
            System.out.println("Can't loop clip '"+name+"' because it doesn't exist");
        }
        c.setFramePosition(0);
        c.loop(Clip.LOOP_CONTINUOUSLY);
    }

}
