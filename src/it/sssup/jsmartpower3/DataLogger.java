package it.sssup.jsmartpower3;

import javax.swing.SwingUtilities;

import it.sssup.jsmartpower3.DataCtlrPanel.DataCtrlListener;
import it.sssup.jsmartpower3.LogCtrlPanel.LogCtrlListener;

public class DataLogger implements DataCtrlListener, LogCtrlListener{
	
	private SerialService serial;
	private WifiService wifi;
	
	public DataLogger() {
		
		this.serial = new SerialService();
		this.wifi = new WifiService(this.serial);
		
		/* Set default log source */
		this.logSourceChanged(LogCtrlListener.SOURCE_SERIAL);
	}
	
	/**
	 * Log a new line
	 * @param line
	 */
	public void logLine(final String line) {
		SwingUtilities.invokeLater(new Runnable() { public void run() {
				AppWindow.getIstance().getLog().addLogLine(line);
			} });
	}
	
	public SerialService getSerialService() {
		return this.serial;
	}
	
	public WifiService getWifiService() {
		return this.wifi;
	}

	@Override
	public void logSourceChanged(int source) {
		if(source == LogCtrlListener.SOURCE_SERIAL) {
			this.wifi.routePacketsTo(null);
			this.serial.routePacketsTo(this);
		} else if (source == LogCtrlListener.SOURCE_UDP) {
			this.serial.routePacketsTo(null);
			this.wifi.routePacketsTo(this);
		}
	}
	
	@Override
	public String createNewFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean importNewFile(String file_path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean clearFile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cropFile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean startLogging() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stopLogging() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean outputDirectoryChanged(String dir_path) {
		// TODO Auto-generated method stub
		return false;
	}

}
