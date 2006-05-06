/*
 * Created on Apr 20, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.io.FileNotFoundException;
import java.net.URL;

public class SpriteManager {
    
    /**
     * This class is not designed for instantiation.
     */
    private SpriteManager() {
        // not used
    }
    
    public static Sprite load(String resourcePath) throws FileNotFoundException {
        URL resourceURL = ClassLoader.getSystemResource(resourcePath);
        if (resourceURL == null) {
            throw new FileNotFoundException("Image '"+resourcePath+"' not found.");
        }
        return new IconSprite(resourceURL);
    }
    
}
