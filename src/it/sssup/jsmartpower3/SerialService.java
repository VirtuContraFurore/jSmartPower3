package it.sssup.jsmartpower3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.fazecast.jSerialComm.SerialPort;

import it.sssup.jsmartpower3.SerialCtrlPanel.SerialCtrlListener;

public class SerialService implements SerialCtrlListener {
	
	private SerialPort port;
	private InputStream in;
	private OutputStream out;
	private ConnectionMonitor conn_monitor;
	private PacketMonitor packet_monitor;
	private boolean wifi_setup_mode;
	
	/* routes packet to logger on port connection */
	private boolean deferred_routing;
	private DataLogger deferred_routing_l;
	
	public SerialService() {
		this.port = null; /* not opened */
		this.conn_monitor = null;
		this.wifi_setup_mode = false;
		this.packet_monitor = null;
		this.deferred_routing = false;
		this.deferred_routing_l = null;
	}
	
	/**
	 * @return true if serial port is connected
	 */
	public boolean isSerialConnected() {
		return (this.port != null) && this.port.isOpen();
	}
	
	/**
	 * Routes data packet to logger
	 * @param logger
	 */
	public void routePacketsTo(DataLogger logger) {
		if(this.packet_monitor != null) {
			this.packet_monitor.stop();
			this.packet_monitor = null;
		}
		
		if(logger == null) {
			this.deferred_routing = false;
			return;
		}
		
		this.deferred_routing_l = logger;
		if(!this.isSerialConnected()) {
			this.deferred_routing = true;
			return;
		}
		
		this.packet_monitor = new PacketMonitor(this, logger);
		new Thread(this.packet_monitor).start();
	}
	
	/**
	 * Direct I/O
	 */
	public InputStream getInputStream() {
		return this.in;
	}
	
	/**
	 * Direct I/O
	 */
	public OutputStream getOutputStream() {
		return this.out;
	}
	
	@Override
	public boolean serialConnect(String port, int baud) {
		if(this.port != null)
			this.port.closePort();
		
		this.port = SerialPort.getCommPort(port);
		if(this.port == null)
			return false;
		
		if(!this.port.openPort()) {
			this.port = null;
			return false;
		}
		
		if(!serialChangeBaud(baud)) {
			this.port.closePort();
			this.port = null;
			return false;
		}
		
		this.conn_monitor = new ConnectionMonitor(this);
		new Thread(this.conn_monitor).start();
		
		this.in = this.port.getInputStream();
		this.out = this.port.getOutputStream();
		
		if(this.deferred_routing) {
			this.deferred_routing = false;
			this.routePacketsTo(this.deferred_routing_l);
		}
		
		return true;
	}

	@Override
	public void serialDisconnect() {
		if(this.port == null)
			return;
		
		boolean save = this.deferred_routing || this.packet_monitor != null;
		this.routePacketsTo(null);
		this.deferred_routing = save;
		
		try {
			this.in.close();
			this.out.close();
		} catch (Exception ignored) { }
		
		this.conn_monitor.stop();
		this.conn_monitor = null;
		this.port.closePort();
		this.port = null;
	}

	@Override
	public boolean serialChangeBaud(int baud) {
		if(this.port == null)
			return false;
		
		if(!this.port.setBaudRate(baud)) {
			JOptionPane.showMessageDialog(AppWindow.getIstance(),
					String.format("Error: serial port %s does not support baudrate value %i", port.getSystemPortPath(), baud));
			return false;
		}
		return true;
	}

	@Override
	public String[] getAvailableSerialPorts() {
		ArrayList<String> list = new ArrayList<String>();
		for(SerialPort p : SerialPort.getCommPorts())
			list.add(p.getSystemPortPath());
			
		return list.toArray(new String[list.size()]);
	}

	@Override
	public int[] getSupportedBaudRates() {
		return new int []{9600, 19200, 38400, 57600, 115200, 230400, 460800, 500000, 576000, 921600};
	}
	
	public boolean enterWifiConfig() {
		if(!this.isSerialConnected())
			return false;
		
		if(this.wifi_setup_mode)
			return true;
		
		synchronized(this.port){
			try {
				this.readAllAvailable();
				
				this.out.write(3); // Send CTRL+C
				try { Thread.sleep(100); } catch (InterruptedException ignored) { }

				String s = this.readAllAvailable();
								
				if(!s.endsWith("Command : "))
					return false;
				
			} catch (IOException e) { 
				return false;
			}
			this.wifi_setup_mode = true;
		}
		return true;
	}
	
	public boolean exitWifiConfig() {
		synchronized(this.port){
			if(!this.wifi_setup_mode)
				return false;
			
			try {
				this.readAllAvailable();
				
				this.out.write(3); // Send CTRL+C
				try { Thread.sleep(100); } catch (InterruptedException ignored) { }
				
				String s = this.readAllAvailable();
				
				if(!s.trim().contains(">>> WiFi command mode exited <<<")) {
					JOptionPane.showMessageDialog(AppWindow.getIstance(),
							String.format("Critical error: exiting wifi setup mode not behaved as expected.\nSerial replied:\n%s", s));
					return false;
				}
			} catch (Exception ignore) { }
			
			this.wifi_setup_mode = false;
			return true;
		}
	}
	
	private class ConnectionMonitor implements Runnable {

		private SerialService service;
		private boolean run;
		
		private ConnectionMonitor(SerialService service) {
			this.service = service;
			this.run = true;
		}
		
		private void stop() {
			this.run = false;
		}
		
		@Override
		public void run() {
			while(this.run) {
				final String path = this.service.port.getSystemPortPath();
				
				if(!new File(path).exists()) {
					this.service.serialDisconnect();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							AppWindow.getIstance().getSerial().setConnected(false);
							JOptionPane.showMessageDialog(AppWindow.getIstance(),
									String.format("Error: serial port %s has been disconnected", path));
						}
					});
				}
				
				try { Thread.sleep(1000); } catch (InterruptedException ignored) { }
			}
		}
		
	}
	
	private String readAllAvailable() throws IOException {
		StringBuilder s = new StringBuilder(3000);
		while(this.in.available() > 0)
			s.append((char) in.read());
		return s.toString();
	}
	
	private class PacketMonitor implements Runnable {
		
		private SerialService service;
		private DataLogger logger;
		private boolean run;
		
		private PacketMonitor(SerialService service, DataLogger logger) {
			this.service = service;
			this.logger = logger;
			this.run = true;
		}
		
		private void stop() {
			this.run = false;
		}
		
		@Override
		public void run() {
			try {
				StringBuffer s = new StringBuffer(300);
				while(this.run) {				
					synchronized (this.service.port) {
						while(!this.service.wifi_setup_mode && this.service.in.available() > 0) {
							int c = this.service.in.read();
							if(c == '\n') {
								this.logger.logLine(s.toString());
								s = new StringBuffer(300);
							} else {
								s.append((char)c);
							}
						}

					}
					Thread.sleep(100);
				}
			} catch (InterruptedException | IOException ignored) { }
		}
		
	}

}
