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
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

// Broadcast Feature Branch
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
		
		Scene scene = getNewConversationScene();
		scene.getStylesheets().add("style.css");
		primaryStage.setScene( scene );
		primaryStage.setTitle("Messenger Thing");
		primaryStage.setHeight(325);
		primaryStage.setWidth(355);
		primaryStage.getIcons().add(new Image("LetterM.png"));
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
		
		Label user = new Label("Username:\t" + username);
		Label infoIP = new Label( "Current IP:\t" + getIpAddress("localhost").getHostAddress() );
		Label infoPort = new Label("App on Port:\t" + this.port);
		user.setId("maLabel");
		infoIP.setId("maLabel");
		infoPort.setId("maLabel");
		
		vbox.getChildren().addAll(user, infoIP, infoPort);
		
		Label destIPLabel = new Label("Enter IP Address: ");
		Label destPortLabel = new Label("Enter Port: ");
		destIPF = new TextField();
		destPortF = new TextField();
		HBox hbox = new HBox();
		
		Button startChatBtn = new Button("Start Chatting");
		startChatBtn.setPrefWidth(200);
		startChatBtn.setPrefHeight(55);
		startChatBtn.setId("startCWButton");
		startChatBtn.setDefaultButton(true);
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
		grid.setVgap(10);
		grid.setHgap(6);
		
		border.setCenter(grid);
		//BorderPane.setAlignment(grid, Pos.CENTER);
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
	
		if( o instanceof ChatWindow) {
			
			String message = (String)data;
			ChatWindow chat = (ChatWindow)o;
			
			// Remove this Chat Window from list of activeChats
			if(message.equals("CLOSED")) {
				list.remove(chat);
				return;
			}
			
			// For Outgoing Messages
			System.out.println("outgoing: " + message);
			socket.send( message , chat.getIP(), chat.getPort());
		}
		
		// For Incoming Messages
		if( o instanceof Socket ) 
		{
			System.out.println("Incoming Message");
			String[] packet = (String[])data; 
			boolean internal;
			
			String senderIP 	= packet[0];
			String senderPort	= packet[1];
			String senderName = "_";
			String senderMsg = packet[2];
			
			internal = fromOtherMessengerApp(packet[2]+":");
			
			if( internal ) {
				System.out.println("INTERNAL MESSAGE RECEIVED");
				senderName	= packet[2].substring(0, packet[2].indexOf(':'));
				senderMsg	= packet[2].substring(packet[2].indexOf(':')+1);
				
				// Handle Name Responses by forwarding responses to the Chat that had sent requests
				if( senderMsg.substring(0, 13).contains("NAME_RESPONSE") ) {
					for( ChatWindow cw: list ) {
						if(cw.isRequesting()) {
							cw.acceptNameResponse(senderName);
							return;
						}
					}
				}
				
				// Check if incoming message is for an active chat
				if( passMessageToActiveChat( senderName, senderMsg ) )
					return;
				
				// If message is not for one of the active chats...
				// Its a New Message from another user!
				System.out.println("New Message! From " + senderName);
				ChatWindow cw = new ChatWindow(username, getIpAddress("localhost"), port);
				cw.addObserver(this);
				list.add(cw);
				cw.setDestination(getIpAddress(senderIP), Integer.valueOf(senderPort));
				cw.setDestinationID(senderName);
				
				// If New Message is a name request, reply with name response
				// and Dont open chat window yet..
				if ( senderMsg.substring(0,12).contains("NAME_REQUEST") ) {
					cw.sendNameResponse();
					return;
				}
				
			} else {
				System.out.println("OUTSIDER MESSAGE RECEIVED");
				for(ChatWindow cw: list) {
					if( cw.getIPString().equals(senderIP) && cw.getPort() == Integer.valueOf(senderPort)) {
						cw.otherAppendToMessageHistory(senderMsg);
						return;
					}
				}
				
				ChatWindow cw = new ChatWindow(username, getIpAddress("localhost"), port);
				cw.addObserver(this);
				list.add(cw);
				cw.setDestination(getIpAddress(senderIP), Integer.valueOf(senderPort));
				cw.otherAppendToMessageHistory(senderMsg);
				cw.isInternalCommunication(false);
				
				Platform.runLater( () -> cw.openNewChatWindow());
		
			}
			
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
				cw.otherAppendToMessageHistory(msgBody);
				
				if(!cw.isOpen())
					Platform.runLater( () -> cw.openNewChatWindow());
				
				return true;
			}
		
		}
		return false;
	}
	

	public boolean fromOtherMessengerApp(String packet) {
		System.out.println("Yerrr");
		System.out.println( packet + " ---> regex check: " + packet.matches("\\w+\\d:.*"));
		System.out.println( packet );
		System.out.println( "regex check: " + packet.matches("\\w+\\d:.*"));
		return packet.matches("\\w+\\d:.*");
	}
	
}
