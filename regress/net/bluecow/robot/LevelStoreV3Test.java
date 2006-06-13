/*
 * Created on Apr 10, 2006
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot;

import junit.framework.TestCase;

public class LevelStoreV3Test extends TestCase {
    public void testLoad() {
        String levelData = 
            "ROCKY 3.0\n" +
            "Sensors\n" +
            " Red\n" +
            " Green\n" +
            " Blue\n" +
            "Gate Types\n" +
            " AND a net.bluecow.robot.gate.AndGate\n" +
            " NAND A net.bluecow.robot.gate.NandGate\n" +
            " OR o net.bluecow.robot.gate.OrGate\n" +
            " NOR O net.bluecow.robot.gate.NorGate\n" +
            " NOT n net.bluecow.robot.gate.NotGate\n" +
            "Square Types\n" +
            " WALL WALL X wall.png\n" +
            " BLACK . K black.png\n" +
            " RED . R red.png Red\n" +
            " GREEN . G green.png Green\n" +
            " BLUE . B blue.png Blue\n" +
            " YELLOW . Y yellow.png Red,Green\n" +
            " CYAN . C cyan.png Green,Blue\n" +
            " MAGENTA . M magenta.png Red,Blue\n" +
            " WHITE . W white.png Red,Green,Blue\n" +
            "Goodies\n" +
            " Cake cake.png _win\n" +
            " Buttercup buttercup.png 100\n" +
            " Jelly_Bean jellybean.png 50" +
            "Map Easy Map\n" +
            "Grods\n" +
            " Grod grod/ 10 AND:10 OR:10 NOT:10 NAND:10 NOR:10\n" +
            " Gregory grod/ 10 NOT:10 NAND:3\n" +
            "Size 10x10\n" +
            "Switches\n" +
            " 3,3 GOAL\n" +
            "";
        System.out.println(levelData);
    }
}
