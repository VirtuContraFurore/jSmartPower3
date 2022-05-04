package it.sssup.jsmartpower3.tests;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class TestUDP {

	public static void main(String[] args) {
		try {
			int port = 6000;
		    System.out.println("Starting listening on UDP at port " + port);
		    
		    boolean use_listener = true;
		    
		    if(use_listener) {
		    	
		    	DatagramChannel channel = DatagramChannel.open();
		    	channel.configureBlocking(false);
		    	channel.socket().bind(new InetSocketAddress(port));

	    		ByteBuffer buf = ByteBuffer.allocateDirect(1000);
		    	while(true) {
		    		if(channel.receive(buf) != null) {
//		    			System.out.println(buf.position());
		    			buf.flip();
		    		    byte[] bytes = new byte[buf.remaining()];
		    		    buf.get(bytes);
			            System.out.print(new String(bytes));
		    		}
		    		
		    	}
		    	
		    	
		    } else {

				@SuppressWarnings("resource")
				DatagramSocket socket = new DatagramSocket(port);
			    byte[] buf = new byte[1000];

			    while(true) {
			    	DatagramPacket packet = new DatagramPacket(buf, buf.length);
		            socket.receive(packet);
		            System.out.print(new String(packet.getData(), packet.getOffset(), packet.getLength()));
			    }
		    
		    }
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
