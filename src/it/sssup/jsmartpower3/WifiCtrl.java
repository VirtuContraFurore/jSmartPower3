package it.sssup.jsmartpower3;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class WifiCtrl extends JPanel {
	
	private JLabel status;
	private JLabel ssid_lbl;
	private JLabel upd_port_lbl;
	private JLabel udp_rec_address_lbl;
	private JButton ssid;
	private JButton udp_port;
	private JButton udp_rec_address;
	private JButton refresh_status;

	public WifiCtrl() {
		this.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK));

	}
}
