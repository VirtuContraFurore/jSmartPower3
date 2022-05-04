package it.sssup.jsmartpower3.tests;

import java.io.IOException;
import java.io.InputStream;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class TestSerialComm {

	public static void main(String[] args) {
		SerialPort ports[] = SerialPort.getCommPorts();
				
		System.out.println("Found "+ports.length+" ports");
		for(SerialPort p : ports) {
			System.out.println(p.getPortDescription());
			System.out.println(p.getPortLocation());
			System.out.println(p.getDescriptivePortName());
			System.out.println(p.getSystemPortName());
			System.out.println(p.getSystemPortPath());
		}
		
		if(ports.length < 1)
			return;
		
		final SerialPort p = ports[0];
		boolean status = p.openPort();
	
		
		if(status) {
			System.out.println("Opened port "+p.getSystemPortPath());
		} else {
			System.exit(0);
		}
		
		p.setBaudRate(921600);
		
		boolean use_event_driven = true;
		
		if(use_event_driven) {
			
			p.addDataListener(new SerialPortDataListener() {
				   @Override
				   public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
				   @Override
				   public void serialEvent(SerialPortEvent event)
				   {
				      if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
				         return;
				      byte[] newData = new byte[p.bytesAvailable()];
				      @SuppressWarnings("unused")
				      int numRead = p.readBytes(newData, newData.length);
				      //System.out.println("Read " + numRead + " bytes.");
				      System.out.print(new String(newData));
				   }
			});
			
			 System.out.println("Added callback!");
			
		} else {
		
		InputStream in = p.getInputStream();
		readInputUnbuffered(in);
		
		}
		
		System.out.println("Program reached end of main");
	}
	
	public static void readInputUnbuffered(InputStream in) {
		while(true) {
			try {
				if(in.available() > 0) {
					char c = (char) in.read();
					if(c == '\n')
						System.out.println();
					else
						System.out.print(Character.toString(c));
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
