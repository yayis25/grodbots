/*
 * Created on Jun 27, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.util.LinkedHashMap;
import java.util.Map;

import net.bluecow.robot.resource.ResourceLoader;

public abstract class AbstractSprite implements Sprite {
    
    /**
     * The resource loader that this sprite's resources came from.
     * It is required for supporting the generic clone() method.
     */
    private final ResourceLoader resourceLoader;
    
    /**
     * The set of attributes this sprite was created with.  The reason
     * the attributes are carried in this way is that it needs to be
     * possible to save an arbitrary sprite implementation in LevelStore.save().
     * Without this map, save() would require special knowledge of every sprite
     * implementation.
     */
    private Map<String, String> attributes;

    protected AbstractSprite(ResourceLoader resourceLoader, Map<String, String> attributes) {
        this.resourceLoader = resourceLoader;
        this.attributes = new LinkedHashMap<String, String>(attributes);
    }

    /**
     * Generic clone implementation.  Simply creates a new sprite by calling
     * SpriteManager.load() with this sprite's resource loader and attribute
     * map.
     */
    public Sprite clone() {
        try {
            return SpriteManager.load(resourceLoader, getAttributes());
        } catch (SpriteLoadException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * See {@link #attributes}.
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * See {@link #attributes}.
     */
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    /**
     * Paints this sprite at the given co-ordinates at the current scale
     * setting.  The image to paint is obtained by a call to getImage(),
     * and the scale comes from a call to getScale().  Each of getImage()
     * and getScale() are called exactly once.  The Graphics2D argument's
     * transform is modified in the course of this method's invocation, but
     * it is guaranteed to be restored to its initial setting before this
     * method returns.
     */
    public void paint(Graphics2D g2, int x, int y) {
        AffineTransform backupXform = g2.getTransform();
        try {
            double scale = getScale();
            g2.translate(x, y);
            g2.drawImage(getImage(), AffineTransform.getScaleInstance(scale, scale), null);
        } finally {
            g2.setTransform(backupXform);
        }
    }

    /**
     * Returns the image that this sprite will draw when paint() is called.
     * Subclasses have to implement this method in order to take advantage of
     * the AbstractSprite's generic implementation of paint().
     */
    public abstract Image getImage();
    
    public double getScale() {
        final String stringScale = getAttributes().get("scale");
        if (stringScale == null) {
            return 1.0;
        } else {
            return Double.parseDouble(stringScale);
        }
    }

    public void setScale(double scale) {
        getAttributes().put("scale", String.valueOf(scale));
    }

    /**
     * This implementation does nothing.  If your subclass supports multiple frames
     * of animation, override this method.
     */
    public void nextFrame() {
        // nothing to do
    }

    /**
     * See {@link #resourceLoader}.
     */
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }
}
