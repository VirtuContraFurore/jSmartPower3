package it.sssup.jsmartpower3;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class WifiCtrlPanel extends JPanel implements ActionListener {
	
	private WifiCtrlListener listener;
	
	/* Graphical elements */
	private JLabel status;
	private JLabel ssid_lbl;
	private JLabel udp_port_lbl;
	private JLabel udp_rec_address_lbl;
	private JLabel info;
	private JButton ssid;
	private JButton udp_port;
	private JButton udp_rec_address;
	private JButton refresh_status;

	public WifiCtrlPanel() {
		this.listener = null;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = c.weighty = 1.00;
		
		/* First line */
		c.gridx = c.gridy = 0;
		c.gridwidth = 2;
		this.status = new JLabel();
		this.add(status, c);
		
		/* Second line */
		c.gridy++;
		c.gridwidth = 1;
		c.weightx = 0.10;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		this.ssid_lbl = new JLabel("SSID: ");
		this.add(ssid_lbl, c);
		
		c.gridx++;
		c.weightx = 0.90;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(0, 0, 0, 10);
		this.ssid = new JButton();
		this.add(ssid, c);
		c.insets = new Insets(0, 0, 0, 0);
		
		/* Third line */
		c.gridx = 0;
		c.gridy++;
		c.weightx = 0.10;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		this.udp_port_lbl = new JLabel("UDP port: ");
		this.add(udp_port_lbl, c);
		
		c.gridx++;
		c.weightx = 0.90;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(0, 0, 0, 10);
		this.udp_port = new JButton();
		this.add(udp_port, c);
		c.insets = new Insets(0, 0, 0, 0);
		
		/* Fourth line */
		c.gridx = 0;
		c.gridy++;
		c.weightx = 0.10;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		this.udp_rec_address_lbl = new JLabel("UDP addr: ");
		this.add(udp_rec_address_lbl, c);
		
		c.gridx++;
		c.weightx = 0.90;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(0, 0, 0, 10);
		this.udp_rec_address = new JButton();
		this.add(udp_rec_address, c);
		c.insets = new Insets(0, 0, 0, 0);
		
		/* Fifth line */
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		this.info = new JLabel("no info");
		this.add(info, c);
		
		/* Sixth line */
		c.gridx = 0;
		c.gridy++;
		c.weighty = 1.00;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		this.refresh_status = new JButton("Refresh device wifi status");
		this.add(refresh_status, c);
		
		this.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK));
		this.setConnectionInfo("click to edit", "click to edit", "click to edit");
		this.setConnected(false);
		
		/* Wire logic */
		this.refresh_status.addActionListener(this);
		this.ssid.addActionListener(this);
		this.udp_port.addActionListener(this);
		this.udp_rec_address.addActionListener(this);
	}
	
	/**
	 * Set if serial port is connected or not, in case something goes wrong and disconnection happens
	 * @param connected
	 */
	public void setConnected(boolean connected) {
		this.status.setText("WIFI " + ((connected) ? "CONNECTED" : "NOT CONNECTED"));
		if(!connected)
			this.setAdditionalInfo("no sig info");
	}
	
	/**
	 * Set connection details to display
	 * @param ssid
	 * @param udp_addr
	 * @param udp_port
	 */
	public void setConnectionInfo(String ssid, String udp_addr, String udp_port) {
		this.ssid.setText(ssid);
		this.udp_rec_address.setText(udp_addr);
		this.udp_port.setText(udp_port);
	}
	
	public void setAdditionalInfo(String info) {
		this.info.setText(info);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		
		if(s instanceof JButton) {
			((JButton) s).setEnabled(false);
		}
		
		if(s == this.refresh_status) {
			this.setConnected(this.listener.refreshWifiStatus());
			
		} else if(s == this.ssid) {
			String [] aps = this.listener.scanAP();
			if(aps != null) {
				String ssid = (String) JOptionPane.showInputDialog(
	                    this, "Select SSID name", "Edit SSID", JOptionPane.QUESTION_MESSAGE,
	                    null, aps, null);
				
				if(ssid != null) {
					String pass = JOptionPane.showInputDialog(AppWindow.getIstance(), "Please enter passphrase for '"+ssid+"'",
							"Enter passphrase", JOptionPane.QUESTION_MESSAGE);
					if(pass != null) {
						if(this.listener.selectAP(ssid, pass)) {
							this.setConnected(true);
							this.listener.refreshWifiStatus();
						} else {
							this.setConnected(false);
						}
					} else {
						this.listener.selectAP(null, null);
					}
				} else {
					this.listener.selectAP(null, null);
				}
			}
			
		} else if(s == this.udp_port) {
			String val = JOptionPane.showInputDialog(AppWindow.getIstance(), "Provide UDP port number (port > 0 and port < 10000)",
					"Enter UDP port", JOptionPane.QUESTION_MESSAGE);
			if(val != null){
				val = val.trim();
				try {
					int port = Integer.parseInt(val);
					if(this.listener.changeUdpPort(port))
						this.udp_port.setText(val);
				} catch (Exception ignored) { }
			}
		} else if(s == this.udp_rec_address) {
			String val = JOptionPane.showInputDialog(AppWindow.getIstance(), "Provide receiver address for UDP packets",
					"Enter UDP ip address", JOptionPane.QUESTION_MESSAGE);
			if(val != null) {
				val = val.trim();
				if(val.matches(""
						+ "\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
						+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
						+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
						+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b")) {
					if(this.listener.changeUdpAddr(val))
						this.udp_rec_address.setText(val);
				}
			}
		}
		
		if(s instanceof JButton) {
			((JButton) s).setEnabled(true);
		}
		
	}
	
	public void setWifiCtrlListener(WifiCtrlListener l) {
		this.listener = l;
	}
	
	public interface WifiCtrlListener {
		
		/**
		 * Scan available APs
		 * @return String of AP list
		 */
		public String[] scanAP();
		
		/**
		 * @param ap Access point name previously scanned 
		 * @param passphrase
		 * @return true on success, false on error
		 */
		public boolean selectAP(String ap, String passphrase);
		
		/**
		 * @param port
		 * @return true on success, false on error
		 */
		public boolean changeUdpPort(int port);
		
		/**
		 * @param addr
		 * @return true on success, false on error
		 */
		public boolean changeUdpAddr(String addr);
		
		/**
		 * Ask to refresh wifi status
		 * @return true if connected, false if not connected
		 */
		public boolean refreshWifiStatus();
		
	}
}
