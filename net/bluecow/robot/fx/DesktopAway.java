/*
 * Created on Jan 31, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.robot.fx;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;

import javax.swing.JOptionPane;
import javax.swing.JWindow;

public class DesktopAway implements Runnable {

    
    public void run() {
        try {
            Robot robot = new Robot();
            Dimension desktopSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screenSize = new Rectangle(0,0,desktopSize.width,desktopSize.height);
            Image roboDesktopSnapshot = robot.createScreenCapture(screenSize);
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            JWindow fsw = new JWindow();
            fsw.setBounds(0,0,1,1);
            fsw.setVisible(true);
            fsw.setIgnoreRepaint(true);
            fsw.setBackground(Color.BLACK);
            

            if (gd.isFullScreenSupported()) {
                JOptionPane.showMessageDialog(null, "Full screen mode is supported.\n" +
                        "Available accelerated memory is "+gd.getAvailableAcceleratedMemory());
            } else {
                JOptionPane.showMessageDialog(null, "Sorry, no full-screen mode.  Go for a coffee.");
            }
            try {
                gd.setFullScreenWindow(fsw);

//                VolatileImage desktop = fsw.createVolatileImage(screenSize.width, screenSize.height);
//                Graphics2D g = desktop.createGraphics();
//                g.drawImage(desktop, 0, 0, desktopSize.width, desktopSize.height, null);
//                g.dispose();
                Image desktop = roboDesktopSnapshot;
                Graphics2D g = (Graphics2D) fsw.getGraphics();
                //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                for (int i = 0; i < desktopSize.height; i += 20) {
                    g.setColor(fsw.getBackground());
                    g.fillRect(0, 0, desktopSize.width, desktopSize.height);
//                    int y = 100;
//                    g.setColor(Color.WHITE);
//                    g.drawString("desktop contents lost? "+desktop.contentsLost(), 0, y += 20);
//                    g.drawString("desktop validate: "+desktop.validate(gc), 0, y += 20);
//                    g.drawString("desktop contents lost? "+desktop.contentsLost(), 0, y += 20);
                    g.drawImage(desktop, i/2, i/2, desktopSize.width - i, desktopSize.height - i, null);
                }
                g.dispose();
                Thread.sleep(1000);
            } finally {
                gd.setFullScreenWindow(null);
            }
        } catch (Exception e) {
            throw new RuntimeException("Problem in DesktopAway", e);
        }
        System.exit(0);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new DesktopAway().run();
    }

}
