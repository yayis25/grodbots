/*
 * Created on Mar 13, 2006
 *
 * This code belongs to Jonathan Fuerth.
 */
package net.bluecow.robot;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.bluecow.robot.gate.Gate;

/**
 * The CircuitStore class is responsible for saving and loading descriptions
 * of circuits.
 * 
 * @author fuerth
 * @version $Id$
 */
public class CircuitStore {

    public static final String MAGIC = "CIRCUIT 1.0";
    
    /**
     * Saves a description of all the gates in the given circuit editor to
     * the given output stream.
     */
    public static void save(OutputStream out, Circuit c) {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
        int nextId = 0;
        Map<Gate,String> idmap = new HashMap<Gate,String>();
        pw.printf(MAGIC+"\n");
        for (Map.Entry<Gate, Rectangle> ent : c.getGatePositions().entrySet()) {
            Gate g = ent.getKey();
            Rectangle r = ent.getValue();
            String id;
            if (g instanceof Robot.RobotSensorOutput) {
                id = g.getLabel();
            } else if (g instanceof Robot.RobotInputsGate) {
                id = "Inputs";
            } else {
                id = String.valueOf(nextId++);
            }
            
            idmap.put(g, id);
            pw.printf("%s [%d,%d,%d,%d] %s\n",
                    id, r.x, r.y, r.width, r.height, g.getClass().getName());
        }
        
        pw.printf("*Connections\n");
        
        for (Map.Entry<Gate, Rectangle> ent : c.getGatePositions().entrySet()) {
            Gate g = ent.getKey();
            String id = idmap.get(g);
            Gate.Input[] inputs = g.getInputs();
            for (int i = 0; i < inputs.length; i++) {
                if (inputs[i].getConnectedGate() != null) {
                    String sourceid = idmap.get(inputs[i].getConnectedGate());
                    pw.printf("%s:%d <- %s\n", id, i, sourceid);
                }
            }
        }
        
        pw.flush();
    }
    
    /**
     * Reads the description of a set of gates from the given input stream
     * and creates the corresponding gates in the given circuit.
     * <p>
     * XXX: should remove Robot argument and just use the inputs and outputs of the circuit.
     * 
     * @throws IOException If the input stream can't be read
     */
    public static void load(InputStream in, Circuit c, Robot robot) throws IOException {
        Pattern gateLine = Pattern.compile("(\\w+) \\[([0-9]+),([0-9]+),([0-9]+),([0-9]+)\\] (.*)");
        Pattern connLine = Pattern.compile("(\\w+):([0-9]+) <- (\\w+)");
        LineNumberReader br = new LineNumberReader(new InputStreamReader(in));
        String gateClassName = null; // declared here so exception messages can use it
        Map<String,Gate> idmap = new HashMap<String,Gate>();
        
        String line = br.readLine();
        if (!MAGIC.equals(line)) {
            throw new FileFormatException(
                    "The first line must be exactly '"+MAGIC+"'" +
                    " (with no leading or trailing space)", 1, line, -1);
        }
        
        // read gate positions and fill idmap
        while ( (line = br.readLine()) != null) {
            line = line.trim();
            if (line.equals("*Connections")) break;
            Matcher m = gateLine.matcher(line);
            if (!m.matches()) {
                throw new FileFormatException(
                        "Invalid line format for gate position",
                        br.getLineNumber(), line, -1);
            }
            String id = m.group(1);
            int x = 0;
            int y = 0;
            try {
                x = Integer.parseInt(m.group(2));
            } catch (NumberFormatException ex) {
                throw new FileFormatException("Could not parse X coordinate", br.getLineNumber(), line, m.start(2));
            }
            try {
                y = Integer.parseInt(m.group(3));
            } catch (NumberFormatException ex) {
                throw new FileFormatException("Could not parse Y coordinate", br.getLineNumber(), line, m.start(3));
            }
            gateClassName = m.group(6);
            Point p = new Point(x, y);
            Gate g;
            if ("net.bluecow.robot.Robot$RobotSensorOutput".equals(gateClassName)) {
                Gate[] robotGates = robot.getOutputs();
                g = null;
                for (Gate roboGate : robotGates) {
                    if (roboGate.getLabel().equals(id)) {
                        g = roboGate; 
                    }
                }
                if (g == null) throw new IOException("Unknwon Robot Sensor '"+id+"'");
            } else if ("net.bluecow.robot.Robot$RobotInputsGate".equals(gateClassName)) {
                g = robot.getInputsGate();
            } else {
                try {
                    g = (Gate) Class.forName(gateClassName).newInstance();
                    c.addGate(g, p);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    throw new FileFormatException(
                            "Couldn't create gate class: "+e.getMessage(),
                            br.getLineNumber(), line, -1);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new FileFormatException(
                            "Couldn't access gate constructor: "+e.getMessage(),
                            br.getLineNumber(), line, -1);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new FileFormatException(
                            "Couldn't find gate class: "+e.getMessage(),
                            br.getLineNumber(), line, -1);
                }
            }
            idmap.put(id, g);
        }
        
        // read gate connections
        while ( (line = br.readLine()) != null) {
            Matcher m = connLine.matcher(line);
            if (!m.matches()) {
                throw new FileFormatException(
                        "Invalid connection line. Required form is " +
                        "TARGETID:INPUT# <- CONNECTTO", br.getLineNumber(), line, -1);
            }
            String id = m.group(1);
            int inpNum = Integer.parseInt(m.group(2));
            String connTo = m.group(3);
            
            Gate target = idmap.get(id);
            Gate.Input targetInput = target.getInputs()[inpNum];
            Gate source = idmap.get(connTo);
            
            targetInput.connect(source);
        }
    }
}
