package it.sssup.jsmartpower3;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class AppContainer extends JFrame {
	
	private static final String app_name = "jSmartPower3";
	private static final Dimension min_size = new Dimension(600, 450);
	private static AppContainer istance;
	
	private boolean populated;
	
	private AppContainer() {
		super();
		this.populated = false;
		this.setSize(min_size);
	   	this.setMinimumSize(min_size);
	   	this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	/**
	 * Populate and show main window
	 */
	public void populate() {
		if(populated)
			return;
		
		populated = true;
				
		Container pane = this.getContentPane();
		
		pane.add(new ChannelPlotPanel(2));
		
	   	this.setVisible(true);
	}
	
	/**
	 * Set windows name to app_name + custom string
	 * @param postfix used to show [CONNECTED] / [NOT CONNECTED] tag in window name
	 */
	public void setWindowPostfix(String postfix) {
		this.setTitle(app_name + postfix);
	}
	
	
	/**
	 * Get contextual JFrame
	 * @return a singleton
	 */
	public static AppContainer getIstance() {
		if(istance == null)
			istance = new AppContainer();
		return istance;
	}

}
