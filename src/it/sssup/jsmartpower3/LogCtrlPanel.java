package it.sssup.jsmartpower3;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class LogCtrlPanel extends JPanel implements ActionListener{
	
	private LogCtrlListener listener;
	
	private final static int MAXLINES = 70;
	private int lines;
	
	/* Graphical elements */
	private JLabel source_lbl;
	private JLabel data_rate;
	private JButton clear;
	private JRadioButton serial;
	private JRadioButton wifi;
	private ButtonGroup bgroup;
	public JTextArea log_data;
	public JScrollPane log_scroll;
	
	public LogCtrlPanel() {
		this.listener = null;
		this.lines = 0;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = c.weighty = 1.00;
		
		/* First line */
		c.gridx = c.gridy = 0;
		c.gridwidth = 1;
		this.source_lbl = new JLabel("DATA SOURCE:");
		this.add(source_lbl, c);
		c.gridx++;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		this.serial = new JRadioButton("Serial port");
		this.serial.setHorizontalTextPosition(SwingConstants.LEFT);
		this.serial.setSelected(true);
		this.wifi = new JRadioButton("UDP over wifi");
		this.bgroup = new ButtonGroup();
		this.bgroup.add(serial);
		this.bgroup.add(wifi);
		this.add(serial, c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		this.add(wifi,c);
		
		/* Second line */
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		c.insets = new Insets(0, 5, 0, 0);
		this.data_rate = new JLabel();
		this.add(data_rate, c);
		c.insets = new Insets(0, 0, 0, 0);
		c.gridx+=c.gridwidth;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		this.clear = new JButton("Clear scroll");
		this.add(clear, c);
		
		/* Third line */
		c.gridy++;
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 5.0;
		c.gridwidth = 3;
		this.log_data = new JTextArea();
		this.log_data.setEditable(false);
		this.log_data.setColumns(130);
		this.log_data.setFont(new Font(Font.MONOSPACED, Font.PLAIN, log_data.getFont().getSize()));
		this.log_scroll = new JScrollPane(log_data);
		this.log_scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.log_scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(log_scroll, c);
		
		this.setIncomingDataRate(-1);
		
		/* Wire some logic */
		this.clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((LogCtrlPanel)((JButton) e.getSource()).getParent()).clearLog();
			}
		});
		this.serial.addActionListener(this);
		this.wifi.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		
		if(s == this.serial) {
			this.listener.logSourceChanged(LogCtrlListener.SOURCE_SERIAL);
		} else if(s == this.wifi) {
			this.listener.logSourceChanged(LogCtrlListener.SOURCE_UDP);
		}
	}
	
	/**
	 * Displays data rate
	 * @param ms measured interval between samples
	 */
	public void setIncomingDataRate(int ms) {
		if(ms < 0)
			this.data_rate.setText("Data rate: zero");
		else
			this.data_rate.setText("Data rate: every "+ms+" ms");
	}
	
	/**
	 * Add line to displayed log data, keeping carriage position
	 * @param line
	 */
	public void addLogLine(String line) {
		int hval = this.log_scroll.getHorizontalScrollBar().getValue();
		int hmax = this.log_scroll.getHorizontalScrollBar().getMaximum();
		int dmax = this.log_data.getColumns();
		
		String text = this.log_data.getText() + "\n" + line;
		if(++this.lines >= LogCtrlPanel.MAXLINES) {
			this.lines--;
			text = text.substring(text.indexOf('\n')+1);
		}
		
		this.log_data.setText(text);
		int d1 = this.log_data.getCaret().getDot() - line.length();
		this.log_data.getCaret().setDot(d1 + hval*dmax/hmax);
	}
	
	/**
	 * Clears log
	 */
	public void clearLog() {
		this.log_data.setText("");
		this.setIncomingDataRate(-1);
		this.lines = 0;
	}
	
	/**
	 * Force log source selection
	 * @param source
	 */
	public void setLogSource(int source) {
		switch(source) {
		case LogCtrlListener.SOURCE_SERIAL:
			this.serial.setSelected(true);
			break;
		case LogCtrlListener.SOURCE_UDP:
			this.wifi.setSelected(true);
			break;
		}
	}
	
	public void setLogCtrlListener(LogCtrlListener l) {
		this.listener = l;
	}
	
	public interface LogCtrlListener {
		
		public static final int SOURCE_SERIAL = 1;
		public static final int SOURCE_UDP = 2;
		
		/**
		 * User requested log source switch
		 * @param source
		 */
		public void logSourceChanged(int source);
		
	}

}
