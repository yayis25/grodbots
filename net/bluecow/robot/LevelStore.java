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
import java.util.List;
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
    public static List<PlayfieldModel> loadLevels(InputStream inStream) throws IOException {
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
        if (major != 2) {
            throw new FileFormatException(
                    "Map file major version "+major+
                    " is not supported (only version 2 is supported)",
                    in.getLineNumber(), magic, magicMatcher.start(1));
        }
        System.out.println("Reading map file version "+major+"."+minor);
        
        List<PlayfieldModel> levels = new ArrayList<PlayfieldModel>();
        String line = null;
        try {
            while ((line = in.readLine()) != null) {
                String levelName = line;
                System.out.print("Found level \""+levelName+"\"");
                
                int xSize = Integer.parseInt(in.readLine());
                int ySize = Integer.parseInt(in.readLine());
                System.out.println(" ("+xSize+"x"+ySize+")");
                
                float initialX = Float.parseFloat(in.readLine());
                float initialY = Float.parseFloat(in.readLine());
                Point2D.Float initialPosition = new Point2D.Float(initialX, initialY);
                
                float roboStepSize = Float.parseFloat(in.readLine());
                
                Square[][] map = new Square[xSize][ySize];
                
                // read the level map (short lines are padded with spaces)
                int lineNum = 0;
                for (;;) {
                    line = in.readLine();
                    if (line == null) break;
                    if (line.equals("*")) break;
                    System.out.println(line);
                    for (int i = 0; i < xSize; i++) {
                        if (i < line.length()) {
                            map[i][lineNum] = new Square(line.charAt(i));
                        } else {
                            map[i][lineNum] = new Square(Square.EMPTY);
                        }
                    }
                    lineNum += 1;
                }
                
                // pad out unspecified lines with spaces
                for (; lineNum < ySize; lineNum++) {
                    for (int i = 0; i < xSize; i++) {
                        map[i][lineNum] = new Square(Square.EMPTY);
                    }
                }
                
                PlayfieldModel pf = new PlayfieldModel(
                        map, levelName, initialPosition, roboStepSize);
                levels.add(pf);
            }
        } catch (NumberFormatException e) {
            throw new FileFormatException(
                    "Could not parse the number: "+e.getMessage(),
                    in.getLineNumber(), line, -1);
        }
        return levels;
    }
}
