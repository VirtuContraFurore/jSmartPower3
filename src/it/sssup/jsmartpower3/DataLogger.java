package it.sssup.jsmartpower3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.knowm.xchart.XYSeries;

import it.sssup.jsmartpower3.DataCtlrPanel.DataCtrlListener;
import it.sssup.jsmartpower3.LogCtrlPanel.LogCtrlListener;

public class DataLogger implements DataCtrlListener, LogCtrlListener{
	
	/* GUI became slow if has to handle large graphics */
	private static final int MAX_PLOT_POINTS = 5*1000;
	private static final String CSV_HEADER = "Time, V0, I0, P0, V1, I1, P1";
	
	/* Logged data*/
	private ArrayList<Date> time;
	private ArrayList<Float> ch0_v;
	private ArrayList<Float> ch0_a;
	private ArrayList<Float> ch0_w;
	private ArrayList<Float> ch1_v;
	private ArrayList<Float> ch1_a;
	private ArrayList<Float> ch1_w;
	
	/* Misc */ 
	private SerialService serial;
	private WifiService wifi;
	private long ptime, gtime;
	private final int DATA_RATE_AVG = 20;
	private long rec_times[];
	private long time_adjust;
	private File log_file;
	private boolean isLogging;
	private String outdir;
	private BufferedWriter writer;
	private String pattern;
	private SimpleDateFormat simpleDateFormat;
	
	public DataLogger() {
		this.time = new ArrayList<Date>();
		this.ch0_v = new ArrayList<Float>();
		this.ch0_a = new ArrayList<Float>();
		this.ch0_w = new ArrayList<Float>();
		this.ch1_v = new ArrayList<Float>();
		this.ch1_a = new ArrayList<Float>();
		this.ch1_w = new ArrayList<Float>();
		this.gtime = 0;
		this.ptime = 0;
		this.rec_times = new long[this.DATA_RATE_AVG];
		this.time_adjust = 0;
		this.isLogging = false;
		this.log_file = null;
		this.outdir = System.getProperty("user.home");
		this.pattern = "yyyy-MM-dd HH:mm:ss.SSS";
		this.simpleDateFormat = new SimpleDateFormat(pattern);

		this.serial = new SerialService();
		this.wifi = new WifiService(this.serial);
		
		/* Set default log source */
		this.logSourceChanged(LogCtrlListener.SOURCE_SERIAL);
	}
	
	/**
	 * Log a new line
	 * @param line
	 */
	public void logLine(String line) {
		line = line.trim();
		if(line.length() != PacketFieldExtractor.PACKET_LENGHT) /* avoid truncated lines */
			return;
		
		// display raw line
		this.displayLine(line);
		
		// unwrap and save incoming data
		final PacketFieldExtractor packet = new PacketFieldExtractor(line);
		this.time.add(this.timeConversion(packet.time_ms));
		this.ch0_v.add(packet.ch0.volt_mV/1000.0f);
		this.ch0_a.add(packet.ch0.ampere_mA/1000.0f);
		this.ch0_w.add(packet.ch0.watt_mW/1000.0f);
		this.ch1_v.add(packet.ch1.volt_mV/1000.0f);
		this.ch1_a.add(packet.ch1.ampere_mA/1000.0f);
		this.ch1_w.add(packet.ch1.watt_mW/1000.0f);
		
		//save memory, reduce plot
		if(this.time.size() > DataLogger.MAX_PLOT_POINTS) {
			this.time.remove(0);
			this.ch0_v.remove(0);
			this.ch0_a.remove(0);
			this.ch0_w.remove(0);
			this.ch1_v.remove(0);
			this.ch1_a.remove(0);
			this.ch1_w.remove(0);
		}
		
		// update graph if necessary
		long time = System.currentTimeMillis();
		if((time - this.gtime) > AppWindow.getIstance().getChannels().getRefreshMs()) {
			updateGraphs(packet);
			this.gtime = time;
		}
		
		// log to file if enabled
		if(this.isLogging) {
			synchronized (this) {
				if(this.writer != null) {
					String s = simpleDateFormat.format(this.time.get(this.time.size()-1));
					s += String.format(",%2.3f,%2.3f,%2.3f,%2.3f,%2.3f,%2.3f\n",
							packet.ch0.volt_mV/1000.0f, packet.ch0.ampere_mA/1000.0f, packet.ch0.watt_mW/1000.0f,
							packet.ch1.volt_mV/1000.0f, packet.ch1.ampere_mA/1000.0f, packet.ch1.watt_mW/1000.0f);
					try {
						writer.append(s);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
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
	
	/**
	 * @param path CSV file to be created
	 * @return true if creation successful
	 */
	private boolean createNewFile(String path) {
		this.log_file = new File(path);
		try {
			this.log_file.createNewFile();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(this.log_file));
			writer.append(DataLogger.CSV_HEADER+"\n");
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	public String createNewFile() {
		if(this.isLogging)
			return null;
		
		String pattern = "yyyyMMdd'_'HHmmss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String fdate = simpleDateFormat.format(new Date());
		if(this.createNewFile(this.outdir+"/"+fdate+".csv"))
			return this.log_file.getAbsolutePath();
		else
			return null;
	}

	@Override
	public boolean clearFile() {
		if(this.isLogging)
			return false;
		
		if(this.log_file == null)
			return false;
		
		this.log_file.delete();
		return createNewFile(this.log_file.getAbsolutePath());
	}

	@Override
	public String startLogging() {
		if(this.isLogging)
			return null;
			
		if(this.log_file == null) {
			if(this.createNewFile()==null)
				return null;
		}
		
		synchronized(this) {
			try {
				this.writer = new BufferedWriter(new FileWriter(this.log_file, true));
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		this.isLogging = true;
		return this.log_file.getAbsolutePath();
	}

	@Override
	public boolean stopLogging() {
		if(!this.isLogging)
			return false;
		
		this.isLogging = false;
		
		synchronized(this) {
			try {
				this.writer.flush();
				this.writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			this.writer = null;
		}
		
		return true;
	}

	@Override
	public boolean outputDirectoryChanged(String dir_path) {
		if(new File(dir_path).isDirectory()) {
			this.outdir = dir_path;
			return true;
		}
		return false;
	}

	@Override
	public boolean importNewFile(String file_path) {
		if(this.isLogging)
			return false;
		
		File f = new File(file_path);
		
		if(!f.exists())
			return false;
		
		try {
			BufferedReader r = new BufferedReader(new FileReader(f));

			String l = r.readLine();
			
			if(!l.equals(DataLogger.CSV_HEADER)) {
				/* wrong header */
				JOptionPane.showMessageDialog(AppWindow.getIstance(),
						"Error importing file "+file_path+"\nExpecting header: "+DataLogger.CSV_HEADER+"\nGot: "+l);
				r.close();
				return false;
			}
			
			this.time = new ArrayList<Date>();
			this.ch0_v = new ArrayList<Float>();
			this.ch0_a = new ArrayList<Float>();
			this.ch0_w = new ArrayList<Float>();
			this.ch1_v = new ArrayList<Float>();
			this.ch1_a = new ArrayList<Float>();
			this.ch1_w = new ArrayList<Float>();
			
			while((l = r.readLine()) != null) {
				String[] tok = l.split(",");
				if(tok.length != 7)
					break;
				
				this.time.add(this.simpleDateFormat.parse(tok[0]));
				this.ch0_v.add(Float.parseFloat(tok[1]));
				this.ch0_a.add(Float.parseFloat(tok[2]));
				this.ch0_w.add(Float.parseFloat(tok[3]));
				this.ch1_v.add(Float.parseFloat(tok[4]));
				this.ch1_a.add(Float.parseFloat(tok[5]));
				this.ch1_w.add(Float.parseFloat(tok[6]));
			}
			
			r.close();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			return false;
		}
		
		this.updateGraphs(null);
		
		return true;
	}
	
	@Override
	public String cropFile() {
		if(this.isLogging) {
			JOptionPane.showMessageDialog(AppWindow.getIstance(), "You can't crop while logging. Please stop logging first.");
			return null;
		}
		
		if(AppWindow.getIstance().getChannels().getChannel(0).isRemoved() == AppWindow.getIstance().getChannels().getChannel(1).isRemoved()) {
			JOptionPane.showMessageDialog(AppWindow.getIstance(), "You can crop only while displaying the channel zoomed in.\nUse show combo box to show only one channel. ");
			return null;
		}
		
		for(int i = 0; i < 2; i++) {
			if(AppWindow.getIstance().getChannels().getChannel(i).isRemoved())
				continue;
			if(!AppWindow.getIstance().getChannels().getChannel(i).isHolding()) {
				JOptionPane.showMessageDialog(AppWindow.getIstance(), "Selected channel must be on hold! Click hold button and zoom to your desired section.\nTip: zoom by dragging mouse over graph.");
				return null;
			}
		}
		
		long min = (long) Long.MIN_VALUE;
		long max = (long) Long.MAX_VALUE;
		
		for(int i = 0; i < 2; i++) {
			if(AppWindow.getIstance().getChannels().getChannel(i).isRemoved())
				continue;
			
			Map<String, XYSeries> map = AppWindow.getIstance().getChannels().getChannel(i).getChart().getSeriesMap();
			if(map.size() < 1) {
				JOptionPane.showMessageDialog(AppWindow.getIstance(), "Selected channel has no enabled trace. Please enable at least one trace!");
				return null;
			}
			for(XYSeries s : map.values()) {
				min = (long) s.getXMin();
				max = (long) s.getXMax();
				break; // all series has the same bounds
			}
		}
		
		ArrayList<Date> time = new ArrayList<Date>();
		ArrayList<Float> ch0_v = new ArrayList<Float>();
		ArrayList<Float> ch0_a = new ArrayList<Float>();
		ArrayList<Float> ch0_w = new ArrayList<Float>();
		ArrayList<Float> ch1_v = new ArrayList<Float>();
		ArrayList<Float> ch1_a = new ArrayList<Float>();
		ArrayList<Float> ch1_w = new ArrayList<Float>();
		
		for(int i = 0; i < this.time.size(); i++) {
			if(this.time.get(i).getTime() >= min && this.time.get(i).getTime() <= max) {
				time.add(this.time.get(i));
				ch0_v.add(this.ch0_v.get(i));
				ch0_a.add(this.ch0_a.get(i));
				ch0_w.add(this.ch0_w.get(i));
				ch1_v.add(this.ch1_v.get(i));
				ch1_a.add(this.ch1_a.get(i));
				ch1_w.add(this.ch1_w.get(i));
			}
		}
		
		String pattern = "yyyyMMdd'_'HHmmss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String fdate = simpleDateFormat.format(new Date());
		File log_file = new File(this.outdir+"/"+fdate+"_crop.csv");

		try {
			log_file.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(log_file));
			writer.append(DataLogger.CSV_HEADER+"\n");
			for(int i = 0; i < time.size(); i++) {
				String s = this.simpleDateFormat.format(time.get(i));
				s += String.format(",%2.3f,%2.3f,%2.3f,%2.3f,%2.3f,%2.3f\n",
						ch0_v.get(i), ch0_a.get(i), ch0_w.get(i),
						ch1_v.get(i), ch1_a.get(i), ch1_w.get(i));
				writer.append(s);
			}
			writer.flush();
			writer.close();
		} catch(IOException e) { e.printStackTrace(); }
		
		return log_file.getAbsolutePath();
	}
	
	/**
	 * Force updating graphs
	 */
	public void updateGraphs(final PacketFieldExtractor packet) {
		AppWindow.getIstance().getChannels().getChannel(0).setDataSeries(this.getTime(), this.getCh0_v(), this.getCh0_a(), this.getCh0_w());
		AppWindow.getIstance().getChannels().getChannel(1).setDataSeries(this.getTime(), this.getCh1_v(), this.getCh1_a(), this.getCh1_w());
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				if(packet != null) {
					AppWindow.getIstance().getChannels().getChannel(0).setVolts(packet.ch0.volt_mV/1000.0f);
					AppWindow.getIstance().getChannels().getChannel(0).setAmps(packet.ch0.ampere_mA/1000.0f);
					AppWindow.getIstance().getChannels().getChannel(0).setWatts(packet.ch0.watt_mW/1000.0f);
					AppWindow.getIstance().getChannels().getChannel(1).setVolts(packet.ch1.volt_mV/1000.0f);
					AppWindow.getIstance().getChannels().getChannel(1).setAmps(packet.ch1.ampere_mA/1000.0f);
					AppWindow.getIstance().getChannels().getChannel(1).setWatts(packet.ch1.watt_mW/1000.0f);
				}
				
				AppWindow.getIstance().getChannels().getChannel(0).repaintChart();				
				AppWindow.getIstance().getChannels().getChannel(1).repaintChart();
			}
			
		});
	}
	
	private Date timeConversion(long time_ms) {
		if(this.time_adjust == 0) {
			this.time_adjust = System.currentTimeMillis() - time_ms;
		}
		return new Date(time_ms + this.time_adjust);
	}

	private void displayLine(final String line) {
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
	
	public ArrayList<Date> getTime() {
		return time;
	}

	public ArrayList<Float> getCh0_v() {
		return ch0_v;
	}

	public ArrayList<Float> getCh0_a() {
		return ch0_a;
	}

	public ArrayList<Float> getCh0_w() {
		return ch0_w;
	}

	public ArrayList<Float> getCh1_v() {
		return ch1_v;
	}

	public ArrayList<Float> getCh1_a() {
		return ch1_a;
	}

	public ArrayList<Float> getCh1_w() {
		return ch1_w;
	}

	/**
	 * Class for extract fields from packet
	 */
	private class PacketFieldExtractor {
		
		public static final int PACKET_LENGHT = 79;
		
		private long time_ms;
		@SuppressWarnings("unused")
		private PowerInput power;
		private ChannelOutput ch0, ch1;
		@SuppressWarnings("unused")
		private int checksum8_2c, checksum8_xor;
		
		private PacketFieldExtractor(String packet) {
			String fields[] = packet.trim().split(",");
			if(fields.length != 17) 
				System.err.println("Received packet that cannot be parsed as expected.\nPacket data:"+packet);
			this.time_ms = Long.parseLong(fields[0]);
			this.power = new PowerInput(
					Integer.parseInt(fields[1]),
					Integer.parseInt(fields[2]),
					Integer.parseInt(fields[3]),
					Integer.parseInt(fields[4])
					);
			this.ch0 = new ChannelOutput(
					Integer.parseInt(fields[5]),
					Integer.parseInt(fields[6]),
					Integer.parseInt(fields[7]),
					Integer.parseInt(fields[8]),
					Integer.parseInt(fields[9], 16)
					);
			this.ch1 = new ChannelOutput(
					Integer.parseInt(fields[10]),
					Integer.parseInt(fields[11]),
					Integer.parseInt(fields[12]),
					Integer.parseInt(fields[13]),
					Integer.parseInt(fields[14], 16)
					);
			this.checksum8_2c = Integer.parseInt(fields[15], 16);
			this.checksum8_xor = Integer.parseInt(fields[16], 16);
		}
		
		private class PowerInput {
			
			@SuppressWarnings("unused")
			private int volt_mV, ampere_mA, watt_mW, on;

			public PowerInput(int volt_mV, int ampere_mA, int watt_mW, int on) {
				super();
				this.volt_mV = volt_mV;
				this.ampere_mA = ampere_mA;
				this.watt_mW = watt_mW;
				this.on = on;
			}

		}
		
		private class ChannelOutput {
			
			@SuppressWarnings("unused")
			private int volt_mV, ampere_mA, watt_mW, on, interrupts; 

			public ChannelOutput(int volt_mV, int ampere_mA, int watt_mW, int on, int interrupts) {
				super();
				this.volt_mV = volt_mV;
				this.ampere_mA = ampere_mA;
				this.watt_mW = watt_mW;
				this.on = on;
				this.interrupts = interrupts;
			}

		}
		
	}
	
}
