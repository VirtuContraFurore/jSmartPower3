package it.sssup.jsmartpower3;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class DataCtlrPanel extends JPanel implements ActionListener {
	
	private String logfile_path; /* Stored here to display later in the helper label */
	private DataCtrlListener listener;
	
	/* Graphical elements */
	private JLabel outdirLabel;
	private JLabel helper;
	private JButton outdirBrowse;
	private JButton captureStart;
	private JButton captureStop;
	private JButton fileImport;
	private JButton fileClear;
	private JButton fileNew;
	private JButton fileCrop;
	private JTextField outdirField;
	
	public DataCtlrPanel() {
		this.logfile_path = null; /* no file opened */
		this.listener = null;
		
		this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
		
		/* First lay down each component */
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 1.0f;
		c.weighty = 1.0f;
		
		/* First line */
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.gridx = c.gridy = 0;
		this.outdirLabel = new JLabel("Selected output directory: ");
		this.add(outdirLabel, c);
	
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx++;
		this.outdirField = new JTextField(20);
		this.outdirField.setText("~/");
		this.add(outdirField, c);

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.gridx++;
		this.outdirBrowse = new JButton("Browse..");
		this.add(outdirBrowse, c);
		
		/* Second line*/
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy++;
		this.captureStart = new JButton("Start capture");
		this.add(captureStart, c);
		
		c.gridx++;
		this.fileNew = new JButton("New log file");
		this.add(fileNew, c);

		c.gridx++;
		this.fileImport = new JButton("Import log file");
		this.add(fileImport, c);
		
		/* Third line */
		c.gridx = 0;
		c.gridy++;
		this.captureStop = new JButton("Stop capture");
		this.add(captureStop, c);
		
		c.gridx++;
		this.fileClear = new JButton("Clear log file");
		this.add(fileClear, c);

		c.gridx++;
		this.fileCrop = new JButton("Crop log file");
		this.add(fileCrop, c);
		
		/* Fourth Line */
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		c.insets = new Insets(2, 15, 0, 0);
		this.helper = new JLabel();
		this.add(helper, c);
		
		this.setHelperString(HelperStrings.NO_FILE);
		
		/* Now installs some base logic */
		this.captureStart.addActionListener(this);
		this.captureStop.addActionListener(this);
		this.fileClear.addActionListener(this);
		this.fileCrop.addActionListener(this);
		this.fileImport.addActionListener(this);
		this.fileNew.addActionListener(this);
		this.outdirBrowse.addActionListener(this);
		this.outdirField.addActionListener(this);
	}
	
	/**
	 * Force output directory to dir_path
	 * @param dir_path forced value
	 */
	public void setOutputDir(String dir_path) {
		this.outdirField.setText(dir_path);
	}
	
	/**
	 * Locks changing directory if logging
	 * @param editable
	 */
	public void setOutputDirEditable(boolean editable) {
		this.outdirField.setEditable(editable);
	}
	
	public void setDataCtrlListener(DataCtrlListener l) {
		this.listener = l;
	}
	
	private void setHelperString(String s) {
		this.helper.setText("[INFO] " + String.format(s, this.logfile_path));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		
		if (s == this.outdirField){			
			if(this.listener.outputDirectoryChanged(e.getActionCommand()))
				this.setHelperString(HelperStrings.DIR_CHANGED);
			else
				this.setHelperString(HelperStrings.DIR_CHANGED_FAIL);
			
		} else if (s == this.outdirBrowse && this.outdirField.isEditable()) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
			    String folder = fc.getSelectedFile().getAbsolutePath();
			    if(this.listener.outputDirectoryChanged(folder))
					this.setHelperString(HelperStrings.DIR_CHANGED);
				else
					this.setHelperString(HelperStrings.DIR_CHANGED_FAIL);
			}
			
		} else if(s == this.fileNew) {
			this.logfile_path = this.listener.createNewFile();
			if(this.logfile_path != null)
				this.setHelperString(HelperStrings.FILE_CREATED);
			else
				this.setHelperString(HelperStrings.FILE_CREATED_FAIL);
			
		} else if (s == this.fileImport) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setCurrentDirectory(new File(this.outdirField.getText().trim()));
			fc.addChoosableFileFilter(new FileFilter() {
				
				@Override
				public String getDescription() {
					return "Comma Separated Value (.csv)";
				}
				
				@Override
				public boolean accept(File f) {
					return f.isFile() && f.getName().toLowerCase().endsWith(".csv");
				}
			});
			fc.setAcceptAllFileFilterUsed(true);
			int returnVal = fc.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
			    this.logfile_path = fc.getSelectedFile().getAbsolutePath();
			    if(this.listener.importNewFile(this.logfile_path))
					this.setHelperString(HelperStrings.FILE_IMPORTED);
				else
					this.setHelperString(HelperStrings.FILE_IMPORTED_FAIL);
			}
			
		} else if (s == this.fileClear) {
			if(this.listener.clearFile())
				this.setHelperString(HelperStrings.FILE_CLEARED);
			else
				this.setHelperString(HelperStrings.FILE_CLEARED_FAIL);
			
		} else if (s == this.fileCrop) {
			if(this.listener.cropFile())
				this.setHelperString(HelperStrings.FILE_CROPPED);
			else
				this.setHelperString(HelperStrings.FILE_CROPPED_FAIL);
			
		} else if (s == this.captureStart) {
			if(this.listener.startLogging())
				this.setHelperString(HelperStrings.LOG_START);
			else
				this.setHelperString(HelperStrings.LOG_START_FAIL);
			
		} else if (s == this.captureStop) {
			if(this.listener.stopLogging())
				this.setHelperString(HelperStrings.LOG_STOP);
			else
				this.setHelperString(HelperStrings.LOG_STOP_FAIL);
		}
		
	}
	
	private static class HelperStrings {
		
		public static final String NO_FILE = "No file opened. Create one pressing 'new log file' or start directly logging to a new file pressing 'start capture'"; /* At idle */
		public static final String FILE_CREATED = "Created new log file %s"; /* When new file created */
		public static final String FILE_IMPORTED = "Imported old log file %s"; /* When file imported */
		public static final String FILE_CLEARED = "The content of %s has been erased. Press start to collect new data."; /* When start logging is pressed */
		public static final String FILE_CROPPED = "Cropped file %s.";
		public static final String LOG_START = "Started logging data to %s, press stop to end."; /* When start logging is pressed */
		public static final String LOG_STOP = "Stopped logging data to %s, press start to resume."; /* When stop logging is pressed */
		public static final String DIR_CHANGED = "Output directory updated. To create there a new log file press 'new log file'.";
		public static final String FILE_CREATED_FAIL = "Creation of file %s failed.";
		public static final String FILE_IMPORTED_FAIL = "Importing file %s failed.";
		public static final String FILE_CLEARED_FAIL = "Erasing file %s failed.";
		public static final String FILE_CROPPED_FAIL = "Error cropping file %s.";
		public static final String LOG_START_FAIL = "Fail starting logging.";
		public static final String LOG_STOP_FAIL = "Fail stopping logging."; 
		public static final String DIR_CHANGED_FAIL = "Requested directory does not exists or is not writable.";
		
	}
	
	/**
	 * Inside these callbacks it is possible to modify GUI elements without using invokeLater(..)
	 */
	public interface DataCtrlListener {
		
		/**
		 * Creates a new file
		 * @return full path of created file, null if creation failed
		 */
		public String createNewFile();
		
		/**
		 * Import an existing file
		 * @param file_path path chosen by user through dialogue
		 * @return true if file opened, false if error occurred during opening
		 */
		public boolean importNewFile(String file_path);
		
		/**
		 * Erase opened file
		 * @return true if current file erased, false if error occurred
		 */
		public boolean clearFile();
		
		/**
		 * Crop opened file
		 * @return true if current file cropped, false if error occurred
		 */
		public boolean cropFile();
		
		/**
		 * Start logging to currently opened file
		 * @return true on success
		 */
		public boolean startLogging();
		
		/**
		 * Stop logging to currently opened file
		 * @return true on success
		 */
		public boolean stopLogging();
		
		/**
		 * @param dir_path User provided directory path
		 * @return true if dir exists and is writable, false otherwise
		 */
		public boolean outputDirectoryChanged(String dir_path);
		
	}
	
}
