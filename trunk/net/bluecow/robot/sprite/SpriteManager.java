/*
 * Created on Apr 20, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class SpriteManager {
    
    /**
     * This class is not designed for instantiation.
     */
    private SpriteManager() {
        // not used
    }
    
    /**
     * Loads the sprite described by the spriteDesc argument.
     * 
     * @param spriteDesc The resource path to the sprite, optionally followed by
     * one or more name=value attributes, each name=value pair separated by a comma.
     * For example, "/ROBO-INF/images/robot.png,scale=0.4,cow=moo".
     * @throws FileNotFoundException If the system resource at the given path does not exist.
     * @throws IllegalArgumentException If any attributes are not in the name=value form.
     */
    public static Sprite load(String spriteDesc) throws SpriteLoadException {
        Map<String, String> attribs = new HashMap<String, String>();
        String[] args = spriteDesc.split(",");
        String resourcePath = args[0];
        for (int i = 1; i < args.length; i++) {
            if (args[i].indexOf('=') == -1) {
                throw new IllegalArgumentException(
                        "Attributes after the pathname must be in name=value pairs. " +
                        "The attribute '"+args[i]+"' does not meet this criterion.");
            }
            String[] nameVal = args[i].split("=");
            attribs.put(nameVal[0], nameVal[1]);
        }
        
        try {
            URL resourceURL = ClassLoader.getSystemResource(resourcePath);
            if (resourceURL == null) {
                throw new FileNotFoundException("Sprite resource '"+resourcePath+"' not found.");
            }
            
            Sprite sprite;
            if (resourcePath.endsWith(".rsf")) {
                sprite = new AnimatedSprite(resourceURL);
            } else {
                sprite = new IconSprite(resourceURL);
            }
            
            if (attribs.get("scale") != null) {
                double scale = Double.parseDouble(attribs.get("scale"));
                sprite.setScale(scale);
            }
            return sprite;
        } catch (FileNotFoundException e) {
            throw new SpriteLoadException(e);
        } catch (ParserConfigurationException e) {
            throw new SpriteLoadException(e);
        } catch (SAXException e) {
            throw new SpriteLoadException(e);
        } catch (IOException e) {
            throw new SpriteLoadException(e);
        }
    }
    
}
