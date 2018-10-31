import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Socket extends Observable{

	private int port;
	private InetAddress localhost;
	private DatagramSocket socket;
	
	public Socket( int port ) {
		System.out.println("Socket instantiated");
		this.port = port;
		this.localhost = null;
		this.socket = null;
	
		try {
			this.localhost = InetAddress.getLocalHost();
			this.socket = new DatagramSocket(this.port, this.localhost);
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		
		Thread receiver = new Thread(()-> receiveData() );
		receiver.start();

	}
	
	
	public void send( String data, InetAddress destAdd, int destPort ) {
		
		byte[] buff = data.getBytes();
		DatagramPacket packetOut = new DatagramPacket(buff, buff.length);
		packetOut.setAddress( destAdd );
		packetOut.setPort( destPort );
		
		try {
			socket.send(packetOut);
		} 
		catch (IOException e) {
			System.out.println("cant send...");
			e.printStackTrace();
		}
	}
	
	protected void receiveData() {
		byte[] buffer = new byte[2048];
		DatagramPacket packetIn = new DatagramPacket(buffer, buffer.length);
		while(true) {
			try {
				// wait for incoming messages
				socket.receive(packetIn);
				
				String data = new String(packetIn.getData());

				// "emit" and event when message received
				String[] packet = new String[3];
				packet[0] = packetIn.getAddress().getHostAddress();
				packet[1] = String.valueOf(packetIn.getPort());
				packet[2] = data;
				
				setChanged();
				notifyObservers( packet );	
			}
			catch(Exception e) {
				
				System.out.println("Something bad happened");
				e.printStackTrace();
				System.exit(-1);
			}
			
			// Clear the buffer
			for(int i=0; i<buffer.length; ++i)
				buffer[i] = 0;
		}
		
	}
	
	public void messageSeen() {
		clearChanged();
	}
	
}
