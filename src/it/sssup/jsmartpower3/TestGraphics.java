package it.sssup.jsmartpower3;

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

public class TestGraphics {

	public static void main(String[] args) {
		System.out.println("Hello world");
		
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
