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
 * Created on Jun 26, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.sprite;

import java.awt.Dimension;
import java.awt.Image;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.bluecow.robot.resource.ResourceLoader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AnimatedSprite extends AbstractSprite {

    /**
     * Receives the contents of the RSF file (which is XML) as the SAX parser
     * parses it.
     */
    private class RsfSaxHandler extends DefaultHandler {
        @Override
        public void startElement(String uri, String localName,
                String qName, Attributes attributes) throws SAXException {
            try {
//              System.out.printf("URI: %s, localName: %s, qName: %s, attributes: %s\n", uri, localName, qName, attributes);
                if (qName.equals("rsf")) {
//                    System.out.println("RSF version "+attributes.getValue("version"));
                } else if (qName.equals("frame")) {
                    String imagePath = basePath + attributes.getValue("href");
                    ImageIcon icon = new ImageIcon(getResourceLoader().getResourceBytes(imagePath));
                    size.width = Math.max(size.width, icon.getIconWidth());
                    size.height = Math.max(size.height, icon.getIconHeight());
                    frames.put(attributes.getValue("id"), icon);
                } else if (qName.equals("sequence")) {
                    // nothing to do, really
                } else if (qName.equals("step")) {
                    int count = 1;
                    String countStr = attributes.getValue("count");
                    if (countStr != null) {
                        count = Integer.parseInt(countStr);
                    }
                    for (int i = 0; i < count; i++) {
                        sequence.add(frames.get(attributes.getValue("frame")));
                    }
                } else {
                    throw new SAXException("Unknown element \""+qName+"\" in RSF file");
                }
            } catch (IOException ex) {
                throw new SAXException(ex);
            }
        }
    }

    /**
     * Maps frame id's to their loaded images.
     */
    private Map<String, ImageIcon> frames = new HashMap<String, ImageIcon>();
    
    /**
     * The "base" resource path, to which all URI's in the RSF file are relative.
     */
    private String basePath;
    
    private Dimension size = new Dimension(0,0);
    private List<ImageIcon> sequence = new ArrayList<ImageIcon>();
    private int curSeqNum = 0;
    
    /**
     * Creates an animated sprite object configured by parsing an RSF file.
     * RSF files are a proprietary file format for animated sprites in this
     * game.  RSF stands for Robot Sprite Format.
     * 
     * @param resourceLoader The resource loader to get the RSF file and all its
     * dependant resources from.
     * @param rsfPath The resource path of the RSF file in the resource loader.  All
     * dependent resources referenced in the RSF file are relative to this location.
     * @param attribs A set of named attributes to give the sprite.  See the AbstractSprite
     * documentation for details.
     * @throws ParserConfigurationException If the SAX parser can't be created
     * @throws SAXException If there is a problem with the RSF file's syntax, or
     * one of the resources it references can't be located.
     * @throws FileNotFoundException If the RSF file does not
     * exist in the given resource loader's namespace.
     * @throws IOException If there are other types of I/O errors.
     */
    public AnimatedSprite(ResourceLoader resourceLoader,
                          String rsfPath,
                          Map<String, String> attribs)
    throws ParserConfigurationException, SAXException, IOException {
        super(resourceLoader, attribs);
        if (rsfPath.lastIndexOf('/') >= 0) {
            basePath = rsfPath.substring(0, rsfPath.lastIndexOf('/') + 1);
        } else {
            basePath = "/";
        }
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        // turn off validation parser.setProperty()
        parser.parse(resourceLoader.getResourceAsStream(rsfPath), new RsfSaxHandler());
    }
    
    @Override
    public void nextFrame() {
        curSeqNum++;
        if (curSeqNum >= sequence.size()) curSeqNum = 0;
    }

    @Override
    public Image getImage() {
        return sequence.get(curSeqNum).getImage();
    }
    
    public int getWidth() {
        return (int) (size.width * getScale());
    }

    public int getHeight() {
        return (int) (size.height * getScale());
    }
}
