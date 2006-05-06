package net.bluecow.robot;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * RobotApplet Main Class
 */
public class RobotApplet extends JApplet {
    

    public void init() {
        final Main main = new Main();
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JComponent cp = (JComponent) getContentPane();
                cp.setLayout(new FlowLayout(FlowLayout.CENTER));
                if (main.getGameConfig().getLevels().isEmpty()) {
                    cp.add(new JLabel("Oops, can't find any levels!"));
                } else {
                    JButton startButton = new JButton("Start Game");
                    startButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            main.setLevel(0);
                        }
                    });
                    cp.add(startButton);
                }
            }
        });
    }
    
}
