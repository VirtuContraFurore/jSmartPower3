package it.sssup.jsmartpower3;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;

@SuppressWarnings("serial")
public class ChannelPlotPanel extends JPanel {
	
	private ChannelPlot channels[];
	
	public ChannelPlotPanel(int num_channels) {
		this.channels = new ChannelPlot[num_channels];
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0f;
		c.weighty = 1.0f;
		c.gridy = 0;

		for(int i = 0; i < num_channels; i++) {
			c.gridx = i;
			this.channels[i] = new ChannelPlot("Channel "+i);
			this.add(channels[i], c);
		}
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
		
		private List<Date> t;
		private List<Float> v;
		private List<Float> i;
		private List<Float> p;
		private boolean isHolding;
		
		private static final Font def = new Font(Font.MONOSPACED, Font.PLAIN, 15);
		
		private ChannelPlot(String name) {
			this.isHolding = false;
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
			this.t = t.stream().collect(Collectors.toList());
			this.v = v.stream().collect(Collectors.toList());
			this.i = i.stream().collect(Collectors.toList());
			this.p = p.stream().collect(Collectors.toList());
		}
		
		/**
		 * Repaint chart with data provided using updateDataSeries(..)
		 */
		public void repaintChart() {
			if(this.isHolding)
				return;
			
			Map<String, XYSeries> map = this.chart.getSeriesMap();
			
			// Remove everything
			for(String s : new String[] {"V", "I", "P"}) {
				if(map.containsKey(s))
					this.chart.removeSeries(s);
			}
			
			// Add one by one
			if(this.v != null && this.plot_v.isSelected()) 
				this.chart.addSeries("V", this.t, this.v, null);
			
			if(this.i != null && this.plot_i.isSelected()) 
				this.chart.addSeries("I", this.t, this.i, null);
			
			if(this.p != null && this.plot_p.isSelected()) 
				this.chart.addSeries("P", this.t, this.p, null);
			
			// Now set axisgroup
			map = this.chart.getSeriesMap();
			int axisgroup = 0;
			
			for(String s : new String[] {"V", "I", "P"}) {
				if(map.containsKey(s)){
					map.get(s).setYAxisGroup(axisgroup);
					String title = "";
					switch (s) {
					case "V":
						title = "Voltage [V]";
						break;
					case "I":
						title = "Current [A]";
						break;
					case "P":
						title = "Power [W]";
						break;
					}
					this.chart.setYAxisGroupTitle(axisgroup, title);
					axisgroup++;
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
				repaintChart();
			} else if(s == this.hold) {
				this.isHolding = !this.isHolding;
				
				if(this.isHolding)
					this.hold.setText("Toggle hold [ON] ");
				else
					this.hold.setText("Toggle hold [OFF]");
			}
		}
		
	}


}
