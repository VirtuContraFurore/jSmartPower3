package it.sssup.jsmartpower3;

import javax.swing.SwingUtilities;

import it.sssup.jsmartpower3.DataCtlrPanel.DataCtrlListener;
import it.sssup.jsmartpower3.LogCtrlPanel.LogCtrlListener;

public class DataLogger implements DataCtrlListener, LogCtrlListener{
	
	private SerialService serial;
	private WifiService wifi;
	private long ptime;
	private final int DATA_RATE_AVG = 20;
	private long rec_times[];
	
	public DataLogger() {
		this.rec_times = new long[this.DATA_RATE_AVG];
		
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
		if(line.length() < 78) /* avoid truncated lines */
			return;
		
		long c = System.currentTimeMillis();
		long t = c - this.ptime;
		this.ptime = c;
		
		for(int i = this.DATA_RATE_AVG-1; i > 0; i--)
			this.rec_times[i] = this.rec_times[i-1];
		this.rec_times[0] = t;
		
		long sum = 0;
		for(int i = 0; i < this.rec_times.length; i++)
			sum += this.rec_times[i];
		
		final long avg = sum/this.DATA_RATE_AVG;
		
		SwingUtilities.invokeLater(new Runnable() { public void run() {
				AppWindow.getIstance().getLog().addLogLine(line);
				if(avg < 3000)
					AppWindow.getIstance().getLog().setIncomingDataRate((int)avg);
				else
					AppWindow.getIstance().getLog().setIncomingDataRate(-1);
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
