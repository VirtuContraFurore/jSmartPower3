package it.sssup.jsmartpower3;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class AppWindow extends JFrame {
	
	private static final String app_name = "jSmartPower3";
	private static final Dimension min_size = new Dimension(900, 600);
	private static AppWindow istance;
	
	private boolean populated;
	private ChannelPlotPanel channels;
	private DataCtlrPanel data;
	private SerialCtrlPanel serial;
	private WifiCtrl wifi;
	private LogCtrlPanel log;
	
	private AppWindow() {
		super();
		this.populated = false;
		this.setSize(min_size);
	   	this.setMinimumSize(min_size);
	   	this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	/**
	 * Populate and show main window
	 * @param channels number of channels to display
	 */
	public void populate(int channel_count) {
		if(populated)
			return;
		
		populated = true;
		Container pane = this.getContentPane();
		pane.setLayout(new GridBagLayout());
	
		this.channels = new ChannelPlotPanel(channel_count);
		AppWindow.gridBagAdd(pane, channels,
				0, 0, 1, 1, 0.80, 0.90);
		
		this.data = new DataCtlrPanel();
		AppWindow.gridBagAdd(pane, data,
				0, 1, 1, 1, 0.80, 0.10);
		
		JPanel side_panel = new JPanel();
		side_panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.BLACK));
		side_panel.setLayout(new GridBagLayout());
		AppWindow.gridBagAdd(pane, side_panel, 
				1, 0, 1, 2, 0.20, 1.00);

		this.serial = new SerialCtrlPanel();
		AppWindow.gridBagAdd(side_panel, serial,
				0, 0, 1, 1, 1.00, 0.33);
		
		this.wifi = new WifiCtrl();
		AppWindow.gridBagAdd(side_panel, wifi,
				0, 1, 1, 1, 1.00, 0.33);
		
		this.log = new LogCtrlPanel();
		AppWindow.gridBagAdd(side_panel, log,
				0, 2, 1, 1, 1.00, 0.33);
		
	   	this.setVisible(true);
	}
	
	/**
	 * Set windows name to app_name + custom string
	 * @param postfix used to show [CONNECTED] / [NOT CONNECTED] tag in window name
	 */
	public void setWindowPostfix(String postfix) {
		this.setTitle(app_name + postfix);
	}
	
	public ChannelPlotPanel getChannels() {
		return channels;
	}

	public DataCtlrPanel getData() {
		return data;
	}

	public SerialCtrlPanel getSerial() {
		return serial;
	}

	public WifiCtrl getWifi() {
		return wifi;
	}

	public LogCtrlPanel getLog() {
		return log;
	}

	/**
	 * Get application's main window (a JFrame)
	 * @return a singleton
	 */
	public static AppWindow getIstance() {
		if(istance == null)
			istance = new AppWindow();
		return istance;
	}
	
	/**
	 * Wrapper for handling grid bag layout boilerplate
	 * @param dest destination container
	 * @param comp component to be layed
	 */
	private static void gridBagAdd(Container dest, Component comp, int gridx, int gridy, int lenx, int leny, double wx, double wy) {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = gridx;
		c.gridy = gridy;
		c.gridwidth = lenx;
		c.gridheight = leny;
		c.weightx = wx;
		c.weighty = wy;
		dest.add(comp, c);
	}

}
