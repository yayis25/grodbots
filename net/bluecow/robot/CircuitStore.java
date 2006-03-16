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
    
    /**
     * Saves a description of all the gates in the given circuit editor to
     * the given output stream.
     */
    public static void save(OutputStream out, CircuitEditor ce) {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
        int nextId = 0;
        Map<Gate,String> idmap = new HashMap<Gate,String>();
        pw.printf("CIRCUIT 1.0\n");
        for (Map.Entry<Gate, Rectangle> ent : ce.getGatePositions().entrySet()) {
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
        
        for (Map.Entry<Gate, Rectangle> ent : ce.getGatePositions().entrySet()) {
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
     * and creates the corresponding gates in the given circuit editor.
     * @throws IOException If the input stream can't be read
     */
    public static void load(InputStream in, CircuitEditor ce, Robot robot) throws IOException {
        Pattern gateLine = Pattern.compile("(\\w+) \\[([0-9]+),([0-9]+),([0-9]+),([0-9]+)\\] (.*)");
        Pattern connLine = Pattern.compile("(\\w+):([0-9]+) <- (\\w+)");
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String gateClassName = null; // declared here so exception messages can use it
        Map<String,Gate> idmap = new HashMap<String,Gate>();
        try {
            String line = br.readLine();
            if (!"CIRCUIT 1.0".equals(line)) {
                throw new IOException("Invalid first line '"+line+"'");
            }
            
            // read gate positions and fill idmap
            while ( (line = br.readLine()) != null) {
                if (line.equals("*Connections")) break;
                Matcher m = gateLine.matcher(line);
                if (!m.matches()) {
                    throw new IOException("Invalid gate position line '"+line+"' ");
                }
                String id = m.group(1);
                int x = Integer.parseInt(m.group(2));
                int y = Integer.parseInt(m.group(3));
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
                    g = (Gate) Class.forName(gateClassName).newInstance();
                }
                idmap.put(id, g);
                ce.addGate(g, p);
            }
            
            // read gate connections
            while ( (line = br.readLine()) != null) {
                Matcher m = connLine.matcher(line);
                if (!m.matches()) {
                    throw new IOException("Invalid connection line '"+line+"' ");
                }
                String id = m.group(1);
                int inpNum = Integer.parseInt(m.group(2));
                String connTo = m.group(3);
                
                Gate target = idmap.get(id);
                Gate.Input targetInput = target.getInputs()[inpNum];
                Gate source = idmap.get(connTo);
                
                targetInput.connect(source);
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
