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
 * Created on Jan 31, 2006
 *
 * This code belongs to Jonathan Fuerth.
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
