/*
 * Created on Apr 12, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.KeyStroke;

import net.bluecow.robot.gate.Gate;

public class GameConfig {

    public class GoodyConfig {
        private String name;
        private Sprite sprite;
        private int value;

        public GoodyConfig(String name, Sprite sprite, int value) {
            super();
            this.name = name;
            this.sprite = sprite;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Sprite getSprite() {
            return sprite;
        }

        public int getValue() {
            return value;
        }
        
        
    }

    public static class GateConfig {
        private String name;
        private KeyStroke accelerator;
        private Class<Gate> gateClass;
        
        /**
         * @param name The gate's identifier in this config
         * @param accelerator The accelerator key for creating a gate of this type
         * @param clazz The class whose instances represent a gate of this type
         */
        public GateConfig(String name, KeyStroke accelerator, Class<Gate> clazz) {
            this.name = name;
            this.accelerator = accelerator;
            this.gateClass = clazz;
        }

        public KeyStroke getAccelerator() {
            return accelerator;
        }
        public Class<Gate> getGateClass() {
            return gateClass;
        }
        public String getName() {
            return name;
        }
    }
    
    public static class SquareConfig implements Square {
        private String name;
        private char mapChar;
        private boolean occupiable;
        private Sprite sprite;
        private Collection<SensorConfig> sensorTypes;
        
        public SquareConfig(String name, char mapChar, boolean occupiable,
                Sprite sprite, Collection<SensorConfig> sensorTypes) {
            this.name = name;
            this.mapChar = mapChar;
            this.occupiable = occupiable;
            this.sprite = sprite;
            this.sensorTypes = Collections.unmodifiableCollection(sensorTypes);
        }

        public String getName() {
            return name;
        }
        
        public char getMapChar() {
            return mapChar;
        }
        
        public boolean isOccupiable() {
            return occupiable;
        }

        public Collection<SensorConfig> getSensorTypes() {
            return sensorTypes;
        }

        public Sprite getSprite() {
            return sprite;
        }
    }
    
    public static class SensorConfig {
        private String id;
        
        public SensorConfig(String id) {
            if (id == null) throw new NullPointerException("Null sensor type not allowed");
            this.id = id;
        }
        
        public String getId() {
            return id;
        }
        
        @Override
        public int hashCode() {
            return id.hashCode();
        }
        
        @Override
        public boolean equals(Object other) {
            return id.equals(((SensorConfig) other).id);
        }
    }
    
    private Map<String, GateConfig> gateTypes = new HashMap<String, GateConfig>();
    private Map<Character, SquareConfig> squareTypes = new HashMap<Character, SquareConfig>();
    private Map<String, SensorConfig> sensorTypes = new LinkedHashMap<String, SensorConfig>();
    private Map<String, GoodyConfig> goodyTypes = new HashMap<String, GoodyConfig>();
    private List<LevelConfig> levels = new ArrayList<LevelConfig>();
    
    @SuppressWarnings("unchecked")
    public void addGate(String gateName, char accelKey, String gateClass) throws ClassNotFoundException {
        gateTypes.put(gateName,
                new GateConfig(gateName,
                        KeyStroke.getKeyStroke(accelKey),
                        (Class<Gate>) Class.forName(gateClass)));
    }

    public Set<String> getGateTypeNames() {
        return Collections.unmodifiableSet(gateTypes.keySet());
    }
    
    public Collection<GateConfig> getGateTypes() {
        return Collections.unmodifiableCollection(gateTypes.values());
    }
    
    public GateConfig getGate(String gateTypeName) {
        return gateTypes.get(gateTypeName);
    }

    public void addSquare(String squareName, char squareChar, boolean occupiable, String graphicsFileName, Collection<String> sensorTypeIdList) throws FileNotFoundException {
        Set<SensorConfig> squareSensorTypes = new HashSet<SensorConfig>();
        for (String st : sensorTypeIdList) {
            squareSensorTypes.add(sensorTypes.get(st));
        }
        squareTypes.put(squareChar, new SquareConfig(
                squareName, squareChar, occupiable, SpriteManager.load(graphicsFileName),
                squareSensorTypes));
    }

    public SquareConfig getSquare(char squareChar) {
        return squareTypes.get(squareChar);
    }

    public void addGoody(String goodyName, String graphicsFileName, int value) throws FileNotFoundException {
        goodyTypes.put(goodyName, new GoodyConfig(
                goodyName, SpriteManager.load(graphicsFileName), value));
    }

    public void addLevel(LevelConfig level) {
        levels.add(level);
    }

    public List<LevelConfig> getLevels() {
        return Collections.unmodifiableList(levels);
    }

    public List<SensorConfig> getSensorTypes() {
        List<SensorConfig> retval = new ArrayList<SensorConfig>(sensorTypes.size());
        for (Map.Entry<String, SensorConfig> entry : sensorTypes.entrySet()) {
            retval.add(entry.getValue());
        }
        return retval;
    }

    public void addSensorType(String typeName) {
        sensorTypes.put(typeName, new SensorConfig(typeName));
    }

    public Object getSensor(String typeName) {
        return sensorTypes.get(typeName);
    }

}
