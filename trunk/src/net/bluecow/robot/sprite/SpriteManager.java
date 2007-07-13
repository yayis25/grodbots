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
 * Created on Apr 20, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.bluecow.robot.resource.ResourceLoader;

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
    public static Sprite load(ResourceLoader resourceLoader, Map<String, String> attribs) throws SpriteLoadException {
        String resourcePath = attribs.get("href");
        
        try {
            
            Sprite sprite;
            if (resourcePath.endsWith(".rsf")) {
                sprite = new AnimatedSprite(resourceLoader, resourcePath, attribs);
            } else {
                sprite = new IconSprite(resourceLoader, resourcePath, attribs);
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
    public static Sprite load(ResourceLoader resourceLoader, String href) throws SpriteLoadException {
        Map<String, String> attribs = new HashMap<String, String>();
        attribs.put("href", href);
        return load(resourceLoader, attribs);
    }
    
}
