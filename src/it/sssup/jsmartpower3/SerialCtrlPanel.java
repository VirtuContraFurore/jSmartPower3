package it.sssup.jsmartpower3;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class SerialCtrlPanel extends JPanel {

	private JLabel title;
	private JLabel port;
	private JLabel baud;
	private JButton open;
	private JButton close;
	private JButton refresh;
	
	
	public SerialCtrlPanel() {
		this.setBackground(Color.green);
	}
	
}
