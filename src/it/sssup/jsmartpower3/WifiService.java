package it.sssup.jsmartpower3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;import java.net.SocketTimeoutException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import it.sssup.jsmartpower3.WifiCtrlPanel.WifiCtrlListener;

public class WifiService implements WifiCtrlListener{
	
	private SerialService serial;
	private PacketMonitor packet_monitor;
	private Thread packet_monitor_t;
	private int udp_port;
	private String udp_addr;
	private boolean udp_settings_dummy;
	
	public WifiService(SerialService serial) {
		this.serial = serial;
		this.packet_monitor = null;
		this.udp_port = 0;
		this.udp_addr = "0.0.0.0";
		this.udp_settings_dummy = true;
	}
	
	public void routePacketsTo(DataLogger logger) {
		if(this.packet_monitor != null) {
			this.packet_monitor.stop();
			try {
				this.packet_monitor_t.join();
			} catch (InterruptedException ignored) { ignored.printStackTrace(); }
			this.packet_monitor = null;
			this.packet_monitor_t = null;
		}
		
		if(logger == null)
			return;
		
		this.packet_monitor = new PacketMonitor(this.getUdpPort(), logger);
		this.packet_monitor_t = new Thread(this.packet_monitor);
		this.packet_monitor_t.start();
	}
	
	/**
	 * @return currently used UDP port for communication
	 */
	public int getUdpPort() {
		return this.udp_port;
	}
	
	/**
	 * @return currently used address for routing UDP packets
	 */
	public String getUdpAddr() {
		return this.udp_addr;
	}
	
	@Override
	public String[] scanAP() {
		if(!this.serial.enterWifiConfig())
			return null;
		
		try {
			this.serial.getOutputStream().write('3');
			try { Thread.sleep(100); } catch (InterruptedException ignored) { }

			String s = "";
			while(this.serial.getInputStream().available() > 0)
				s = s + ((char) this.serial.getInputStream().read());
			
			if(!s.contains(">>> AP scanning <<<")) {
				JOptionPane.showMessageDialog(AppWindow.getIstance(), "Error, serial answered:"+s);
				this.serial.exitWifiConfig();
				return null;
			}
			
			JOptionPane.showMessageDialog(AppWindow.getIstance(), "Scanning APs is gonna take a while, please wait");
			
			while(this.serial.getInputStream().available() < 1);
			s = "";
			while(true) {
				while(this.serial.getInputStream().available() > 0)
					s = s + ((char) this.serial.getInputStream().read());
				
				/* Allow small pause in data sending */
				try { Thread.sleep(250); } catch (InterruptedException ignored) { }
				if(this.serial.getInputStream().available() < 1)
					break;
			}
			
			String lines[] = s.trim().split("\\n");
			ArrayList<String> aps = new ArrayList<String>();
			for(String l : lines) {
				l = l.trim();
				if(l.length() < 4 || !Character.isDigit(l.charAt(0)))
					continue;
				aps.add(l);
			}
			
			return aps.toArray(new String[aps.size()]);
			
		} catch (IOException ignored) { }
			
		return null;
	}

	@Override
	public boolean selectAP(String ap, String passphrase) {
		if(ap == null || passphrase == null) {
			try {
				this.serial.getOutputStream().write(3); // Send CTRL+C
			} catch (IOException ignored) { ignored.printStackTrace(); } 
			try { Thread.sleep(100); } catch (InterruptedException ignored) { }
			this.serial.exitWifiConfig();
			return false;
		}
		
		if(!Character.isDigit(ap.charAt(0))) {
			JOptionPane.showMessageDialog(AppWindow.getIstance(), "Invalid AP name: "+ap);
			this.serial.exitWifiConfig();
			return false;
		}
		
		try {
			this.serial.getOutputStream().write(ap.charAt(0));
			this.serial.getOutputStream().write(0x0D);
			try { Thread.sleep(250); } catch (InterruptedException ignored) { }
			
			String s = "";
			while(this.serial.getInputStream().available() > 0)
				s = s + ((char) this.serial.getInputStream().read());
			
			if(!s.contains("Password")) {
				JOptionPane.showMessageDialog(AppWindow.getIstance(), "Error, device answered "+s);
				this.serial.exitWifiConfig();
				return false;
			}
			
			this.serial.getOutputStream().write(passphrase.getBytes());
			this.serial.getOutputStream().write(0x0D);
			while(this.serial.getInputStream().available() < passphrase.length()); /* wait echo */
			try { Thread.sleep(250); } catch (InterruptedException ignored) { }
			
			s = "";
			while(this.serial.getInputStream().available() > 0)
				s = s + ((char) this.serial.getInputStream().read());
			
			if(!s.contains(">>> AP connecting <<<")) {
				JOptionPane.showMessageDialog(AppWindow.getIstance(), "Error, device answered "+s);
				this.serial.exitWifiConfig();
				return false;
			}
			
			JOptionPane.showMessageDialog(AppWindow.getIstance(), "Validating network connection...");
			
			s = "";
			while(true) {
				while(this.serial.getInputStream().available() > 0)
					s = s + ((char) this.serial.getInputStream().read());
				
				/* Allow small pause in data sending */
				try { Thread.sleep(1500); } catch (InterruptedException ignored) { }
				if(this.serial.getInputStream().available() < 1)
					break;
			}
			
			if(!s.contains("[[[ Connection Okay ]]]")) {
				JOptionPane.showMessageDialog(AppWindow.getIstance(), "Error, device answered "+s);
				this.serial.exitWifiConfig();
				return false;
			}
			
			JOptionPane.showMessageDialog(AppWindow.getIstance(), "Connection success!");
			
		} catch (IOException ignored) { }
		
		this.serial.exitWifiConfig();
		return true;
	}

	@Override
	public boolean changeUdpPort(int port) {
		/* if serial not connect, try only to listen to user requested port */
		if(!this.serial.isSerialConnected()) {
			this.udp_port = port;
			if(this.packet_monitor != null)
				this.packet_monitor.changePort(port);
			return true;
		}
		
		/* if serial is connected first change device setting */
		if(this.udp_settings_dummy)
			this.refreshWifiStatus();
		
		boolean success = this.setUdpParam(this.udp_addr, port);
		if(success && this.packet_monitor != null)
			this.packet_monitor.changePort(port);
		return success;
	}

	@Override
	public boolean changeUdpAddr(String addr) {
		if(this.udp_settings_dummy)
			this.refreshWifiStatus();
		
		return this.setUdpParam(addr, this.udp_port);
	}

	@Override
	public boolean refreshWifiStatus() {
		if(!this.serial.enterWifiConfig())
			return false;
		
		boolean connected = true;
		try {
			this.serial.getOutputStream().write('1');
			try { Thread.sleep(100); } catch (InterruptedException ignored) { }

			String s = "";
			while(this.serial.getInputStream().available() > 0)
				s = s + ((char) this.serial.getInputStream().read());
			
			String lines[] = s.trim().split("\\n");
			
			if (lines[1].equals(">>> AP connection info <<<")){
				connected = true;
				String ssid = lines[2].split(" ")[0].trim();
				String gain = lines[2].substring(lines[2].indexOf('(')+1, lines[2].lastIndexOf(')')-1);
				String ip = lines[3].substring(lines[3].indexOf('[')+1, lines[3].lastIndexOf(']'));
				String mac = lines[4].substring(lines[4].indexOf('[')+1, lines[4].lastIndexOf(']'));
				
				this.serial.getOutputStream().write('2');
				try { Thread.sleep(100); } catch (InterruptedException ignored) { }

				s = "";
				while(this.serial.getInputStream().available() > 0)
					s = s + ((char) this.serial.getInputStream().read());
				
				lines = s.trim().split("\\n");
				this.udp_addr = lines[2].substring(lines[2].indexOf('[')+1, lines[2].lastIndexOf(']'));
				String udp_port = lines[3].substring(lines[3].indexOf('[')+1, lines[3].lastIndexOf(']'));
				this.udp_port = Integer.parseInt(udp_port);
				
				AppWindow.getIstance().getWifi().setConnectionInfo(ssid, udp_addr, udp_port);
				AppWindow.getIstance().getWifi().setAdditionalInfo("<html>Gain: "+gain+"<br> Device IP: "+ip+"<br>Device MAC: " + mac+"</html>");
			} else {
				if(!lines[1].equals(">>> AP no connnection <<<"))
					JOptionPane.showMessageDialog(AppWindow.getIstance(),
						"Error: reply error while asking wifi status");
				connected = false;
			}
			
		} catch (IOException ignored) { }
		
		this.serial.exitWifiConfig();
		this.udp_settings_dummy = false;
		return connected;
	}
	
	public boolean setUdpParam(String addr, int port) {
		this.udp_addr = addr.trim();
		this.udp_port = port;
		String s = "";
		
		if(!this.serial.enterWifiConfig())
			return false;
		
		try {
			while(this.serial.getInputStream().available() > 0)
				this.serial.getInputStream().read();
			
			this.serial.getOutputStream().write('4');
			try { Thread.sleep(100); } catch (InterruptedException ignored) { }

			s = "";
			while(this.serial.getInputStream().available() > 0)
				s = s + ((char) this.serial.getInputStream().read());
			
			this.serial.getOutputStream().write(this.udp_addr.getBytes());
			while(this.serial.getInputStream().available() < this.udp_addr.length()); /* wait for echo */
			this.serial.getOutputStream().write(0x0D); /* Serial enter */
			try { Thread.sleep(300); } catch (InterruptedException ignored) { }

			s = "";
			while(this.serial.getInputStream().available() > 0)
				s = s + ((char) this.serial.getInputStream().read());
			
			if(!s.contains("IP address set ok")) {
				JOptionPane.showMessageDialog(AppWindow.getIstance(), "Failing settin IP addr");
				this.serial.exitWifiConfig();
				return false;
			}
			
			String udp_port_string = ((Integer) this.udp_port).toString();
			this.serial.getOutputStream().write(udp_port_string.getBytes());
			while(this.serial.getInputStream().available() < udp_port_string.length()); /* wait for echo */
			this.serial.getOutputStream().write(0x0D); /* Serial enter */
			try { Thread.sleep(300); } catch (InterruptedException ignored) { }
			
			s = "";
			while(this.serial.getInputStream().available() > 0)
				s = s + ((char) this.serial.getInputStream().read());
			
			if(!s.contains("port set ok")) {
				JOptionPane.showMessageDialog(AppWindow.getIstance(), "Failing settin UPD port");
				this.serial.exitWifiConfig();
				return false;
			}
			
		} catch (IOException ignored) { }
		
		this.serial.exitWifiConfig();
		return true;
	}
	
	private class PacketMonitor implements Runnable {
		
		private DataLogger logger;
		private boolean run;
		private DatagramSocket socket;
		private byte buf[];
		
		private PacketMonitor(int port, DataLogger logger) {
			this.logger = logger;
			this.run = true;
			try {
				this.socket = new DatagramSocket(port);
			} catch (SocketException ignored) { ignored.printStackTrace(); }
		    this.buf = new byte[1000];
		}
		
		private void stop() {
			this.run = false;
		}
		
		private void changePort(int port) {
			synchronized (this) {
				try {
					this.socket.close();
					this.socket = new DatagramSocket(port);
				} catch (SocketException ignored) { ignored.printStackTrace(); }
			}
		}
		
		@Override
		public void run() {
		while(this.run) {
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				try { Thread.sleep(5); } catch (InterruptedException ignored) { } /* Ease releasing mutex */
				synchronized (this) {
					try {
						socket.setSoTimeout(500);
						socket.receive(packet);
						this.logger.logLine(new String(packet.getData(), packet.getOffset(), packet.getLength()).trim());
					} catch (Exception ignored) { if(!(ignored instanceof SocketTimeoutException)) ignored.printStackTrace(); }
				}
			}
			this.socket.close();
		}
		
	}
	

}
