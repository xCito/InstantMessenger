import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
//import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MessengerApp extends Application implements Observer {

	Vector<ChatWindow> list = new Vector<>();
	
	TextField destIPF;
	TextField destPortF;
	String username;
	
	InetAddress add;
	int port = 5000;
	Socket socket;
	
	public MessengerApp() {
		System.out.println("Messenger Instantiated");
		
		QueryWindow query = new QueryWindow();
		query.launch();		// Blocking call
		this.username = query.getName();
		this.port     = query.getPort();
		
		socket = new Socket(port);
		socket.addObserver(this);
		add = getIpAddress("localhost");
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		primaryStage.setScene( getNewConversationScene() );
		primaryStage.setTitle("Networking Messenger");
		primaryStage.setHeight(250);
		primaryStage.setWidth(300);
		primaryStage.show();
		
		primaryStage.setOnCloseRequest( (e) -> {
			System.exit(0);
		});
	}
	
	/**
	 * Retrieves the IP address of specified host or localhost
	 * Mainly to handle try catches here.
	 * 
	 * @param host - dotted decimal IP address
	 * @return InetAddress Object of host
	 * 		   InetAddress Object of localhost
	 * 		   null if host not found.
	 */
	private InetAddress getIpAddress(String host) {
		try {
			if(host == "localhost")
				return InetAddress.getLocalHost();
			return InetAddress.getByName(host);
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	/**
	 * Constructs the UI for Messenger App, Menu?
	 * @return Scene object with UI components
	 */
	private Scene getNewConversationScene() {
		BorderPane border = new BorderPane();
		GridPane grid = new GridPane();
		VBox vbox = new VBox();
		
		Text user = new Text("Username: " + username);
		Text infoIP = new Text( "Current IP:" + getIpAddress("localhost").getHostAddress() );
		Text infoPort = new Text("App on Port: " + this.port);
		vbox.getChildren().addAll(user, infoIP, infoPort);
		
		Label destIPLabel = new Label("Enter IP Address: ");
		Label destPortLabel = new Label("Enter port number: ");
		destIPF = new TextField();
		destPortF = new TextField();
		HBox hbox = new HBox();
		
		Button startChatBtn = new Button("Start Chat");
		startChatBtn.setPrefWidth(200);
		startChatBtn.setPrefHeight(50);
		startChatBtn.setOnAction( e -> { 
			
			String destip = destIPF.getText();
			int destPort = Integer.valueOf(destPortF.getText());
			
			ChatWindow cw = new ChatWindow(username, getIpAddress("localhost"), port);
			cw.setDestination( getIpAddress(destip), destPort );
			cw.addObserver(this);
			list.add(cw);
			
			cw.sendNameRequest();
			//cw.setRecipientID();		// NAME REQUEST NEEDED!
			cw.openNewChatWindow();
			
		});
		
		
		hbox.getChildren().add(startChatBtn);
		hbox.setAlignment(Pos.BASELINE_CENTER);
		
		grid.add(destIPLabel, 	0, 	1);
		grid.add(destPortLabel, 0,  2);

		grid.add(destIPF, 		1, 	1);
		grid.add(destPortF, 	1, 	2);
		
		grid.add(hbox, 			0, 	3,	2,	1);
		border.setCenter(grid);
		border.setTop(vbox);
		return new Scene(border);
	}

	
	/**
	 * UPDATED BY OBSERVABLE OBJECTS, like ChatWindows and Sockets.
	 * - If observable object is ChatWindow instance,
	 *       ChatWindow object wants to send a message
	 * 
	 * - If observable object is Socket instance,
	 *       > Socket object received an incoming message
	 *       > Checks if its a name request
	 *       > Attempts to find which ChatWindow the message is heading to
	 *       > If message doesn't belong to any existing messages
	 *           - Start new ChatWindow and send message there.
	 */
	@Override
	public void update(Observable o, Object data) {
	
		// For Outgoing Messages
		if( o instanceof ChatWindow) {
			String message = (String)data;
			ChatWindow chat = (ChatWindow)o;
			System.out.println("outgoing: " + message);
			socket.send( message , chat.getIP(), chat.getPort());
		}
		
		// For Incoming Messages
		if( o instanceof Socket ) 
		{
			String[] packet = (String[])data; 
			
			String senderIP 	= packet[0];
			String senderPort	= packet[1];
			String senderName	= packet[2].substring(0, packet[2].indexOf(':'));
			String senderMsg	= packet[2].substring(packet[2].indexOf(':')+1);
			

			// Handle name requests and responses
			if( senderMsg.substring(0,12).contains("NAME_REQUEST") || 
				senderMsg.substring(0, 13).contains("NAME_RESPONSE")) 
			{
				for( ChatWindow cw: list ) {
					if(cw.requesting) {
						cw.acceptNameResponse(senderName);
						return;
					}
				}
			}
			
			// Check if message received is from active chat
			if( passMessageToActiveChat( senderName, senderMsg) )
				return;
			
			// If message is not from one of the current chats...
			// New Message Received from another user!
			System.out.println("New Message!");
			ChatWindow cw = new ChatWindow(username, getIpAddress("localhost"), port);
			cw.addObserver(this);
			list.add(cw);
			
			cw.setDestination(getIpAddress(senderIP), Integer.valueOf(senderPort));
			cw.setDestinationID(senderName);
			cw.passToChatWindow(senderMsg);
			Platform.runLater( () -> cw.openNewChatWindow());
			
			
			
		}
		
	
	}
	
	/**
	 * Search for an existing chat window who is conversating with
	 * senderID.
	 * If not found,  return false.
	 * @param senderID
	 * @param msgBody
	 * @return
	 */
	public boolean passMessageToActiveChat(String senderID, String msgBody) {
		for(ChatWindow cw: list) {
			System.out.println("checking.." + senderID + " == " + cw.getDestinationID());
			if( senderID.equals( cw.getDestinationID() )) {
				cw.passToChatWindow(msgBody);
				return true;
			}
		}
		return false;
	}
	

	
	
}
