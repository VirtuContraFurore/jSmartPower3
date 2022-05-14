package it.sssup.jsmartpower3.tests;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.Styler.LegendPosition;

public class TestXchart {

	public static void main(String[] args) {
		System.out.println("Hello world");
		
		// Create Chart
		final XYChart chart = new XYChartBuilder().width(600).height(400).title("Area Chart").xAxisTitle("X").yAxisTitle("Y").build();

		// Customize Chart
		chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Area);

		// Series
		chart.addSeries("a", new double[] { 0, 3, 5, 7, 9 }, new double[] { -3, 5, 9, 6, 5 });
		chart.addSeries("b", new double[] { 0, 2, 4, 6, 9 }, new double[] { -1, 6, 4, 0, 4 });
		chart.addSeries("c", new double[] { 0, 1, 3, 8, 9 }, new double[] { -2, -1, 1, 0, 1 });
		
		chart.getStyler().setZoomEnabled(true);
		chart.getStyler().setZoomResetByDoubleClick(false);
		chart.getStyler().setZoomResetByButton(true);
		chart.getStyler().setZoomSelectionColor(new Color(0,0 , 192, 128));
		
		JFrame f = new JFrame("Multiple grid bag layout tests");
		Container pane = f.getContentPane();
		pane.setLayout(new GridBagLayout());
		
		// Constraints
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH; 
		
		// Large panel 4/5 and 3/4
	   	JPanel MyPanel1 = createPanel("MAIN WINDOW");
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.8;
		c.weighty = 0.75;
	   	pane.add(MyPanel1, c);
	   	
	    JPanel chartPanel = new XChartPanel<XYChart>(chart);
	    MyPanel1.add(chartPanel);


	   	// Large panel 4/5 and 3/4
	   	JPanel MyPanel2 = createPanel("LOGGING DATA");
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0.25;
	   	pane.add(MyPanel2, c);
	   	
	   	JPanel MyPanel3 = new JPanel();
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.weightx = 0.2;
		c.weighty = 0.0;
	   	pane.add(MyPanel3, c);
	   	
	   	
	   	MyPanel3.setLayout(new GridBagLayout());
	   	c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.gridx = 0;

		JPanel MyPanel4 = createPanel("SERIAL");
		c.gridy = 0;
		c.weighty = 0.33;
		MyPanel3.add(MyPanel4, c);
		
		JPanel MyPanel5 = createPanel("WI-FI");
		c.gridy = 1;
		c.weighty = 0.33;
		MyPanel3.add(MyPanel5, c);
		
		JPanel MyPanel6 = createPanel("DATA RECEIVED");
		c.gridy = 2;
		c.weighty = 0.33;
		MyPanel3.add(MyPanel6, c);
		
	   	f.setSize(400, 300);
	   	f.setMinimumSize(new Dimension(400, 300));
	   	f.setVisible(true);
	   	f.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent arg0) {
				// TODO Auto-generated method stub
				System.exit(0);

			}
			
			@Override
			public void windowClosed(WindowEvent arg0) {
			}
			
			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public static JPanel createPanel(String desc) {
	   	JPanel p = new JPanel();
	   	p.setBackground(Color.getHSBColor((float) Math.random(), (float) Math.random(), (float) Math.random()));
	   	p.setLayout( new BorderLayout() );
	   	JLabel l = new JLabel(desc);
	   	l.setHorizontalAlignment(JLabel.CENTER);
	   	p.add(l, BorderLayout.CENTER);
	   	LineBorder b = new LineBorder(Color.RED, 2);
	   	p.setBorder(b);
	   	return p;
	}


}
