/*
 * Created on Mar 19, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The LevelStore is responsible for loading in level descriptions.  If I
 * ever make an in-game level editor, this class will also be responsible
 * for saving out level descriptions.
 *
 * @author fuerth
 * @version $Id$
 */
public class LevelStore {
    
    public static final String V3_SENSORS_HEADER = "Sensors";
    public static final String V3_GATE_HEADER = "Gate Types";
    public static final String V3_SQUARE_HEADER = "Square Types";
    public static final String V3_GOODIES_HEADER = "Goodies";
    public static final String V3_LEVEL_GROD_HEADER = "Grods";
    public static final String V3_LEVEL_SIZE_HEADER = "Size";
    public static final String V3_LEVEL_SWITCH_HEADER = "Switches";
    public static final String WALL_FLAG = "WALL";
    
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
    public static GameConfig loadLevels(InputStream inStream) throws IOException {
        LineNumberReader in = new LineNumberReader(new InputStreamReader(inStream));
        
        Pattern magicPattern = Pattern.compile("^ROCKY ([0-9]+)\\.([0-9]+)$");
        String magic = in.readLine();
        Matcher magicMatcher = magicPattern.matcher(magic);
        if (!magicMatcher.matches()) {
            throw new FileFormatException(
                    "Bad magic! Level descriptions must begin with the line " +
                    "\"ROCKY M.m\" where M is the major version and m is the " +
                    "minor version. M and m must both be integers.",
                    in.getLineNumber(), magic, 0);
        }
        int major = Integer.parseInt(magicMatcher.group(1));
        int minor = Integer.parseInt(magicMatcher.group(2));
        System.out.println("Found map file version "+major+"."+minor);
        if (major == 2) {
            //return loadVersion2LevelFile(in);
            //FIXME: implement this
            throw new FileFormatException("Backward compatibility with version 2 level file not yet implemented.", 1, magic, 0);
        } else if (major == 3) {
            return loadVersion3LevelFile(in);
        } else {
            throw new FileFormatException(
                    "Map file major version "+major+
                    " is not supported (only versions 2.x and 3.x are supported)",
                    in.getLineNumber(), magic, magicMatcher.start(1));

        }
    }

    private static GameConfig loadVersion3LevelFile(LineNumberReader in) throws IOException {
        GameConfig config = new GameConfig();
        String line = null;
        Pattern pat;
        
        // subroutine: load an animated sprite with multiple images and a certain delay between frames
        
        // sensor types (square attributes)
        line = in.readLine();
        if (line == null) {
            throw new FileFormatException("Expected Sensors header; got EOF", in.getLineNumber(), null, -1);
        } else if (!line.equals(V3_SENSORS_HEADER)) {
            throw new FileFormatException("Expected Sensors header", in.getLineNumber(), null, -1);
        } else {
            while ((line = in.readLine()) != null) {
                if (!line.matches("\\s+.*")) break;
                config.addSensorType(line.trim());
            }
        }

        debug("Found sensors: %s", config.getSensorTypes());
        
        // gate types
        if (line == null) {
            throw new FileFormatException("Expected Gate Types header line; got EOF", in.getLineNumber(), null, -1);
        } else if (!line.equals(V3_GATE_HEADER)) {
            throw new FileFormatException("Expected Gate Types header line 'Gate Types'", in.getLineNumber(), line, 0);
        } else {
            pat = Pattern.compile("(\\w+)\\s+(.+)\\s+(.+)");
            while ((line = in.readLine()) != null) {
                if (!line.matches("\\s+.*")) break;
                Matcher m = pat.matcher(line.trim());
                if (!m.matches()) {
                    throw new FileFormatException("Invalid gate description line.  Format is 'name key class'.", in.getLineNumber(), line, 0);
                }
                String gateName = m.group(1);
                String accelKey = m.group(2);
                String gateClass = m.group(3);
                
                try {
                    config.addGate(gateName, accelKey, gateClass);
                } catch (ClassNotFoundException e) {
                    throw new FileFormatException("Could not find gate class '"+gateClass+"'", in.getLineNumber(), line, m.start(3));
                }
            }
        }
        
        // square types
        if (line == null) {
            throw new FileFormatException("Expected Square Types header line; got EOF", in.getLineNumber(), null, -1);
        } else if (!line.equals(V3_SQUARE_HEADER)) {
            throw new FileFormatException("Expected Square Types header line 'Square Types'", in.getLineNumber(), line, 0);
        } else {
            pat = Pattern.compile("(.+)\\s+(.+)\\s+(.)\\s+([^ \\t]+)\\s*(.*)");
            while ((line = in.readLine()) != null) {
                if (!line.matches("\\s+.*")) break;
                Matcher m = pat.matcher(line.trim());
                if (!m.matches()) {
                    throw new FileFormatException("Invalid square description line.  " +
                       "Format is 'name flag[,flag[,flag[, ...]]] char graphics_file " +
                       "[sensor[,sensor[,sensor[, ...]]]]'.",
                       in.getLineNumber(), line, 0);
                }
                String squareName = m.group(1);
                String flagsDesc = m.group(2);
                List<String> flagsList = Arrays.asList(flagsDesc.split(","));
                char squareChar = m.group(3).charAt(0);
                if (squareChar == '_') squareChar = ' ';
                String graphicsFileName = m.group(4);
                String sensorTypesDesc = m.group(5);
                List<String> sensorTypesList =
                    new ArrayList<String>(Arrays.asList(sensorTypesDesc.split(",")));
                ListIterator<String> it = sensorTypesList.listIterator();
                while (it.hasNext()) {
                    String type = it.next();
                    if (type.equals("")) {
                        it.remove(); // this accommodates empty sensor type lists (i.e. for walls)
                    } else if (config.getSensor(type) == null) {
                        throw new FileFormatException(
                                "Undeclared sensor type '"+type+"'",
                                in.getLineNumber(), line, m.start(4));
                    }
                }
                config.addSquare(squareName, squareChar, !flagsList.contains(WALL_FLAG), graphicsFileName, sensorTypesList);
            }
        }
        
        // goodies
//        if (line == null) {
//            throw new FileFormatException("Expected Goodies header line; got EOF", in.getLineNumber(), null, -1);
//        } else if (!line.equals(V3_GOODIES_HEADER)) {
//            throw new FileFormatException("Expected Goodies header line", in.getLineNumber(), line, 0);
//        } else {
//            pat = Pattern.compile("(\\w+)\\s+(.+)\\s+([0-9]+)");
//            while ((line = in.readLine()) != null) {
//                if (!line.matches("\\s+.*")) break;
//                Matcher m = pat.matcher(line.trim());
//                if (!m.matches()) {
//                    throw new FileFormatException("Invalid goody description line.  Format is 'name graphics_file pointval'.", in.getLineNumber(), line, 0);
//                }
//                String goodyName = m.group(1);
//                String graphicsFileName = m.group(2);
//                int value;
//                try {
//                    value = Integer.parseInt(m.group(3));
//                } catch (NumberFormatException ex) {
//                    throw new FileFormatException("Couldn't parse numeric point value '"+m.group(3)+"'", in.getLineNumber(), line, m.start(3));
//                }
//                config.addGoody(goodyName, graphicsFileName, value);
//            }
//        }
        
        // maps
        while (line != null) {
            LevelConfig level = new LevelConfig();
            pat = Pattern.compile("Map (.*)");
            {
                Matcher m = pat.matcher(line); 
                if (!m.matches()) {
                    throw new FileFormatException("Expected map declaration", in.getLineNumber(), line, 0);
                } else {
                    level.setName(m.group(1));
                }
            }
            // grods
            line = in.readLine();
            if (line == null) {
                throw new FileFormatException("Expected Grods header line; got EOF", in.getLineNumber(), null, -1);
            } else if (!line.equals(V3_LEVEL_GROD_HEADER)) {
                throw new FileFormatException("Expected Grods header line", in.getLineNumber(), line, 0);
            } else {
                //  Grod ROBO-INF/images/robot_00.png 0.1 3.5,2.5 AND:10 OR:10 NOT:10 NAND:10 NOR:10
                pat = Pattern.compile("(\\w+)\\s+([^ \\t]+)\\s+([0-9.]+)\\s+([0-9.]+),([0-9.]+)\\s+(.*)");
                while ((line = in.readLine()) != null) {
                    if (!line.matches("\\s+.*")) break;
                    Matcher m = pat.matcher(line.trim());
                    if (!m.matches()) {
                        throw new FileFormatException(
                                "Invalid Grod description line.  Format is" +
                                " 'name graphics_file stepsize startx,starty gate_allowances'.",
                                in.getLineNumber(), line, 0);
                    }
                    String name = m.group(1);
                    String graphicsFile = m.group(2);
                    float stepSize;
                    try {
                        stepSize = Float.parseFloat(m.group(3));
                    } catch (NumberFormatException ex) {
                        throw new FileFormatException("Couldn't parse numeric step size '"+m.group(3)+"'", in.getLineNumber(), line, m.start(3));
                    }
                    
                    float startx, starty;
                    try {
                        startx = Float.parseFloat(m.group(4));
                    } catch (NumberFormatException ex) {
                        throw new FileFormatException("Couldn't parse X coordinate of starting point", in.getLineNumber(), line, m.start(4));
                    }
                    try {
                        starty = Float.parseFloat(m.group(5));
                    } catch (NumberFormatException ex) {
                        throw new FileFormatException("Couldn't parse Y coordinate of starting point", in.getLineNumber(), line, m.start(4));
                    }
                    Robot robot = new Robot(name, level, config.getSensorTypes(), graphicsFile, new Point2D.Float(startx, starty), stepSize);
                    level.addRobot(robot);
                    String gateAllowances = m.group(6);
                    String[] gateAllowancePairs = gateAllowances.split("\\s+");
                    Pattern allowancePat = Pattern.compile("(\\w+):(-?[0-9]+)");
                    for (String allowance : gateAllowancePairs) {
                        Matcher am = allowancePat.matcher(allowance);
                        if (!am.matches()) {
                            throw new FileFormatException("Could not parse gate allowance '"+allowance+"'. Expected format is 'GATENAME:count'", in.getLineNumber(), line, m.start(6));
                        }
                        String gateType = am.group(1);
                        if (!config.getGateTypeNames().contains(gateType)) {
                            throw new FileFormatException("Found an allowance for gate type '"+gateType+"', which is not defined!", in.getLineNumber(), line, m.start(6));
                        }
                        int count;
                        try {
                            count = Integer.parseInt(am.group(2));
                        } catch (NumberFormatException ex) {
                            throw new FileFormatException("Could not parse gate count '"+am.group(2)+"' as an integer.", in.getLineNumber(), line, m.start(6));
                        }
                        robot.addGateAllowance(config.getGate(gateType), count);
                    }
                }
            }
            
            // layout
            pat = Pattern.compile(V3_LEVEL_SIZE_HEADER+" ([0-9]+)x([0-9]+)");
            int xSize, ySize;
            if (line == null) {
                throw new FileFormatException("Expected level size line; got EOF", in.getLineNumber(), null, -1);
            } else {
                Matcher m = pat.matcher(line);
                if (!m.matches()) {
                    throw new FileFormatException("Expected level size line", in.getLineNumber(), line, 0);
                }
                try {
                    xSize = Integer.parseInt(m.group(1));
                } catch (NumberFormatException ex) {
                    throw new FileFormatException("Couldn't parse X size of level '"+level.getName()+"'", in.getLineNumber(), line, m.start(1));
                }
                try {
                    ySize = Integer.parseInt(m.group(2));
                } catch (NumberFormatException ex) {
                    throw new FileFormatException("Couldn't parse Y size of level '"+level.getName()+"'", in.getLineNumber(), line, m.start(2));
                }
                level.setSize(xSize, ySize);
            }
            
            // switches and side effects
            line = in.readLine();
            if (line == null) {
                throw new FileFormatException("Expected Switches header, got EOF", in.getLineNumber(), line, -1);
            } else if (!line.trim().equals(V3_LEVEL_SWITCH_HEADER)) {
                throw new FileFormatException("Expected Switches header", in.getLineNumber(), line, 0);
            } else {
                pat = Pattern.compile("([0-9]+),([0-9]+)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s+([^ \\t]+)\\s*(.*)");
                while ((line = in.readLine()) != null) {
                    if (!line.startsWith(" ")) break;
                    Matcher m = pat.matcher(line.trim());
                    if (!m.matches()) {
                        throw new FileFormatException(
                                "Incorrect format for switch line. Format is " +
                                "'x,y name image_path code'.\n" +
                                "Name must be a valid JavaScript identifier" +
                                " (letters, numbers and underscore)", in.getLineNumber(), line, 0);
                    }
                    int x, y;
                    try {
                        x = Integer.parseInt(m.group(1));
                    } catch (NumberFormatException ex) {
                        throw new FileFormatException(
                                "Couldn't parse X coord of switch",
                                in.getLineNumber(), line, m.start(1));
                    }
                    try {
                        y = Integer.parseInt(m.group(2));
                    } catch (NumberFormatException ex) {
                        throw new FileFormatException(
                                "Couldn't parse Y coord of switch",
                                in.getLineNumber(), line, m.start(2));
                    }
                    String switchName = m.group(3);
                    String imagePath = m.group(4);
                    String switchCode = m.group(5);
                    level.addSwitch(x, y, switchName, imagePath, switchCode);
                }
            }
            
            // The squares of the map
            int y = 0;
            for (;;) {
                if (line == null) break;
                if (line.equals("*")) break;
                for (int x = 0; x < xSize; x++) {
                    if (x < line.length()) {
                        System.out.printf("Square '%c' : %s\n", line.charAt(x), config.getSquare(line.charAt(x)));
                        level.setSquare(x, y, config.getSquare(line.charAt(x)));
                    } else {
                        level.setSquare(x, y, config.getSquare(' '));
                    }
                }
                y += 1;
                line = in.readLine();
            }
            
            // pad out unspecified lines with spaces
            for (; y < ySize; y++) {
                for (int i = 0; i < xSize; i++) {
                    level.setSquare(i, y, config.getSquare(' '));
                }
            }
            
            config.addLevel(level);
        }
        
        return config;
    }

    private static void debug(String fmt, Object ... args) {
        System.out.printf(fmt, args);
    }

    /**
     * Reads everything after the magic line in a version 2.0 level file.
     * 
     * @param in
     * @return A list of the PlayfieldModel objects described in the file (one per level).
     * @throws IOException
     */
//    private static List<PlayfieldModel> loadVersion2LevelFile(LineNumberReader in) throws IOException {
//        
//        List<PlayfieldModel> levels = new ArrayList<PlayfieldModel>();
//        String line = null;
//        try {
//            while ((line = in.readLine()) != null) {
//                String levelName = line;
//                System.out.print("Found level \""+levelName+"\"");
//                
//                int xSize = Integer.parseInt(in.readLine());
//                int ySize = Integer.parseInt(in.readLine());
//                System.out.println(" ("+xSize+"x"+ySize+")");
//                
//                float initialX = Float.parseFloat(in.readLine());
//                float initialY = Float.parseFloat(in.readLine());
//                Point2D.Float initialPosition = new Point2D.Float(initialX, initialY);
//                
//                float roboStepSize = Float.parseFloat(in.readLine());
//                
//                Square[][] map = new Square[xSize][ySize];
//                
//                // read the level map (short lines are padded with spaces)
//                int lineNum = 0;
//                for (;;) {
//                    line = in.readLine();
//                    if (line == null) break;
//                    if (line.equals("*")) break;
//                    System.out.println(line);
//                    for (int i = 0; i < xSize; i++) {
//                        if (i < line.length()) {
//                            map[i][lineNum] = new Square(line.charAt(i));
//                        } else {
//                            map[i][lineNum] = new Square(Square.EMPTY);
//                        }
//                    }
//                    lineNum += 1;
//                }
//                
//                // pad out unspecified lines with spaces
//                for (; lineNum < ySize; lineNum++) {
//                    for (int i = 0; i < xSize; i++) {
//                        map[i][lineNum] = new Square(Square.EMPTY);
//                    }
//                }
//                
//                PlayfieldModel pf = new PlayfieldModel(
//                        map, levelName, initialPosition, roboStepSize);
//                levels.add(pf);
//            }
//        } catch (NumberFormatException e) {
//            throw new FileFormatException(
//                    "Could not parse the number: "+e.getMessage(),
//                    in.getLineNumber(), line, -1);
//        }
//        return levels;
//    }
}
