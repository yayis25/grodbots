/*
 * Created on Mar 19, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.bluecow.robot.GameConfig.GateConfig;
import net.bluecow.robot.GameConfig.SensorConfig;
import net.bluecow.robot.GameConfig.SquareConfig;
import net.bluecow.robot.gate.Gate;
import net.bluecow.robot.resource.ResourceLoader;
import net.bluecow.robot.sprite.Sprite;
import net.bluecow.robot.sprite.SpriteLoadException;
import net.bluecow.robot.sprite.SpriteManager;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The LevelStore is responsible for loading in level descriptions and
 * saving out level descriptions.
 *
 * @author fuerth
 * @version $Id$
 */
public class LevelStore {
    
    public static final String DEFAULT_MAP_RESOURCE_PATH = "ROBO-INF/default.map";

    public static final String WALL_FLAG = "WALL";

    private static boolean debugging = false;
    
    /**
     * Writes the given Game configuration in the Rocky 4.0 XML format to the
     * given file.  The actual writing process is done into a temporary file in
     * the same directory as destFile, and if successful, the temporary file
     * is renamed to the pathname given for destFile.  Therefore, if an exception
     * gets thrown, there may be a partially complete new level file in the
     * same directory as destFile.  In that case, destFile will not have been
     * affected.
     * 
     * @param destFile
     * @param gc
     * @param encoding The desired encoding of the output file.  This string will
     * also be written to the XML declaration.
     * @throws IOException
     */
    public static void save(File destFile, GameConfig gc, String encoding) throws IOException {
        File destDir = destFile.getParentFile();
        if (!destDir.exists()) {
            throw new IOException("Destination directory \""+destDir+"\" does not exist");
        }
        File tempFile = new File(destDir, "map_tmp_"+System.currentTimeMillis());
        Writer out = null;
        try {
            out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(tempFile), encoding));
            save(out, gc, encoding);
            if (destFile.exists()) {
                destFile.renameTo(new File(destDir, "map_backup_"+System.currentTimeMillis()));
            }
            tempFile.renameTo(destFile);
        } catch (Exception ex) {
            throw new IOException(
                    "Error writing game description XML file.  You can examine the " +
                    "partially-saved game description in " + tempFile.getAbsolutePath() + " " +
                    "The original file, if any, has not been affected.");
        } finally {
            if (out != null) {
                out.flush();
                out.close();
                out = null;
            }
        }
    }

    /**
     * Writes the given game configuration as described in
     * {@link #save(File, GameConfig, String)}, but makes no attempts at safety
     * (the game config simply gets dumped to the given writer).
     * 
     * @param out The writer to write the game config to.
     * @param gc The game config to save.
     * @param encoding The character encoding to output in the XML header. Should
     * match the character encoding of the given writer.  It is important
     * to get this right, because XML parsers tend to blindly trust whatever the
     * XML delcaration says.
     * @throws IOException
     */
    private static void save(Writer out, GameConfig gc, String encoding) throws IOException {
        out.write("<?xml version=\"1.0\" encoding=\""+encoding+"\"?>\n");
        out.write("<rocky version=\"4.0\">");

        out.write("\n");

        for (SensorConfig sensor : gc.getSensorTypes()) {
            out.write("  <sensor type=\""+sensor.getId()+"\" />\n");
        }
        
        out.write("\n");
        
        Map<Class<Gate>, String> gateNameMap = new HashMap<Class<Gate>, String>();
        for (GateConfig gate : gc.getGateTypes()) {
            out.write("  <gate type=\""+gate.getName()+"\" " +
                          "mnemonic=\""+gate.getAccelerator().getKeyChar()+"\" " +
                             "class=\""+gate.getGateClass().getName()+"\" />\n");
            gateNameMap.put(gate.getGateClass(), gate.getName());
        }
        
        out.write("\n");

        for (SquareConfig square : gc.getSquareTypes()) {
            out.write("  <square type=\""+square.getName()+"\" " +
                             "mapchar=\""+square.getMapChar()+"\" " +
                             "graphic=\""+square.getSprite().getAttributes().get(Sprite.KEY_HREF)+"\">\n");
            if (!square.isOccupiable()) {
                out.write("    <attribute type=\"WALL\" />\n");
            }
            for (SensorConfig sensor : square.getSensorTypes()) {
                out.write("    <activate-sensor type=\""+sensor.getId()+"\" />\n");
            }
            out.write("  </square>\n");
        }

        out.write("\n");

        for (LevelConfig level : gc.getLevels()) {
            out.write("  <level name=\""+level.getName()+"\" " +
                             "size-x=\""+level.getWidth()+"\" " +
                             "size-y=\""+level.getHeight()+"\">\n");

            if (level.getDescription() != null && level.getDescription().trim().length() > 0) {
                out.write("  <description><![CDATA[\n");
                out.write(level.getDescription());
                out.write("\n  ]]></description>\n");
            }
            
            for (Robot r : level.getRobots()) {
                out.write("    <grod id=\""+r.getId()+"\" " +
                             "step-size=\""+r.getStepSize()+"\" " +
                               "start-x=\""+r.getPosition().x+"\" " +
                               "start-y=\""+r.getPosition().y+"\" " +
                        "evals-per-step=\""+r.getEvalsPerStep()+"\" " +
                       makeLabelAttributes(r) +
                       ">\n");
                out.write("      "+makeSpriteTag(r.getSprite())+"\n");
                
                for (Map.Entry<Class<? extends Gate>, Integer> ent : r.getCircuit().getGateAllowances().entrySet()) {
                    String gateType = gateNameMap.get(ent.getKey());
                    int allowance = ent.getValue();
                    out.write("      <gate-allowance type=\""+gateType+"\" value=\""+allowance+"\" />\n");
                }
                
                out.write("    </grod>\n");
            }

            out.write("\n");

            for (LevelConfig.Switch s : level.getSwitches()) {
                out.write("    <switch id=\""+s.getId()+"\" " +
                                   "loc-x=\""+s.getX()+"\" " +
                                   "loc-y=\""+s.getY()+"\" " +
                                "on-enter=\""+s.getOnEnter()+"\" " +
                                 "enabled=\""+s.isEnabled()+"\" " +
                                  ""+makeLabelAttributes(s)+">\n");
                out.write("      "+makeSpriteTag(s.getSprite())+"\n");
                out.write("    </switch>\n");
            }

            out.write("\n");

            out.write("    <map>\n");
            for (int x = 0; x < level.getMap()[0].length; x++) {
                for (int y = 0; y < level.getMap().length; y++) {
                    out.write(level.getMap()[y][x].getMapChar());
                }
                out.write("\n");
            }
            out.write("    </map>\n");
            
            out.write("  </level>\n");
        }
        out.write("</rocky>\n");
        out.flush();
    }
    
    private static String makeLabelAttributes(Labelable labelable) {
        if (labelable.getLabel() == null) {
            return "";
        } else {
            return        "label=\""+labelable.getLabel()+"\" " +
                "label-direction=\""+labelable.getLabelDirection().getCode()+"\" " +
                  "label-enabled=\""+labelable.isLabelEnabled()+"\"";
        }
    }

    private static String makeSpriteTag(Sprite sprite) throws IOException {
        StringBuffer tag = new StringBuffer();
        tag.append("<graphic");
        for (Map.Entry<String, String> ent : sprite.getAttributes().entrySet()) {
            tag.append(" ").append(ent.getKey()).append("=\"").append(ent.getValue()).append("\"");
        }
        tag.append("/>");
        return tag.toString();
    }

    public static GameConfig loadLevels(ResourceLoader resourceLoader) throws IOException {
        return loadLevels(resourceLoader, DEFAULT_MAP_RESOURCE_PATH);
    }
    
    /**
     * Reads in a list of 0 or more levels from the given input stream.  The file
     * format is documented only by the code that makes up this implementation.
     * 
     * <p>Use the source, Luke!
     * 
     * @param inStream The stream to read the level descriptions from.  This stream will
     * not be closed by this method, but it may be read to its end-of-file.
     * @return A List of playfield models, one per level described in the given stream.
     * The return value is never null.
     * @throws FileFormatException If the input data does not conform to the expectations of
     * this method.
     * @throws IOException If there is a general I/O problem reading the file.
     */
    public static GameConfig loadLevels(ResourceLoader resourceLoader, String mapResourcePath) throws IOException {
        InputStream inStream = new BufferedInputStream(resourceLoader.getResourceAsStream(mapResourcePath));
        LevelSaxHandler handler = new LevelSaxHandler(resourceLoader);
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            // turn off validation parser.setProperty()
            parser.parse(inStream, handler);
            if (!handler.getWarnings().isEmpty()) {
                System.out.println("Warnings encountered during load:");
                for (FileFormatException ffe : handler.getWarnings()) {
                    System.out.println(ffe.getMessage()+" (line "+ffe.getLineNum()+" col "+ffe.getBadCharPos()+")");
                }
            }
        } catch (SAXException ex) {
            if (ex.getException() instanceof IOException) {
                throw (IOException) ex.getException();
            } else if (ex.getException() == null) {
                throw new FileFormatException(ex.getMessage(), handler.loc.getLineNumber(), "Line not available", handler.loc.getColumnNumber());
            } else {
                IOException ioe = new IOException("Unexpected exception while parsing levels");
                ioe.initCause(ex);
                throw ioe;
            }
        } catch (ParserConfigurationException ex) {
            IOException ioe = new IOException("Couldn't create XML level parser");
            ioe.initCause(ex);
            throw ioe;
        }
        return handler.getGameConfig();
    }
    
    private static class LevelSaxHandler extends DefaultHandler {

        /**
         * The GameConfig object that this SAX handler is populating based
         * on the SAX events it recieves.
         */
        private GameConfig config;
        
        /**
         * The locator that tells us where the current element is in the input
         * file.  Useful for error reporting.
         */
        private Locator loc;

        /**
         * A list of warnings encountered during processing.
         */
        private List<FileFormatException> warnings;
        
        /**
         * Caches flags of square types while processing a &lt;square&gt; element.
         * Should be null at all other times.
         */
        List<String> squareAttributes;

        /**
         * Caches sensor types for square types while processing a &lt;square&gt; element.
         * Should be null at all other times.
         */
        List<String> squareSensors;

        /**
         * The type of the current square we're configuring.
         */
        private String squareName;

        /**
         * The map character of the current square we're configuring.
         */
        private Character squareChar;

        /**
         * The graphics filename of the current square we're configuring.
         */
        private String squareGraphicsFileName;

        /**
         * The level config we're currently configuring via SAX events.
         */
        private LevelConfig level;
        
        /**
         * The robot we're currently configuring via SAX events.
         */
        private Robot robot;

        /**
         * A sprite that has been loaded.  It's up to the endElement code to pick this
         * up and add it to the appropriate object (robot, tile, switch) as necessary.
         * If there was no nested sprite element, endElement should see this value as null.
         */
        private Sprite sprite;
        
        private LevelConfig.Switch newSwitch;
        
        /**
         * Character data within an element accumulates in this buffer.  It gets
         * reinitialised at every startElement, so if there are sub elemements
         * intermixed with character data, the endElement handlers will see all the
         * character data since the last start tag.  If this approach becomes problematic,
         * it will get changed.
         */
        private StringBuffer charData;
        
        public LevelSaxHandler(ResourceLoader resourceLoader) {
            this.config = new GameConfig(resourceLoader);
            this.warnings = new ArrayList<FileFormatException>();
        }

        public List<FileFormatException> getWarnings() {
            return warnings;
        }

        public GameConfig getGameConfig() {
            return config;
        }
        
        @Override
        public void setDocumentLocator(Locator locator) {
            this.loc = locator;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            
            // FIXME: need a way to get the text of a particular line from the input stream
            final String line = "(Original line from file is not available)";
            
            charData = new StringBuffer();

            try {
                if (qName.equals("rocky")) {
                    
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String aname = attributes.getQName(i);
                        String aval = attributes.getValue(i);
                        
                        if (aname.equals("version")) {
                            Pattern versionPattern = Pattern.compile("^([0-9]+)\\.([0-9]+)$");
                            Matcher magicMatcher = versionPattern.matcher(aval);
                            if (aval == null || !magicMatcher.matches()) {
                                throw new FileFormatException(
                                        "Version number is missing in the rocky start tag",
                                        loc.getLineNumber(), line, 0);
                            }
                            int major = Integer.parseInt(magicMatcher.group(1));
                            int minor = Integer.parseInt(magicMatcher.group(2));
                            debug("Found map file version "+major+"."+minor);
                        } else {
                            handleUnknownAttribute(qName, line, aname, aval);
                        }
                    }
                    
                } else if (qName.equals("sensor")) {
                    // sensor types (square attributes)
                    
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String aname = attributes.getQName(i);
                        String aval = attributes.getValue(i);
                        
                        if (aname.equals("type")) {
                            config.addSensorType(new GameConfig.SensorConfig(aval));
                        } else {
                            handleUnknownAttribute(qName, line, aname, aval);
                        }
                    }
                    
                    
                } else if (qName.equals("gate")) {
                    // gate types
                    
                    String type = null;
                    Character mnemonic = null;
                    String gateClass = null;
                    
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String aname = attributes.getQName(i);
                        String aval = attributes.getValue(i);
                        
                        if (aname.equals("type")) {
                            type = aval;
                        } else if (aname.equals("mnemonic")) {
                            if (aval.length() == 1) {
                                mnemonic = aval.charAt(0);
                            } else {
                                throw new FileFormatException("The mnemonic for a gate type has to be a single character.",
                                        loc.getLineNumber(), line, loc.getColumnNumber());
                            }
                        } else if (aname.equals("class")) {
                            gateClass = aval;
                        } else {
                            handleUnknownAttribute(qName, line, aname, aval);
                        }
                    }
                    
                    checkMandatory(qName, "type", type);
                    checkMandatory(qName, "mnemonic", mnemonic);
                    checkMandatory(qName, "class", gateClass);
                    
                    try {
                        config.addGate(type, mnemonic, gateClass);
                    } catch (ClassNotFoundException e) {
                        throw new FileFormatException("Could not find gate class '"+gateClass+"'", loc.getLineNumber(), line, loc.getColumnNumber());
                    }
                    
                } else if (qName.equals("square")) {
                    // square types
                    
                    squareName = null;
                    squareAttributes = new ArrayList<String>();
                    squareChar = null;
                    squareGraphicsFileName = null;
                    squareSensors = new ArrayList<String>();
                    
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String aname = attributes.getQName(i);
                        String aval = attributes.getValue(i);
                        
                        if (aname.equals("type")) {
                            squareName = aval;
                        } else if (aname.equals("mapchar")) {
                            if (aval.length() != 1) {
                                throw new FileFormatException("mapchar attribute must be exactly one character in element <square>",
                                        loc.getLineNumber(), line, loc.getColumnNumber());
                            }
                            squareChar = aval.charAt(0);
                        } else if (aname.equals("graphic")) {
                            squareGraphicsFileName = aval;
                        } else {
                            handleUnknownAttribute(qName, line, aname, aval);
                        }
                    }
                    
                    checkMandatory(qName, "type", squareName);
                    checkMandatory(qName, "mapchar", squareChar);
                    checkMandatory(qName, "graphic", squareGraphicsFileName);
                    
                    // square gets added to config in endElement handler
                    
                } else if (qName.equals("attribute")) {  // XXX: check if nested in square elem
                    
                    String type = null;
                    
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String aname = attributes.getQName(i);
                        String aval = attributes.getValue(i);
                        
                        if (aname.equals("type")) {
                            type = aval;
                        } else {
                            handleUnknownAttribute(qName, line, aname, aval);
                        }
                    }
                    
                    checkMandatory(qName, "type", type);
                    
                    squareAttributes.add(type);
                    
                } else if (qName.equals("activate-sensor")) {  // XXX: check if nested in square elem
                    
                    String type = null;
                    
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String aname = attributes.getQName(i);
                        String aval = attributes.getValue(i);
                        
                        if (aname.equals("type")) {
                            type = aval;
                        } else {
                            handleUnknownAttribute(qName, line, aname, aval);
                        }
                    }
                    
                    checkMandatory(qName, "type", type);
                    
                    if (config.getSensor(type) == null) {
                        throw new FileFormatException(
                                "Found reference to undeclared sensor type '"+type+"'",
                                loc.getLineNumber(), line, loc.getColumnNumber());
                    }
                    
                    squareSensors.add(type);
                    
                } else if (qName.equals("level")) {
                    level = new LevelConfig();
                    String name = null;
                    Integer xSize = null;
                    Integer ySize = null;
                    
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String aname = attributes.getQName(i);
                        String aval = attributes.getValue(i);
                        
                        if (aname.equals("name")) {
                            name = aval;
                        } else if (aname.equals("description")) {
                                name = aval;
                        } else if (aname.equals("size-x")) {
                            try {
                                xSize = Integer.parseInt(aval);
                            } catch (NumberFormatException ex) {
                                throw new FileFormatException("Couldn't parse X size of level '"+level.getName()+"'", loc.getLineNumber(), line, loc.getColumnNumber());
                            }
                        } else if (aname.equals("size-y")) {
                            try {
                                ySize = Integer.parseInt(aval);
                            } catch (NumberFormatException ex) {
                                throw new FileFormatException("Couldn't parse Y size of level '"+level.getName()+"'", loc.getLineNumber(), line, loc.getColumnNumber());
                            }
                        } else {
                            handleUnknownAttribute(qName, line, aname, aval);
                        }
                    }
                    
                    checkMandatory(qName, "name", name);
                    checkMandatory(qName, "size-x", xSize);
                    checkMandatory(qName, "size-y", ySize);
                    
                    level.setName(name);
                    level.setSize(xSize, ySize);
                    
                    // description comes from nested element
                    
                } else if (qName.equals("description")) { // XXX: check we're directly inside level element
                    
                    if (level == null) {
                        throw new FileFormatException("Encountered description element while not inside level element", loc.getLineNumber(), line, loc.getColumnNumber());
                    }
                    // description text is cdata inside this element
                    
                } else if (qName.equals("grod")) { // XXX: ensure this is inside a level element
                    
                    String id = null;
                    Float stepSize = null;
                    Float startx = null;
                    Float starty = null;
                    int evalsPerStep = 1;
                    
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String aname = attributes.getQName(i);
                        String aval = attributes.getValue(i);
                        
                        if (aname.equals("id")) {
                            id = aval;
                        } else if (aname.equals("step-size")) {
                            try {
                                stepSize = Float.parseFloat(aval);
                            } catch (NumberFormatException ex) {
                                throw new FileFormatException("Couldn't parse numeric step size '"+aval+"'", loc.getLineNumber(), line, loc.getColumnNumber());
                            }
                        } else if (aname.equals("start-x")) {
                            try {
                                startx = Float.parseFloat(aval);
                            } catch (NumberFormatException ex) {
                                throw new FileFormatException("Couldn't parse X coordinate of starting point", loc.getLineNumber(), line, loc.getColumnNumber());
                            }
                        } else if (aname.equals("start-y")) {
                            try {
                                starty = Float.parseFloat(aval);
                            } catch (NumberFormatException ex) {
                                throw new FileFormatException("Couldn't parse Y coordinate of starting point", loc.getLineNumber(), line, loc.getColumnNumber());
                            }
                        } else if (aname.equals("evals-per-step")) {
                            try {
                                evalsPerStep = Integer.parseInt(aval);
                            } catch (NumberFormatException ex) {
                                throw new FileFormatException("Couldn't parse evals per step as integer", loc.getLineNumber(), line, loc.getColumnNumber());
                            }
                        } else {
                            handleUnknownAttribute(qName, line, aname, aval);
                        }
                    }
                    
                    checkMandatory(qName, "id", id);
                    checkMandatory(qName, "step-size", stepSize);
                    checkMandatory(qName, "start-x", startx);
                    checkMandatory(qName, "start-y", starty);
                    
                    robot = new Robot(id, "Grod", level, config.getSensorTypes(), config.getGateTypes(), null, new Point2D.Float(startx, starty), stepSize, null, evalsPerStep);

                    setupLabel(robot, attributes);
                    
                } else if (qName.equals("gate-allowance")) { // XXX: ensure we're inside a grod element
                    String gateType = null;
                    Integer count = null;
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String aname = attributes.getQName(i);
                        String aval = attributes.getValue(i);
                        
                        if (aname.equals("type")) {
                            if (!config.getGateTypeNames().contains(aval)) {
                                throw new FileFormatException("Found an allowance for gate type '"+aval+"', which is not defined!", loc.getLineNumber(), line, loc.getColumnNumber());
                            }
                            gateType = aval;
                        } else if (aname.equals("value")) {
                            try {
                                count = Integer.parseInt(aval);
                            } catch (NumberFormatException ex) {
                                throw new FileFormatException("Could not parse gate count '"+aval+"' as an integer.", loc.getLineNumber(), line, loc.getColumnNumber());
                            }
                        } else {
                            handleUnknownAttribute(qName, line, aname, aval);
                        }
                        
                    }
                    
                    checkMandatory(qName, "type", gateType);
                    checkMandatory(qName, "value", count);
                    
                    robot.getCircuit().addGateAllowance(config.getGate(gateType).getGateClass(), count);
                    
                } else if (qName.equals("graphic")) {
                    // could apply to anything that has a sprite
                    Map<String,String> spriteAttribs = new HashMap<String,String>();
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String aname = attributes.getQName(i);
                        String aval = attributes.getValue(i);
                        
                        spriteAttribs.put(aname, aval);
                    }
                    sprite = SpriteManager.load(config.getResourceLoader(), spriteAttribs);
                    
                } else if (qName.equals("switch")) {
                    // switches and side effects
                    String switchId = null;
                    String switchOnEnter = null;
                    boolean switchEnabled = true;
                    Integer x = null;
                    Integer y = null;
                    
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String aname = attributes.getQName(i);
                        String aval = attributes.getValue(i);
                        
                        if (aname.equals("id")) {
                            switchId = aval;
                        } else if (aname.equals("loc-x")) {
                            try {
                                x = Integer.parseInt(aval);
                            } catch (NumberFormatException ex) {
                                throw new FileFormatException("Couldn't parse X coordinate of switch", loc.getLineNumber(), line, loc.getColumnNumber());
                            }
                        } else if (aname.equals("loc-y")) {
                            try {
                                y = Integer.parseInt(aval);
                            } catch (NumberFormatException ex) {
                                throw new FileFormatException("Couldn't parse Y coordinate of switch", loc.getLineNumber(), line, loc.getColumnNumber());
                            }
                        } else if (aname.equals("on-enter")) {
                            switchOnEnter = aval;
                        } else if (aname.equals("enabled")) {
                            switchEnabled = Boolean.parseBoolean(aval);
                        } else {
                            handleUnknownAttribute(qName, line, aname, aval);
                        }
                    }
                    
                    checkMandatory(qName, "id", switchId);
                    checkMandatory(qName, "loc-x", x);
                    checkMandatory(qName, "loc-y", y);
                    
                    Point switchPosition = new Point(x, y);
                    
                    // XXX this might not work (maybe the switch should have a level reference and get the interpreter as needed)
                    newSwitch = new LevelConfig.Switch(switchPosition, switchId, null, null, switchOnEnter, level.getBshInterpreter());
                    newSwitch.setEnabled(switchEnabled);
                    setupLabel(newSwitch, attributes);
                    // gets added to level in endElement
                    
                } else if (qName.equals("map")) {
                    // The squares of the map
                    // requires the charData inside this element, so it's handled in the endElement
                } else {
                    throw new FileFormatException("Unrecognised XML element <"+qName+">", loc.getLineNumber(), line, loc.getColumnNumber());
                }
            } catch (FileFormatException ex) {
                throw new SAXException(ex);
            } catch (SpriteLoadException ex) {
                throw new SAXException(ex);
            }
        }

        private void handleUnknownAttribute(String qName, final String line, String attribName, String attribValue) {
            if (!attribName.startsWith("label")) {
                warnings.add(new FileFormatException(
                        "Unknown attribute "+attribName+"=\""+attribValue+"\" in element <"+qName+">",
                        loc.getLineNumber(), line, loc.getColumnNumber()));
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            charData.append(ch, start, length);
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            final String line = "Original line not available";
            
            try {
                if (qName.equals("square")) {
                    try {
                        config.addSquareType(squareName, squareChar, !squareAttributes.contains(WALL_FLAG), squareGraphicsFileName, squareSensors);
                    } catch (SpriteLoadException ex) {
                        throw new SAXException("Failed to load a sprite", ex);
                    }
                } else if (qName.equals("description")) {
                    String desc = charData.toString().trim();
                    if (desc.length() > 0) {
                        level.setDescription(desc);
                    } else {
                        level.setDescription(null);
                    }
                } else if (qName.equals("grod")) {
                    if (sprite ==  null) throw new FileFormatException("The <grod> element must contain a nested <graphic> element!", loc.getLineNumber(), line, loc.getColumnNumber());
                    robot.setSprite(sprite);
                    level.addRobot(robot);
                    robot = null;
                } else if (qName.equals("switch")) {
                    if (sprite ==  null) throw new FileFormatException("The <switch> element must contain a nested <graphic> element!", loc.getLineNumber(), line, loc.getColumnNumber());
                    newSwitch.setSprite(sprite);
                    level.addSwitch(newSwitch);
                } else if (qName.equals("map")) {
                    try {
                        
                        // split on \r\n or \n line ends (doesn't handle old mac style \r)
                        String[] allRows = charData.toString().split("\r?\n");

                        // skip first and last rows because the first one is the empty space after the <map> tag and the last is the empty space before for </map> tag.
                        String[] rows = new String[allRows.length-2];
                        for (int i = 0; i < allRows.length-2; i++) {
                            rows[i] = allRows[i+1];
                        }
                        
                        int y;
                        for (y = 0; y < rows.length && y < level.getHeight(); y++) {
                            for (int x = 0; x < level.getWidth(); x++) {
                                if (x < rows[y].length()) {
                                    level.setSquare(x, y, config.getSquare(rows[y].charAt(x)));
                                } else {
                                    level.setSquare(x, y, config.getSquare(' '));
                                }
                            }
                        }
                        
                        // pad out unspecified lines with spaces
                        for (; y < level.getHeight(); y++) {
                            for (int i = 0; i < level.getWidth(); i++) {
                                level.setSquare(i, y, config.getSquare(' '));
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        throw new FileFormatException(
                                "Error in level \""+level.getName()+"\": index out of bounds at "
                                +ex.getMessage(), loc.getLineNumber(), line, -1);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new FileFormatException(
                                "General error reading map for level \""+level.getName()+"\": "
                                +ex.getMessage()+"\n\nA stack trace is available on the system console.",
                                loc.getLineNumber(), line, -1);
                    }

                } else if (qName.equals("level")) {
                    level.snapshotState();
                    config.addLevel(level);
                    level = null;
                }
            } catch (FileFormatException ex) {
                throw new SAXException(ex);
            }
        }
        
        private void setupLabel(Labelable obj, Attributes attributes) throws FileFormatException {
            
            String labelText = attributes.getValue("label");
            obj.setLabel(labelText);

            String enabled = attributes.getValue("label-enabled");
            if (enabled == null || enabled.equals("true")) {
                obj.setLabelEnabled(true);
            } else if (enabled.equals("false")) {
                obj.setLabelEnabled(false);
            } else {
                throw new FileFormatException(
                        "The value of attribute label-enabled must be \"true\" or \"false\"." +
                        " You used \""+enabled+"\"", loc.getLineNumber(),
                        null, loc.getColumnNumber());
            }
            
            try {
                String position = attributes.getValue("label-direction");
                if (position == null) obj.setLabelDirection(Direction.EAST);
                else obj.setLabelDirection(Direction.get(position));
            } catch (IllegalArgumentException e) {
                throw new FileFormatException(e.getMessage(), loc.getLineNumber(), null, loc.getColumnNumber());
            }
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

    private static void debug(String fmt, Object ... args) {
        if (debugging) {
            System.out.printf(fmt, args);
        }
    }

}
