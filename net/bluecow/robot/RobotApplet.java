package net.bluecow.robot;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * RobotApplet Main Class
 */
public class RobotApplet extends JApplet {
    private Playfield playfield;
    
    private String levelName;
    private Square[][] map;
    private ImageIcon goalIcon;
    private Point initialPosition;

    public void init() {
        URL levelMapURL;
        
        try {
            levelMapURL = new URL(getDocumentBase(), getParameter("levelmap"));
            URLConnection levelMapURLConnection = levelMapURL.openConnection();
            BufferedReader in = new BufferedReader(new 
                    InputStreamReader(levelMapURLConnection.getInputStream()));

            if (!"ROCKY".equals(in.readLine())) {
                throw new IOException("Bad magic!");
            }
            levelName = in.readLine();
            int xSize = Integer.parseInt(in.readLine());
            int ySize = Integer.parseInt(in.readLine());

            int initialX = Integer.parseInt(in.readLine());
            int initialY = Integer.parseInt(in.readLine());
            initialPosition = new Point(initialX, initialY);
            
            map = new Square[xSize][ySize];
            
            // read the level map (short lines are padded with spaces)
            String line;
            int lineNum = 0;
            while((line = in.readLine()) != null) {
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
            
            goalIcon = new ImageIcon(new URL(getDocumentBase(), "cake.PNG"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (map == null) {
                    getContentPane().add(new JLabel("Oops, null map!"));
                } else {
                    playfield = new Playfield(map);
                    Robot robot = new Robot(playfield, initialPosition);
                    //circuitEditor = new CircuitEditor(robot);
                    playfield.setGoalIcon(goalIcon);
                    getContentPane().add(playfield);
                }
            }
        });
    }
}
