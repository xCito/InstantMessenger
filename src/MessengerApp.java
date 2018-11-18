import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javafx.stage.Stage;

// Broadcast Feature Branch
public class MessengerApp extends Application implements Observer {

	HashMap<String,ChatWindow> lookUpTable;
	TextField destNameField;
	String username;
	
	InetAddress add;
	int port = 5000;
	Socket socket;
	
	public MessengerApp() {
		System.out.println("Messenger Instantiated");
		lookUpTable = new HashMap<>();
		
		QueryWindow query = new QueryWindow();
		query.launch();		// Blocking call
		this.username = query.getName();
		this.port     = 64000; //query.getPort();
		
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
		primaryStage.setWidth(335);
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
		
		Label destIPLabel = new Label("Enter Name: ");
		destNameField = new TextField();
		HBox hbox = new HBox();
		
		Button startChatBtn = new Button("Start Chatting");
		startChatBtn.setPrefWidth(200);
		startChatBtn.setPrefHeight(55);
		startChatBtn.setId("startCWButton");
		startChatBtn.setDefaultButton(true);
		startChatBtn.setOnAction( e -> { 
			String destName = destNameField.getText();
			broadcastMessage(destName);
			System.out.println("sent! a broadcast!");
		});
		
		
		hbox.getChildren().add(startChatBtn);
		hbox.setAlignment(Pos.BASELINE_CENTER);
		
		grid.add(destIPLabel, 	0, 	1);
		grid.add(destNameField,	1, 	1);
		grid.add(hbox, 			0, 	2,	2,	1);
		grid.setVgap(10);
		grid.setHgap(6);
		
		border.setCenter(grid);
		border.setTop(vbox);
		return new Scene(border);
	}
	
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
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
				//list.remove(chat);
				lookUpTable.remove( chat.getIP().getHostAddress()+chat.getPort() );
				return;
			}
			
			// For Outgoing Messages
			System.out.println("outgoing: " + message);
			socket.send( message , chat.getIP(), chat.getPort(), false);
		}
		
		// For Incoming Messages
		if( o instanceof Socket ) 
		{
			System.out.println("Incoming Message");
			String[] packet = (String[])data; 
			
			String senderIP 	= packet[0];
			String senderPort	= packet[1];
			String senderMsg = packet[2];
			
			for(String str: packet) {
				System.out.println("\t-->" + str);
			}
			// Check if incoming message is a broadcasted request
			if(isBroadcastRequest( senderMsg )) {
				System.out.println("RECEIVED BROADCAST REQUEST!!!!");
				handleBroadcastRequest(senderMsg, senderIP);
				ChatWindow cw = new ChatWindow(username, getIpAddress("localhost"), port);
				cw.addObserver(this);
				cw.setDestination(getIpAddress(senderIP), Integer.valueOf(senderPort));
				cw.setDestinationName( getName( senderMsg ) );
				
				lookUpTable.put(senderIP+senderPort, cw);
				return;
			}
			
			// Check if incoming message is a broadcast response
			if(isBroadcastResponse(senderMsg)) {
				System.out.println("RECEIVED BROADCAST resPONSE !!!!");
				List<String> nameAndIP = getNameAndIP(senderMsg);
				ChatWindow cw = new ChatWindow(username, getIpAddress("localhost"), port);
				cw.addObserver(this);
				cw.setDestination(getIpAddress(nameAndIP.get(1)), 64000);
				cw.setDestinationName(nameAndIP.get(0));
				
				lookUpTable.put(nameAndIP.get(1)+64000, cw);
				Platform.runLater( () -> cw.openNewChatWindow());
				return;
			}
			
			// Check if I have received message from this sender
			ChatWindow cw = lookUpTable.get(senderIP+senderPort);
			if(cw != null) 	{// True, If in my lookUpTable
				if(!cw.isOpen())
					Platform.runLater(()-> cw.openNewChatWindow());
				
				cw.otherAppendToMessageHistory(senderMsg);
				return;
			}
			
			// v v v v v v v v v v SHOULD NOT REACH HERE v v v v v v v v v v
			
			// Not in my lookUpTable -> Message from a new sender
			ChatWindow temp = new ChatWindow(username, getIpAddress("localhost"), port);
			temp.addObserver(this);
			temp.setDestination(getIpAddress(senderIP), Integer.valueOf(senderPort));
			temp.otherAppendToMessageHistory(senderMsg);
			lookUpTable.put(senderIP+senderPort, temp);
			
			Platform.runLater( () -> temp.openNewChatWindow());
		}
	}
	
	/**
	 * Checks if the incoming message has a REQUEST pattern.
	 * @param msg
	 * @return true - If message has the pattern of a REQUEST
	 *        false - Message doesnt have pattern of a REQUEST 
	 */
	public boolean handleBroadcastRequest(String requestMsg, String srcIp) {
		
		if(requestMsg.contains(username)) {
			String resp = getResponse();
			socket.send(resp, getIpAddress(srcIp), 64000, false);
			return true;
		}
		System.out.println("Broadcast not for me");
		return false;
	}
	
	public List<String> getNameAndIP(String respMsg) {
		String regexPatt = "^[#]{5}\\s(.*)\\s[#]{5}\\s(.*)$";
		Pattern patt = Pattern.compile(regexPatt);
		Matcher match = patt.matcher( respMsg );
		String name = "";
		String ip = "";
		while(match.find()) {
			name = match.group(1);
			ip	 = match.group(2);
		}
		
		return Arrays.asList(name,ip);
	}
	
	public String getName(String reqMsg) {
		String regexPatt = "^[\\?]{5}\\s(.*)\\s[#]{5}\\s(.*)$";
		Pattern patt = Pattern.compile(regexPatt);
		Matcher match = patt.matcher( reqMsg );
		String name = "";
		while(match.find()) {
			name = match.group(2);
		}
		
		return name;
	}
	
	public boolean isBroadcastRequest(String msg) {
		String regexPatt = "^[\\?]{5}\\s.*\\s[#]{5}\\s.*$";
		return msg.matches(regexPatt);
	}
	
	public boolean isBroadcastResponse(String msg) {
		String regexPatt = "^[#]{5}\\s.*\\s[#]{5}\\s.*$";
		return msg.matches(regexPatt);
	}
	
	public void broadcastMessage(String otherName) {
		
		String broadcastMsg = "????? " +otherName+ " ##### " +username;		
		InetAddress netBroadcastAdd = getIpAddress("255.255.255.255");

		try {
			socket.broadcast(broadcastMsg, netBroadcastAdd);
		}
		catch(Exception e) {
			System.out.println("NOPE");
		}		
	}
	
	public String getResponse() {
		return "##### " +username+ " ##### " + getIpAddress("localhost").getHostAddress();
	}
	
}

