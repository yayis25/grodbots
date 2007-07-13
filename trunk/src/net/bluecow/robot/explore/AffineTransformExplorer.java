/*
 * Copyright (c) 2007, Jonathan Fuerth
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Jonathan Fuerth nor the names of other
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * Created on Mar 1, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.explore;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bluecow.robot.Circuit;
import net.bluecow.robot.GameConfig;
import net.bluecow.robot.LevelConfig;
import net.bluecow.robot.Playfield;
import net.bluecow.robot.Robot;
import net.bluecow.robot.Square;
import net.bluecow.robot.GameConfig.GateConfig;
import net.bluecow.robot.GameConfig.SensorConfig;
import net.bluecow.robot.resource.ResourceLoader;
import net.bluecow.robot.resource.SystemResourceLoader;
import net.bluecow.robot.sprite.Sprite;
import net.bluecow.robot.sprite.SpriteLoadException;
import net.bluecow.robot.sprite.SpriteManager;

public class AffineTransformExplorer {
    
    /**
     * The GUI component that houses this transform exporer.
     */
    private JComponent component;
    
    private Robot robot;
    private double robotHeading;
    
    private JSlider robotScaleSlider;
    private JSlider robotHeadingSlider;
    
    private JSlider xShearSlider;
    private JSlider yShearSlider;

    private JSlider xScaleSlider;
    private JSlider yScaleSlider;

    private SliderListener sliderListener = new SliderListener();
    
    public AffineTransformExplorer() throws SpriteLoadException {
        component = new JPanel(new BorderLayout());
        
        ResourceLoader resourceLoader = new SystemResourceLoader();
        GameConfig gc = new GameConfig(resourceLoader);
        gc.addSquareType("basic", 'b', true, "ROBO-INF/images/blacktile.png", new LinkedList<String>());
        
        LevelConfig level = new LevelConfig();
        level.setSize(11, 11);
        Square[][] map = level.getMap();
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                map[x][y] = gc.getSquare('b');
            }            
        }
        Sprite sprite = SpriteManager.load(resourceLoader, "ROBO-INF/images/grod/grod.rsf");
        robot = new RobotWithHeadingOverride("robot", "Robot", level, new LinkedList<SensorConfig>(), new LinkedList<GateConfig>(), sprite, new Point2D.Float(5.5f, 5.5f), 0.1f, null, 1);
        level.addRobot(robot);

        gc.addLevel(level);
        Playfield playfield = new Playfield(gc, level);
        playfield.setHeaderHeight(0);
        
        component.add(playfield, BorderLayout.CENTER);
        component.add(makeTransformPanel(), BorderLayout.EAST);
    }
    
    private JComponent makeTransformPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        
        panel.add(new JLabel("Robot scale"));
        panel.add(robotScaleSlider = new JSlider(0, 300));
        robotScaleSlider.setValue((int) (robot.getSprite().getScale() * 100.0));
        robotScaleSlider.addChangeListener(sliderListener);

        panel.add(new JLabel("Robot heading"));
        panel.add(robotHeadingSlider = new JSlider(0, (int) (Math.PI * 100.0 * 2.0)));
        robotHeadingSlider.setValue((int) (robot.getIconHeading() * 100.0));
        robotHeadingSlider.addChangeListener(sliderListener);
        
        panel.add(new JLabel("Sprite Shear (x)"));
        panel.add(xShearSlider = new JSlider(0, 500));
        xShearSlider.setValue(0);
        xShearSlider.addChangeListener(sliderListener);

        panel.add(new JLabel("Sprite Shear (y)"));
        panel.add(yShearSlider = new JSlider(0, 500));
        yShearSlider.setValue(0);
        yShearSlider.addChangeListener(sliderListener);

        panel.add(new JLabel("Sprite Scale (x)"));
        panel.add(xScaleSlider = new JSlider(0, 500));
        xScaleSlider.setValue(100);
        xScaleSlider.addChangeListener(sliderListener);

        panel.add(new JLabel("Sprite Scale (y)"));
        panel.add(yScaleSlider = new JSlider(0, 500));
        yScaleSlider.setValue(100);
        yScaleSlider.addChangeListener(sliderListener);

        return panel;
    }

    public JComponent getComponent() {
        return component;
    }
    
    private class SliderListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            double robotScale = robotScaleSlider.getValue() / 100.0;
            robotHeading = robotHeadingSlider.getValue() / 100.0;
            
            Sprite s = robot.getSprite();
            s.setScale(robotScale);
            
            AffineTransform transform = new AffineTransform();
            transform.translate(s.getWidth() / 2.0, s.getHeight() / 2.0);

            double shx = xShearSlider.getValue() / 100.0;
            double shy = yShearSlider.getValue() / 100.0;
            transform.shear(shx, shy);
            
            double sx = xScaleSlider.getValue() / 100.0;
            double sy = yScaleSlider.getValue() / 100.0;
            transform.scale(sx, sy);

            transform.translate(-s.getWidth() / 2.0, -s.getHeight() / 2.0);
            s.setTransform(transform);
        }
        
    }
    
    class RobotWithHeadingOverride extends Robot {
        
        public RobotWithHeadingOverride(String id, String name, LevelConfig level, List<SensorConfig> sensorList, Collection<GateConfig> gateConfigs, Sprite sprite, Float startPosition, float stepSize, Circuit circuit, int evalsPerStep) {
            super(id, name, level, sensorList, gateConfigs, sprite, startPosition,
                    stepSize, circuit, evalsPerStep);
        }

        @Override
        public double getIconHeading() {
            return robotHeading;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    AffineTransformExplorer ate = new AffineTransformExplorer();
                    JFrame f = new JFrame("Affine Transform Explorer");
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.add(ate.getComponent());
                    f.pack();
                    f.setLocationRelativeTo(null);
                    f.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Couldn't start the Affine Transform Explorer!\n" +
                            "See the console for a stack trace.");
                    System.exit(1);
                }
            }
        });
        
    }
}
