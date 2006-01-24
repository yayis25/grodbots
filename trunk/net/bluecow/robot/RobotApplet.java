package net.bluecow.robot;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.*;

/**
 * RobotApplet Main Class
 */
public class RobotApplet extends JApplet {
	private Playfield playfield;

	private PlayfieldModel pfModel;

	private ImageIcon goalIcon;

	private Point initialPosition;

	private ImageIcon robotIcon;

	public void init() {
		URL levelMapURL;

		try {
			levelMapURL = new URL(getDocumentBase(), getParameter("levelmap"));
			URLConnection levelMapURLConnection = levelMapURL.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					levelMapURLConnection.getInputStream()));

			if (!"ROCKY".equals(in.readLine())) {
				throw new IOException("Bad magic!");
			}
			String levelName = in.readLine();
			System.out.println("Reading level \""+levelName+"\"");
			int xSize = Integer.parseInt(in.readLine());
			int ySize = Integer.parseInt(in.readLine());

			int initialX = Integer.parseInt(in.readLine());
			int initialY = Integer.parseInt(in.readLine());
			initialPosition = new Point(initialX, initialY);

			Square[][] map = new Square[xSize][ySize];

			// read the level map (short lines are padded with spaces)
			String line;
			int lineNum = 0;
			while ((line = in.readLine()) != null) {
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

			goalIcon = new ImageIcon(new URL(getDocumentBase(), "cake.png"));
			robotIcon = new ImageIcon(new URL(getDocumentBase(), "robot.png"));
			pfModel = new PlayfieldModel(map);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (pfModel == null) {
					getContentPane().add(new JLabel("Oops, null map!"));
				} else {
					final Robot robot = new Robot(pfModel, initialPosition,
							robotIcon);
					playfield = new Playfield(pfModel, robot);

					final CircuitEditor ce = new CircuitEditor(robot.getOutputs(), robot.getInputsGate());
					JFrame cef = new JFrame("Curcuit Editor");
					cef.getContentPane().add(ce);
					cef.pack();
					cef.setLocation(getX()+getWidth(), getY());
					cef.setVisible(true);

					playfield.setGoalIcon(goalIcon);
					getContentPane().add(playfield, BorderLayout.CENTER);

					final GameLoop gameLoop = new GameLoop(robot, playfield, ce);
					
					final JButton startButton = new JButton("Start Game");
					startButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
                            if (gameLoop.isRunning()) {
                                gameLoop.requestStop();
                                startButton.setText("Start Game");
                            } else {
                                new Thread(gameLoop).start();
                                startButton.setText("Stop Game");
                            }
						}
					});
					final JButton resetButton = new JButton("Reset");
					resetButton.addActionListener(new ActionListener() {
					    public void actionPerformed(ActionEvent e) {
                            robot.setPosition(initialPosition);
                            playfield.repaint();
					    }
					});
					JPanel buttonPanel = new JPanel(new FlowLayout());
					buttonPanel.add(startButton);
					buttonPanel.add(resetButton);
					getContentPane().add(buttonPanel, BorderLayout.SOUTH);

					// XXX: shouldn't be necessary, but applet shows up blank otherwise
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							getContentPane().validate();
							getContentPane().repaint();
						}
					});
				}
			}
		});
	}
}
