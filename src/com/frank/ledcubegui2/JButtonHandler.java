package com.frank.ledcubegui2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class JButtonHandler implements ActionListener {

	private MainGUI parent;

	public JButtonHandler(MainGUI parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			JButton jb = (JButton) e.getSource();
			String command = jb.getActionCommand();
			if (command.equals(MainGUI.strPREV)) {
				parent.loadPrevFrame();
			}
			if (command.equals(MainGUI.strNEXT)) {
				parent.loadNextFrame();
			}
			if (command.equals(MainGUI.strUPLOAD)) {
				parent.upload();
			}
			if (command.equals(MainGUI.strUPLOADSTRING)) {
				parent.uploadString();
			}
			if (command.equals(MainGUI.strSAVE)) {
				parent.saveToFile();
			}
			if (command.equals(MainGUI.strLOAD)) {
				parent.loadFromFile();
			}
			if (command.equals(MainGUI.strRAIN)) {
				parent.rain(35);
			}
			if (command.equals(MainGUI.strCLEAR)) {
				parent.clearCurrentFrame();
			}
			if (command.equals(MainGUI.strDELETE)) {
				parent.removeCurrentFrame();
			}
			// if (command.startsWith("+")) {
			// currentFrame.changeDuration(Integer.parseInt(command.substring(1)));
			// System.out.println("Duration changed to: " +
			// currentFrame.getDuration());
			// break;
			// }
			// if (command.startsWith("-")) {
			// currentFrame.changeDuration(Integer.parseInt(command));
			// System.out.println("Duration changed to: " +
			// currentFrame.getDuration());
			// break;
			// }
		} catch (ClassCastException ex) {
			ex.printStackTrace();
		}
	}
}
