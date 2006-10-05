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
                    System.out.println("RSF version "+attributes.getValue("version"));
                } else if (qName.equals("frame")) {
                    String imagePath = basePath + attributes.getValue("href");
                    ImageIcon icon = new ImageIcon(resourceLoader.getResourceBytes(imagePath));
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

    private ResourceLoader resourceLoader;
    
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
        super(attribs);
        this.resourceLoader = resourceLoader;
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
