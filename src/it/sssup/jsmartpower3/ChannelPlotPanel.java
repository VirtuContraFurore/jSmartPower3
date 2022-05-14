package it.sssup.jsmartpower3;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

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
	public static class ChannelPlot extends JPanel {
		
		private JLabel ch_name;
		private JLabel voltage;
		private JLabel current;
		private JLabel power;
		private XYChart chart;
		// private JPanel ch_option //contains some radio buttons
		
		// TODO add option panel to select plotting []V []I []P
		// and set window interval
		
		private static final Font def = new Font(Font.MONOSPACED, Font.PLAIN, 15);
		
		private ChannelPlot(String name) {
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
					.xAxisTitle("time [s]").yAxisTitle("Y").build();
			chart.getStyler().setChartBackgroundColor(this.getBackground());
			chart.getStyler().setZoomEnabled(true);
			chart.getStyler().setZoomResetByDoubleClick(true);
			chart.getStyler().setZoomResetByButton(true);
			chart.getStyler().setZoomSelectionColor(new Color(0,0 , 192, 128));
			this.add(new XChartPanel<XYChart>(chart));
			
			this.setVolts(0.0f);
			this.setAmps(0.0f);
			this.setWatts(0.0f);
		}
		
		// TODO: change with method do update data series
		// set channel title ?
		public XYChart getChart() {
			return chart;
		}
		
		public void setChannelName(String name) {
			ch_name.setText(name);
		}
		
		public void setVolts(float v) {
			this.voltage.setText(String.format("%2.2f Volt  ", v));
		}
		
		public void setAmps(float a) {
			this.current.setText(String.format("%2.2f Ampere", a));
		}
		
		public void setWatts(float w) {
			this.power.setText(  String.format("%2.2f Watt  ", w));
		}
		
		public JComponent add(JComponent comp) {
			comp.setAlignmentX(CENTER_ALIGNMENT);
			super.add(comp);
			return comp;
		}
		
	}


}
