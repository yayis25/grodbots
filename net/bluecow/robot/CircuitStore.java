/*
 * Created on Mar 13, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.robot;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.bluecow.robot.gate.Gate;

/**
 * The CircuitStore class is responsible for saving and loading descriptions
 * of circuits.
 * 
 * <P>
 * TODO: figure out how to reconnect the restored gates to the robot inputs and outputs
 * TODO: implement connections between gates
 *
 * @author fuerth
 * @version $Id$
 */
public class CircuitStore {
    
    /**
     * Saves a description of all the gates in the given circuit editor to
     * the given output stream.
     */
    public static void save(OutputStream out, CircuitEditor ce) {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
        pw.printf("CIRCUIT 1.0\n");
        for (Map.Entry<Gate, Rectangle> ent : ce.getGatePositions().entrySet()) {
            Gate g = ent.getKey();
            Rectangle r = ent.getValue();
            pw.printf("[%d,%d,%d,%d] %s\n", r.x, r.y, r.width, r.height, g.getClass().getName());
        }
    }
    
    /**
     * Reads the description of a set of gates from the given input stream
     * and creates the corresponding gates in the given circuit editor.
     * @throws IOException If the input stream can't be read
     */
    public static void load(InputStream in, CircuitEditor ce) throws IOException {
        Pattern gateLine = Pattern.compile("\\[([0-9]*),([0-9]*),([0-9]*),([0-9]*)\\] (.*)");
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String gateClassName = null; // declared here so exception messages can use it
        try {
            String line;
            while ( (line = br.readLine()) != null) {
                Matcher m = gateLine.matcher(line);
                if (m.groupCount() != 5) {
                    throw new IOException("Gate description line '"+line+"' ");
                }
                int x = Integer.parseInt(m.group(1));
                int y = Integer.parseInt(m.group(2));
                int w = Integer.parseInt(m.group(3));
                int h = Integer.parseInt(m.group(4));
                gateClassName = m.group(5);
                Point p = new Point(x, y);
                Gate g = (Gate) Class.forName(gateClassName).newInstance();
                ce.addGate(g, p);
            }
        } catch (InstantiationException e) {
            System.out.println("Couldn't create gate class: '"+gateClassName+"'");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.out.println("Couldn't access gate constructor: '"+gateClassName+"'");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't find gate class: '"+gateClassName+"'");
            e.printStackTrace();
        }
    }
}
