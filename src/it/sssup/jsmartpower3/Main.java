package it.sssup.jsmartpower3;

public class Main {
	
	private static int SMARTPOWER3_CHANNELS = 2;

	public static void main(String[] args) {
		
		DataLogger logger = new DataLogger();

		AppWindow.getIstance().populate(SMARTPOWER3_CHANNELS);
		
		AppWindow.getIstance().getSerial().setSerialCtrlListener(logger.getSerialService());
		AppWindow.getIstance().getWifi().setWifiCtrlListener(logger.getWifiService());
		AppWindow.getIstance().getLog().setLogCtrlListener(logger);
		AppWindow.getIstance().getData().setDataCtrlListener(logger);
		AppWindow.getIstance().getChannels().setLogger(logger);
		
		/* Refresh serial port list on startup (needs to run in awt's thread) */
		java.awt.EventQueue.invokeLater(new Runnable() {
	        public void run() {
	    		AppWindow.getIstance().getSerial().forceRefresh();
	        }
	    });
	}

}
