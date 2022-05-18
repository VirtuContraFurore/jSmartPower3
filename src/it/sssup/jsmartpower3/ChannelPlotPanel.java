package it.sssup.jsmartpower3;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

@SuppressWarnings("serial")
public class ChannelPlotPanel extends JPanel implements ActionListener {
	
	private ChannelPlot channels[];
	
	private JComboBox<String> refresh;
	private JComboBox<String> time_scale;
	private JComboBox<String> show_ch;
	private JButton clear;
	private long refresh_ms;
	private DataLogger logger;
	
	public void setLogger(DataLogger logger) {
		this.logger = logger;
	}

	public ChannelPlotPanel(int num_channels) {
		this.channels = new ChannelPlot[num_channels];
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1.00;
		c.weighty = 0.01;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = num_channels;
		
		JPanel opt = new JPanel();
		opt.setLayout(new BoxLayout(opt, BoxLayout.X_AXIS));
		opt.add(new JLabel("Refresh "));
		this.refresh = new JComboBox<String>();
		opt.add(refresh);
		opt.add(new JLabel("  Time scale "));
		this.time_scale = new JComboBox<String>();
		opt.add(time_scale);
		opt.add(new JLabel("  Show "));
		this.show_ch = new JComboBox<String>();
		opt.add(show_ch);
		this.clear = new JButton("Clear graphs");
		opt.add(clear);
		this.add(opt, c);

		for(int i = 0; i < num_channels; i++)
			this.channels[i] = new ChannelPlot("Channel "+i);
		this.showCh(num_channels);
		
		this.refresh.addItem("1 sec");
		this.refresh.addItem("5 sec");
		this.refresh.addItem("10 sec");
		this.refresh.addItem("1 min");
		this.refresh.addItem("10 min");
		this.refresh.setSelectedIndex(1);
		this.refresh_ms = 5000;

		this.time_scale.addItem("millisec");
		this.time_scale.addItem("seconds");
		this.time_scale.addItem("minutes");
		this.time_scale.addItem("hours");
		this.time_scale.addItem("days");
		this.time_scale.setSelectedIndex(1);
		
		for(int i = 0; i < num_channels; i++)
			this.show_ch.addItem("Ch "+i);
		this.show_ch.addItem("all");
		this.show_ch.setSelectedIndex(num_channels);
		
		this.refresh.addActionListener(this);
		this.time_scale.addActionListener(this);
		this.show_ch.addActionListener(this);
		this.clear.addActionListener(this);
	}

	public int getChannelCount() {
		return this.channels.length;
	}
	
	private void showCh(int ch) {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.00;
		c.weighty = 1.00;
		c.gridy = 1;
		c.gridx = 0;
		
		for(ChannelPlot p : channels)
			if(!p.isRemoved()) {
				this.remove(p);
				p.setRemoved(true);
			}
		
		if(ch < this.channels.length) {
			c.gridwidth = this.channels.length;
			this.add(this.channels[ch], c);
			this.channels[ch].setRemoved(false);
			
		} else {
			c.gridwidth = 1;
			for(int i = 0; i < this.channels.length; i++) {
				c.gridx = i;
				this.add(channels[i], c);
				channels[i].setRemoved(false);
			}
		}
		
		this.revalidate();
		this.repaint();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		
		if(s == this.refresh) {
			switch((String) this.refresh.getSelectedItem()){
				case "1 sec":
					this.refresh_ms = 1000;
					break;
				case "5 sec":
					this.refresh_ms = 5000;
					break;
				case "10 sec":
					this.refresh_ms = 10000;
					break;
				case "1 min":
					this.refresh_ms = 60000;
					break;
				case "10 min":
					this.refresh_ms = 600000;
					break;
				default:
					this.refresh_ms = 5000;
			}
		} else if(s == this.time_scale) {
			long t;
			switch((String) this.time_scale.getSelectedItem()){
				case "millisec":
					t = 1;
					break;
				case "seconds":
					t = 500; // 2 points per second
					break;
				case "minutes":
					t = 20*1000; // 3 points per minute
					break;
				case "hours":
					t = 6 * 60000; // 10 points per hour
					break;
				case "days":
					t = 3600*500; // 48 points per day
					break;
				default:
					t = 20*1000;
					
			}
			this.channels[0].setTimeStepMs(t);
			this.channels[1].setTimeStepMs(t);
		} else if(s == this.show_ch) {
			this.showCh(this.show_ch.getSelectedIndex());
		} else if(s == this.clear) {
			this.logger.clearGraphs();
		}
	}
	
	public long getRefreshMs() {
		return this.refresh_ms;
	}
	
	/**
	 * Return the channel associated with channel number
	 * @param index start from 0 to channel_count - 1
	 * @return the channel plot object
	 */
	public ChannelPlot getChannel(int index) {
		return channels[index];
	}
	
	/**
	 * This class is responsible for channel's data presentations.
	 * You should periodically updated the status using V,I,W setters and graph data.
	 * Every interaction must be wrapped inside swing's invokeLater(..) as it directly 
	 * manipulates swing objects.
	 */
	public static class ChannelPlot extends JPanel implements ActionListener {
		
		private JLabel ch_name;
		private JLabel voltage;
		private JLabel current;
		private JLabel power;
		private JCheckBox plot_v;
		private JCheckBox plot_i;
		private JCheckBox plot_p;
		private JButton hold;
		private XYChart chart;
		private XChartPanel<XYChart> ws;
		
		private List<Date> t, t_;
		private List<Float> v, v_;
		private List<Float> i, i_;
		private List<Float> p, p_;
		private boolean isHolding;
		private boolean isRemoved;
		private boolean forceRedrawTimescale;
		private long time_step_ms;
		private int markers;

		private static final Font def = new Font(Font.MONOSPACED, Font.PLAIN, 15);
		
		private ChannelPlot(String name) {
			this.isRemoved = true;
			this.isHolding = false;
			this.forceRedrawTimescale = false;
			this.time_step_ms = 500;
			this.markers = 0;
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			ch_name = new JLabel(name);
			ch_name.setFont(new Font(Font.MONOSPACED, Font.PLAIN, def.getSize() + 5));
			this.add(ch_name);
			
			voltage = new JLabel();
			voltage.setFont(def);
			this.add(voltage);
			
			current = new JLabel();
			current.setFont(def);
			this.add(current);
			
			power = new JLabel();
			power.setFont(def);
			this.add(power);
			
			chart = new XYChartBuilder().width(600).height(400).title("")
					.xAxisTitle("time").yAxisTitle("Y").build();
			chart.getStyler().setChartBackgroundColor(this.getBackground());
			chart.getStyler().setDatePattern("yyyy-MM-dd HH:mm:ss.SSS");
			chart.getStyler().setYAxisTicksVisible(true);
			chart.getStyler().setZoomEnabled(true);
			chart.getStyler().setZoomResetByDoubleClick(true);
			chart.getStyler().setZoomResetByButton(true);
			chart.getStyler().setZoomSelectionColor(new Color(0,0 , 192, 128));
			chart.getStyler().setyAxisTickLabelsFormattingFunction(new Function<Double, String>() {
				
				@Override
				public String apply(Double t) {
					return String.format("%2.3f", t);
				}
				
			});
			ws = new XChartPanel<XYChart>(chart);
			this.add(ws);
			
			JPanel checkbox = new JPanel();
			checkbox.setLayout(new BoxLayout(checkbox, BoxLayout.X_AXIS));
			this.plot_v = new JCheckBox("Voltage");
			this.plot_i = new JCheckBox("Current");
			this.plot_p = new JCheckBox("Power");
			checkbox.add(new JLabel("Plot:"));
			checkbox.add(plot_v);
			checkbox.add(plot_i);
			checkbox.add(plot_p);
			this.add(checkbox);
			
			this.hold = new JButton("Toggle hold [OFF]");
			checkbox.add(hold);
			
			// Default selection:
			this.plot_v.setSelected(true);
			this.plot_i.setSelected(true);
			
			this.plot_v.addActionListener(this);
			this.plot_i.addActionListener(this);
			this.plot_p.addActionListener(this);
			this.hold.addActionListener(this);
			
			this.setVolts(0.0f);
			this.setAmps(0.0f);
			this.setWatts(0.0f);
		}
		
		public boolean isRemoved() {
			return isRemoved;
		}

		public void setRemoved(boolean isRemoved) {
			this.isRemoved = isRemoved;
		}

		public void setTimeStepMs(long step) {
			if(this.time_step_ms != step) {
				this.time_step_ms = step;
				timeScaleUpate();
				this.forceRedrawTimescale = true;
				repaintChart();
				this.forceRedrawTimescale = false;
			}
		}
		
		/**
		 * Only for advanced usage
		 */
		public XYChart getChart() {
			return chart;
		}
		
		/**
		 * Update serie's lists
		 */
		public void setDataSeries(List<Date> t, List<Float> v, List<Float> i, List<Float> p) {
			if(this.isHolding)
				return;
			
			this.t_ = t.stream().collect(Collectors.toList());
			this.v_ = v.stream().collect(Collectors.toList());
			this.i_ = i.stream().collect(Collectors.toList());
			this.p_ = p.stream().collect(Collectors.toList());
			
			this.timeScaleUpate();
		}
		
		private void timeScaleUpate() {
			if(t_ == null || v_ == null || i_ == null || p_  == null)
				return;
			
			long last = -1000000000;
			
			this.t = new ArrayList<Date>();
			this.v = new ArrayList<Float>();
			this.i = new ArrayList<Float>();
			this.p = new ArrayList<Float>();
			
			for(int idx = 0; idx < t_.size(); idx++) {
				Date d = t_.get(idx);
				long time = d.getTime();
				if(time - last >= this.time_step_ms) {
					last = time;
					this.t.add(d);
					this.v.add(v_.get(idx));
					this.i.add(i_.get(idx));
					this.p.add(p_.get(idx));
				}
			}
		}
		
		/**
		 * Repaint chart with data provided using updateDataSeries(..)
		 */
		public void repaintChart() {
			if(this.isRemoved() || (this.isHolding && !this.forceRedrawTimescale))
				return;
			
			Map<String, XYSeries> map = this.chart.getSeriesMap();

			@SuppressWarnings("unchecked")
			final List<Float>[] list = new List[] {this.v, this.i, this.p};
			final String[] k = new String[] {"V", "I", "P"};
			final String[] desc = new String[] {"Voltage [V]", "Current [A]", "Power [W]"};
			final Color[] color = new Color[] {Color.RED, Color.YELLOW, Color.GREEN};
			final JCheckBox[] enable = new JCheckBox[] {this.plot_v, this.plot_i, this.plot_p};
			
			for(int i = 0; i < k.length; i++) {
				if(enable[i].isSelected() && list[i] != null && list[i].size() > 0) {
					if(map.containsKey(k[i])) {
						if(this.forceRedrawTimescale) { /* Preserve zooming */
							double[] bounds = new double[] {map.get(k[i]).getXMin(), map.get(k[i]).getXMax()};
							this.chart.updateXYSeries(k[i], this.t, list[i], null);
							map.get(k[i]).filterXByValue(bounds[0], bounds[1]);
						} else {
							this.chart.updateXYSeries(k[i], this.t, list[i], null);
						}
					} else {
						this.chart.addSeries(k[i], this.t, list[i], null);
						map.get(k[i]).setYAxisGroup(i);
						this.chart.setYAxisGroupTitle(i, desc[i]);
						map.get(k[i]).setMarker(SeriesMarkers.NONE);
						map.get(k[i]).setLineColor(color[i]);
					}
				} else if(map.containsKey(k[i])) {
					this.chart.removeSeries(k[i]);
				}
			}

			ws.invalidate();
			ws.repaint();
		}
		
		public void setChannelName(String name) {
			ch_name.setText(name);
		}
		
		public void setVolts(float v) {
			this.voltage.setText(String.format("%2.3f Volt  ", v));
		}
		
		public void setAmps(float a) {
			this.current.setText(String.format("%2.3f Ampere", a));
		}
		
		public void setWatts(float w) {
			this.power.setText(  String.format("%2.3f Watt  ", w));
		}
		
		public JComponent add(JComponent comp) {
			comp.setAlignmentX(CENTER_ALIGNMENT);
			super.add(comp);
			return comp;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object s = e.getSource();
			
			if(s == this.plot_v || s == plot_i || s == plot_p) {
				this.forceRedrawTimescale = true;
				repaintChart();
				this.forceRedrawTimescale = false;
			} else if(s == this.hold) {
				this.isHolding = !this.isHolding;
								
				if(this.isHolding)
					this.hold.setText("Toggle hold [ON] ");
				else
					this.hold.setText("Toggle hold [OFF]");
			}
		}
		
		public void addMarker(Date date, Color color, boolean start) {
			String s = (start ? "START" : "STOP") + this.markers++/2;
			this.getChart().addSeries(s, Arrays.asList(new Date[] {date, date}), Arrays.asList(new Float[] {0.00f, 1.0f}), null);
			this.getChart().getSeriesMap().get(s).setLineColor(color);
			this.repaintChart();
		}
		
		public void removeMarkers() {
			while(this.markers > 0) {
				for(String s : this.getChart().getSeriesMap().keySet()) {
					if(s.startsWith("ST")) {
						this.getChart().removeSeries(s);
						this.markers--;
						break;
					}
				}
			}
			this.repaintChart();
		}

		public boolean isHolding() {
			return this.isHolding;
		}
		
	}

}
