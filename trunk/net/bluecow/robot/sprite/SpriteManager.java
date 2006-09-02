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
     * Loads the sprite described by the attribs map.
     * 
     * @param attribs A dictionary of attributes that define the sprite.
     *                Currently-supported values are:
     * <ul>
     *  <li><b>href</b>: the <tt>ClassLoader.getSystemResource()</tt> path to the
     *                   file defining the sprite.  Currently, gif, png, and rsf formats
     *                   are supported.
     *  <li><b>scale</b>: The amount to scale the raw image by before painting it.  For example,
     *                   "1.0" means natural size; "0.5" means half size; "2.0" means double. 
     * </ul>
     * @throws FileNotFoundException If the system resource at the given path does not exist.
     * @throws IllegalArgumentException If any attributes are not in the name=value form.
     */
    public static Sprite load(Map<String, String> attribs) throws SpriteLoadException {
        String resourcePath = attribs.get("href");
        
        try {
            URL resourceURL = ClassLoader.getSystemResource(resourcePath);
            if (resourceURL == null) {
                throw new FileNotFoundException("Sprite resource '"+resourcePath+"' not found.");
            }
            
            Sprite sprite;
            if (resourcePath.endsWith(".rsf")) {
                sprite = new AnimatedSprite(resourceURL, attribs);
            } else {
                sprite = new IconSprite(resourceURL, attribs);
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

    /**
     * Equivalent to calling {@link #load(Map)} with the <tt>attribs</tt> map
     * containing one entry mapping "href" to the value of the <tt>href</tt>
     * argument.
     * 
     * @param href the location of the sprite's graphic file
     * @return The newly-loaded Sprite instance
     * @throws SpriteLoadException if there is a problem creating the sprite
     */
    public static Sprite load(String href) throws SpriteLoadException {
        Map<String, String> attribs = new HashMap<String, String>();
        attribs.put("href", href);
        return load(attribs);
    }
    
}
