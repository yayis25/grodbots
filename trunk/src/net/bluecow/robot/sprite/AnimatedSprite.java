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
import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.bluecow.robot.FileFormatException;
import net.bluecow.robot.resource.ResourceLoader;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AnimatedSprite extends AbstractSprite {

    /**
     * Receives the contents of the RSF file (which is XML) as the SAX parser
     * parses it.
     */
    private class RsfSaxHandler extends DefaultHandler {
        
        /**
         * Object from the parser that tracks parse position.
         */
        private Locator loc;

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
                } else if (qName.equals("collision-box")) {
                    Integer x = null;
                    Integer y = null;
                    Integer width = null;
                    Integer height = null;
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String aname = attributes.getQName(i);
                        String aval = attributes.getValue(i);
                        
                        if (aname.equals("x")) {
                            x = Integer.parseInt(aval);
                        } else if (aname.equals("y")) {
                            y = Integer.parseInt(aval);
                        } else if (aname.equals("width")) {
                            width = Integer.parseInt(aval);
                        } else if (aname.equals("height")) {
                            height = Integer.parseInt(aval);
                        }                        
                    }
                    
                    checkMandatory(qName, "x", x);
                    checkMandatory(qName, "y", y);
                    checkMandatory(qName, "width", width);
                    checkMandatory(qName, "height", height);
                    
                    Rectangle collisionBox = new Rectangle(x, y, width, height);
                    setCollisionBox(collisionBox);
                    
                } else if (qName.equals("sequence")) {
                    // the ID might be null, which is fine.
                    curSeqName = attributes.getValue("id");
                    sequences.put(curSeqName, new ArrayList<ImageIcon>());
                } else if (qName.equals("step")) {
                    int count = 1;
                    String countStr = attributes.getValue("count");
                    if (countStr != null) {
                        count = Integer.parseInt(countStr);
                    }
                    for (int i = 0; i < count; i++) {
                        sequences.get(curSeqName).add(frames.get(attributes.getValue("frame")));
                    }
                } else {
                    throw new SAXException("Unknown element \""+qName+"\" in RSF file");
                }
            } catch (IOException ex) {
                throw new SAXException(ex);
            }
        }
        
        @Override
        public void setDocumentLocator(Locator locator) {
            this.loc = locator;
        }
        
        /**
         * Helper routine that throws a FileFormatException when a mandatory attribute
         * is null.
         * 
         * @param elemName The qName of the element that the mandatory attribute should
         * be found in.
         * @param attName The name of the mandatory attribute
         * @param attVal The value of the mandatory attribute
         * @throws FileFormatException if attVal is null.  The message will say the name of
         * the element and the attribute, and say it should have been there.
         */
        private void checkMandatory(String elemName, String attName, Object attVal) throws FileFormatException {
            if (attVal == null) {
                throw new FileFormatException("Missing mandatory attribute \""+attName+"\" of element <"+elemName+">",
                        loc.getLineNumber(), "(original line from file not available)", loc.getColumnNumber());
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
    
    /**
     * All sequences that were defined in the RSF file.
     */
    private Map<String, List<ImageIcon>> sequences = new LinkedHashMap<String, List<ImageIcon>>();
    
    /**
     * The name of the current sequence being played back. The default sequence after
     * parsing the RSF file is the last one that was defined in that file.
     */
    private String curSeqName = null;
    
    /**
     * The current frame number within the current sequence.
     */
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
        if (curSeqNum >= sequences.get(curSeqName).size()) curSeqNum = 0;
    }

    @Override
    public Image getImage() {
        return sequences.get(curSeqName).get(curSeqNum).getImage();
    }
    
    public int getWidth() {
        return (int) (size.width * getScale());
    }

    public int getHeight() {
        return (int) (size.height * getScale());
    }
    
    /**
     * Switches the animation progression to the given sequence, and if the
     * given sequence is not the current sequence, resets the frame position to
     * the first frame of the new sequence. In other words, this method has
     * no effect if the given sequence is already the current sequence.
     * 
     * @param seqName
     *            The sequence to switch to. If this sequence is not defined for
     *            this sprite, the animation state will not be changed.
     */
    public void setCurrentSequence(String seqName) {
        if (seqName == curSeqName || (seqName != null && seqName.equals(curSeqName))) {
            return;
        }
        if (sequences.containsKey(seqName)) {
            this.curSeqName = seqName;
            curSeqNum = 0;
        } else {
            System.out.println(
                    "Warning: this animated sprite has no sequence called " +
                    seqName + ". Staying with sequence " + curSeqName + ".");
        }
    }
    
    public String getCurrentSequence() {
        return curSeqName;
    }
    
    /**
     * Enhances the basic clone functionality to make sure the new copy's animation sequences
     * are truly independant of the ones in this object.
     */
    @Override
    public Sprite clone() {
        AnimatedSprite copy = (AnimatedSprite) super.clone();
        
        for (Map.Entry<String, List<ImageIcon>> entry : copy.sequences.entrySet()) {
            entry.setValue(new ArrayList<ImageIcon>(entry.getValue()));
        }
        
        return copy;
    }
}
