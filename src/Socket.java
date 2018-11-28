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
				System.out.println("Waiting.....");
				socket.receive(packetIn);
				
				String data = new String(packetIn.getData());
				System.out.println("----->" + data);
				
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
			
			//socket.close();
			System.out.println(socket.isClosed());
			//break;
		}
		
	}
	
	public void messageSeen() {
		clearChanged();
	}
	
}
