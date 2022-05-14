package it.sssup.jsmartpower3;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class SerialCtrlPanel extends JPanel implements ActionListener {

	private SerialCtrlListener listener;
	
	/* Graphical elements */
	private JLabel status;
	private JLabel port_lbl;
	private JLabel baud_lbl;
	private JButton open;
	private JButton close;
	private JButton refresh;
	private JComboBox<String> ports;
	private JComboBox<Integer> bauds;
	
	public SerialCtrlPanel() {
		this.listener = null;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = c.weighty = 1.0f;
		
		/* First line */
		c.gridx = c.gridy = 0;
		c.gridwidth = 3;
		this.status = new JLabel();
		this.add(status, c);
		
		/* Second line */
		c.gridy++;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		this.port_lbl = new JLabel("Port: ");
		this.add(port_lbl, c);
		
		c.gridx++;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(0, 0, 0, 10);
		this.ports = new JComboBox<String>();
		this.add(ports, c);
		c.insets = new Insets(0, 0, 0, 0);
		
		/* Third line */
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		this.baud_lbl = new JLabel("Baud: ");
		this.add(baud_lbl, c);
		
		c.gridx++;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(0, 0, 0, 10);
		this.bauds = new JComboBox<Integer>();
		this.add(bauds, c);
		c.insets = new Insets(0, 0, 0, 0);
		
		/* Fourth line */
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		this.open = new JButton("Open");
		this.add(open, c);
		
		c.gridx++;
		this.close = new JButton("Close");
		this.add(close, c);
		
		c.gridx++;
		this.refresh = new JButton("Refresh");
		this.add(refresh, c);
		
		// Prevents editing values of String JComboBox
		if(this.ports.getEditor().getEditorComponent() instanceof JTextField)
			((JTextField) this.ports.getEditor().getEditorComponent()).setEditable(false);		
		
		this.setConnected(false);
		
		/* Glue some logic */
		this.open.addActionListener(this);
		this.close.addActionListener(this);
		this.refresh.addActionListener(this);

	}
	
	public void setSerialCtrlListener(SerialCtrlListener l) {
		this.listener = l;
	}
	
	/**
	 * Set if serial port is connected or not, in case something goes wrong and disconnection happens
	 * @param connected
	 */
	public void setConnected(boolean connected) {
		status.setText("Serial port " + ((connected) ? "CONNECTED" : "NOT CONNECTED"));
		ports.setEditable(!connected);
		open.setEnabled(!connected);
		close.setEnabled(connected);
		refresh.setEnabled(!connected);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		
		System.out.println(e.getActionCommand()+":"+e.getClass());
		
		if(s == this.open) {
			boolean connected = this.listener.serialConnect((String)this.ports.getSelectedItem(), (Integer)this.bauds.getSelectedItem());
			this.setConnected(connected);
		} else if(s == this.close) {
			this.listener.serialDisconnect();
			this.setConnected(false);
		} else if(s == this.refresh) {
			forceRefresh();
		} else if(s == this.bauds) {
			this.listener.serialChangeBaud((Integer)this.bauds.getSelectedItem());
		}
		
	}
	
	/**
	 * Force updating bauds and ports combo box values
	 */
	public void forceRefresh() {
		this.bauds.removeActionListener(this);
		
		/* Save previous value */
		Integer b = (Integer) bauds.getSelectedItem();
		String p = (String) ports.getSelectedItem();
		
		this.bauds.removeAllItems();
		for(int in : this.listener.getSupportedBaudRates()) {
			Integer i = in;
			this.bauds.addItem(i);
			if(b != null && i.equals(b))
				this.bauds.setSelectedItem(i);
		}
		
		this.ports.removeAllItems();
		for(String s : this.listener.getAvailableSerialPorts()) {
			this.ports.addItem(s);
			if(p != null && p.equals(s))
				this.ports.setSelectedItem(s);
		}
		
		this.bauds.addActionListener(this);
	}
	
	/**
	 * Force port selection (from config)
	 * @param port
	 */
	public void setDisplayedPort(String port) {
		this.ports.removeAllItems();
		this.ports.addItem(port);
		this.ports.setSelectedItem(port);
	}
	
	/**
	 * Force baud selection (from config)
	 * @param baud
	 */
	public void setDisplayedBaudrate(int baud) {
		this.bauds.removeActionListener(this);
		this.bauds.removeAllItems();
		this.bauds.addItem(baud);
		this.bauds.setSelectedItem(baud);
		this.bauds.addActionListener(this);
	}
	
	/**
	 * Inside these callbacks it is possible to modify GUI elements without using invokeLater(..)
	 */
	public interface SerialCtrlListener {
		
		/**
		 * Try to connect
		 * @param port
		 * @param baud
		 * @return true if connected, false if not succeded
		 */
		public boolean serialConnect(String port, int baud);
		
		/**
		 * Disconnect from connected port
		 */
		public void serialDisconnect();
		
		/**
		 * Change baud rate
		 */
		public void serialChangeBaud(int baud);
		
		/**
		 * @return an array of serial port names
		 */
		public String[] getAvailableSerialPorts();
		
		/**
		 * @return an array of supported baudrates
		 */
		public int[] getSupportedBaudRates();
	}
	
}
