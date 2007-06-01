/*
 * Created on May 2, 2007
 *
 * This code belongs to Jonathan Fuerth
 */
package net.bluecow.robot.editor;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;

import net.bluecow.robot.GameConfig;
import net.bluecow.robot.LevelConfig;
import net.bluecow.robot.RobotUtils;

public class OrganizeLevelsAction extends AbstractAction {

    private final Component owningComponent;
    private final GameConfig gameConfig;
    
    public OrganizeLevelsAction(Component owningComponent, GameConfig gameConfig) {
        super("Organize Levels...");
        if (gameConfig == null) {
            throw new NullPointerException("GameConfig must not be null");
        }
        this.owningComponent = owningComponent;
        this.gameConfig = gameConfig;
    }
    
    public void actionPerformed(ActionEvent e) {
        showOrganizeDialog();
    }

    public void showOrganizeDialog() {
        JDialog d = RobotUtils.makeOwnedDialog(owningComponent, "Organize Levels");
        final JList list = new JList(new LevelChooserListModel(gameConfig));
        list.setCellRenderer(new LevelChooserListRenderer());
        
        JButton moveUpButton = new JButton("Move Up");
        moveUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LevelConfig level = (LevelConfig) list.getSelectedValue();
                if (level == null) return;
                int index = list.getSelectedIndex();
                if (index > 0) {
                    gameConfig.removeLevel(level);
                    gameConfig.addLevel(index - 1, level);
                    list.setSelectedValue(level, true);
                }
            }
        });

        JButton moveDownButton = new JButton("Move Down");
        moveDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LevelConfig level = (LevelConfig) list.getSelectedValue();
                if (level == null) return;
                int index = list.getSelectedIndex();
                if (index < gameConfig.getLevels().size() - 1) {
                    gameConfig.removeLevel(level);
                    gameConfig.addLevel(index + 1, level);
                    list.setSelectedValue(level, true);
                }
            }
        });

        JComponent cp = (JComponent) d.getContentPane();
        cp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        cp.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        d.add(new JScrollPane(list), gbc);
        
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        d.add(moveUpButton, gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridx = 1;
        d.add(moveDownButton, gbc);
        
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.pack();
        d.setLocationRelativeTo(owningComponent);
        d.setVisible(true);
    }
    
    
}
