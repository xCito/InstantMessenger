import java.net.InetAddress;
import java.util.Observable;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

/*
 * ID
 * 		Name:Message
 */

public class ChatWindow extends Observable {

	private static int instanceCount = 0;
	private int curInstanceCount;
	
	private Button sendBtn;			// UI stuff
	private TextField textF;		// UI stuff
	private TextArea textA;			// UI stuff
	private Label label;			// UI stuff
	
	private String id;				// Chat owner's ID
	private String name;			// Chat owner's Username
	private InetAddress sourceIP;	// Chat owner's IP address
	private int sourcePort;			// Chat owner's Port number
	
	private String destID;			// Recipient's ID
	private String destName;		// Recipient's Username
	private InetAddress destIP;		// Recipient's IP address
	private int destPort;			// Recipient's Port number
	
	public boolean requesting;	
	
	public ChatWindow( String username, InetAddress srcIP, int srcPort ) {
		curInstanceCount = instanceCount++;
		
		this.name = username;
		this.sourceIP = srcIP;
		this.sourcePort = srcPort;
		this.requesting = false;
		
		textA = new TextArea();
		textF = new TextField();
		sendBtn = new Button("send");
		label = new Label( destID );
		
		sendBtn.setOnAction( e -> sendButtonEvent() );
		textA.setEditable(false);
		
		createView();
		createID();
		
	}

	/**
	 * Launches Chat Window
	 */
	public void openNewChatWindow() {
		Stage stage = new Stage();
		Scene scene = new Scene( createView() );
		stage.setScene(scene);
		stage.setHeight(200);
		stage.setWidth(300);
		stage.setTitle("Chat #" + curInstanceCount);
		stage.show();
	}

	
	public void passToChatWindow(String msg) {
		
		// Handle name requests
		if(msg.contains("NAME_REQUEST")) {
			System.out.println(name + "--N RES");
			setChanged();
			notifyObservers(id+":NAME_RESPONSE="+id);
		}
		
		// Handle name responses
		else if(msg.contains("NAME_RESPONSE") ) {
			requesting = false;
			System.out.println(name + "--SUC");
			destName = msg.substring( msg.indexOf('=')+1);
		}
		
		// Handle other messages
		else {
			otherAppendToMessageHistory(msg);
		}
	}
	
	public void acceptNameResponse(String name) {
		destID = name;
		label.setText(destID);
	}
	/**
	 * Appends Text to the textArea (Conversation history)
	 * @param msg - The message
	 */
	public void ownerAppendToMessageHistory(String msg) {
		Platform.runLater(() -> textA.appendText("Me: "+msg + "\n"));
	}
	private void otherAppendToMessageHistory(String msg) {
		Platform.runLater(() -> textA.appendText(destID+": "+msg + "\n"));
	}


	/**
	 * Constructs chat owner's ID
	 */
	private void createID() {
		id = name+""+curInstanceCount;
	}

	
	/**
	 * Sends a name request to other user 
	 */
	public void sendNameRequest() {
		String msg = id+":NAME_REQUEST";
		requesting = true;
		setChanged();
		notifyObservers(msg);
		System.out.println(name + "init NR--" + System.currentTimeMillis());
	}

	/**
	 * Send Button Event, triggered when clicked.
	 * - Extracts the text from the input field, and clears it.
	 * - Notify MessengerApp.java that user wants to send
	 *       a message.
	 * - Append extracted text to TextArea
	 */
	private void sendButtonEvent() {
		String msg = textF.getText();
		textF.clear();
		
		if(msg.equals(""))
			return;
		
		setChanged();
		notifyObservers(id+":"+msg);
	
		ownerAppendToMessageHistory(msg);
	}
	
	/**
	 * Creates the UI for the chat window
	 */
	private BorderPane createView() {
		BorderPane border = new BorderPane();
		AnchorPane anchor = new AnchorPane();
		AnchorPane.setRightAnchor(sendBtn, 1.0);
		anchor.getChildren().add(sendBtn);
		
		HBox hbox = new HBox();
		HBox.setHgrow(textF, Priority.ALWAYS);
		hbox.getChildren().addAll(textF, anchor);
	
		border.setTop(label);
		border.setCenter(textA);
		border.setBottom(hbox);
		
		return border;
	}
	
	// ------------------------------ GETTERS ------------------------------ //
	/**
	 * Retrieves the IP address of the message destination
	 * @return ip address
	 */
	public InetAddress getIP() {
		return destIP;
	}
	
	/**
	 * Retrieves the Port number of the message destination
	 * @return port number
	 */
	public int getPort() {
		return destPort;
	}
	
	
	/**
	 * Retrieves the ID of the other user send this chat messages
	 * Used to verify if incoming message is for this chat window
	 * @return identifier composed of name,number,ip, port
	 *         of other user.
	 */
	public String getDestinationID() {
		return destID;
	}
	
	/**
	 * Retrieves other user's basic communication information
	 * @return IP address and port of other user separated by '/'
	 */
	public String getSenderIPAndPort() {
		return destIP.getHostAddress()+"/"+destPort;
	}
	
	
	// ------------------------------ SETTERS ------------------------------ //
	
	/**
	 * Set the destination IP address and port to where 
	 * messages will be sent.
	 * @param destIP   - IP address of destination
	 * @param destPort - Port number of destination
	 */
	public void setDestination( InetAddress destIP, int destPort) {
		this.destIP = destIP;
		this.destPort = destPort;
	}
	
	
	/**
	 * NOT REALLY NEEDED
	 * @param senderID
	 */
	public void setDestinationID(String senderID) {
		destID = senderID;
		label.setText(destID);
	}

}
