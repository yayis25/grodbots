/*
 * Copyright (c) 2007, Jonathan Fuerth
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
/*
 * Created on Jun 27, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.LinkedHashMap;
import java.util.Map;

import net.bluecow.robot.resource.ResourceLoader;

public abstract class AbstractSprite implements Sprite {
    
    /**
     * Controls debugging features of this class, including whether or not
     * it prints debug information to the console.
     */
    private static final boolean debugOn = false;
    
    /**
     * Prints the given string plus a newline to stdout, if debugOn==true. 
     */
    private static void debug(String msg) {
        if (debugOn) System.out.println(msg);
    }
    
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

    /**
     * The transform currently applied to this sprite when it's painting.
     * Defaults to the identity transform.
     */
    private AffineTransform transform = new AffineTransform();
    
    /**
     * Optional bounding box that defines how this sprite collides with its
     * surroundings and other sprites. If not specified, this sprite will not
     * collide with anything.
     * <p>
     * The rectangle's coordinates are relative to the top left corner of the
     * sprite (the image's (0,0) point).
     */
    private Rectangle collisionBox;
    
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
     * Paints this sprite at the given co-ordinates at the current scale setting
     * modified by the current transform. The image to paint is obtained by a
     * call to getImage(), the scale comes from a call to getScale(), and the
     * transform is obtained from getTransform(). Each of getImage(),
     * getScale(), and getTransform are called exactly once. The Graphics2D
     * argument's transform is modified in the course of this method's
     * invocation, but it is guaranteed to be restored to its initial setting
     * before this method returns.
     */
    public void paint(Graphics2D g2, int x, int y) {
        AffineTransform backupXform = g2.getTransform();
        try {
            double scale = getScale();
            g2.translate(x, y);
            AffineTransform drawingTransform = AffineTransform.getScaleInstance(scale, scale);
            drawingTransform.concatenate(transform);
            g2.drawImage(getImage(), drawingTransform, null);
            if (debugOn && getCollisionBox() != null) {
                Color backupColor = g2.getColor();
                g2.setColor(Color.RED);
                Rectangle cb = getCollisionBox();
                g2.drawRect(cb.x, cb.y, cb.width, cb.height);
                g2.setColor(backupColor);
            }
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
    
    
    public Rectangle getCollisionBox() {
        return collisionBox;
    }
    
    public void setCollisionBox(Rectangle box) {
        this.collisionBox = box;
    }
    
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

    public AffineTransform getTransform() {
        return transform;
    }

    public void setTransform(AffineTransform transform) {
        this.transform = transform;
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
