package com.frank.ledcubegui2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

public class JCheckboxHandler implements ActionListener {

    private MainGUI parent;

    public JCheckboxHandler(MainGUI parent) {
        this.parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            JCheckBox cb = (JCheckBox) e.getSource();
            String command = cb.getActionCommand();
            System.out.println(command);
            int x = Character.getNumericValue(command.charAt(0));
            int y = Character.getNumericValue(command.charAt(1));
            int z = Character.getNumericValue(command.charAt(2));
            parent.toggleBit(x, y, z);
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }
    }
}
