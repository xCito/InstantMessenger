import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Socket extends Observable {

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
	
	/**
	 * Sends a User Datagram Protocol (UDP) message to a specified IP and Port. 
	 * @param data		- The message / payload
	 * @param destAdd	- The destination IP address where the message is going
	 * @param destPort	- The port number of that destination
	 */
	public void send( String data, InetAddress destAdd, int destPort) {
		byte[] buff = data.getBytes();
		DatagramPacket packetOut = new DatagramPacket(buff, buff.length, destAdd, destPort);

		try {
			socket.send(packetOut);
		} 
		catch (IOException e) {
			System.out.println("cant send...");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Open a temporary DatagramSocket, broadcast a message out the specified network,
	 * close the DatagramSocket
	 * @param broadcastMessage	- The message to be broadcasted
	 * @param address			- The Broadcast address (eg: 255.255.255.255)
	 * @throws IOException		- Just in case I can't send it out the socket for some reason
	 */
    public void broadcast( String broadcastMessage, InetAddress address) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
 
        byte[] buffer = broadcastMessage.getBytes();

        DatagramPacket packet 
          = new DatagramPacket(buffer, buffer.length, address, 64000);
        socket.send(packet);
        socket.close();
    }
    
    
	protected void receiveData() {
		byte[] buffer = new byte[2048];
		DatagramPacket packetIn = new DatagramPacket(buffer, buffer.length);
		while(true) {
			try {
				// wait for incoming messages
				socket.receive(packetIn);
				
				String data = new String(packetIn.getData());
				System.out.println("----->" + data);		// DEBUG (prints the incoming message)
				
				// Hold packet data in string array
				String[] packet = new String[3];
				packet[0] = packetIn.getAddress().getHostAddress();	// IP
				packet[1] = String.valueOf(packetIn.getPort());		// Port
				packet[2] = data;									// Payload
				
				// "emit" an event when message received and send packet data as well
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
	
	
	// Not really using this..
	public void messageSeen() {
		clearChanged();
	}
	
}
