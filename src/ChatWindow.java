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
 * The ID is composed of:
 * 		The users Name curInstance count appended: Ex: Bob0, Nick2
 * 
 * The Chat Window has a Name Request and Response feature where 
 * USER1 wants to send a message to USER2. USER1 sends a name request to USER2 to
 * obtain their name before chatting.
 */

public class ChatWindow extends Observable {

	private static int instanceCount = 0;	// Used to help Identify which instance 
	private int curInstanceCount;			// of chat Window to forward incoming messages to
	
	private Button sendBtn;			// UI stuff
	private TextField textF;		// UI stuff
	private TextArea textA;			// UI stuff
	private Label label;			// UI stuff
	
	private String id;				// Chat owner's ID
	private String name;			// Chat owner's Username
	
	private String destID;			// Recipient's ID
	private InetAddress destIP;		// Recipient's IP address
	private int destPort;			// Recipient's Port number
	
	private boolean requesting;		// True if waiting for a response to a name request 
	private boolean isOpen;			// True if the chat window is open
	
	public ChatWindow( String username, InetAddress srcIP, int srcPort ) {
		curInstanceCount = instanceCount++;
		
		this.name = username;
		this.requesting = false;
		this.isOpen = false;
		
		textA = new TextArea();
		textF = new TextField();
		sendBtn = new Button("send");
		label = new Label( destID );
		
		sendBtn.setOnAction( e -> sendButtonEvent() );
		textA.setEditable(false);
		
		createView();
		createID();
	}

// Chat Window core functions
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
		isOpen = true;
		
		// Let the "main" class know this ChatWindow is no longer active
		stage.setOnCloseRequest( (e) -> {
			setChanged();
			notifyObservers("CLOSED");
		});
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
		
		if(msg.equals(""))		// Ignore empty string messages 
			return;
		
		setChanged();
		notifyObservers(id+":"+msg);	// Notify "main" message is going to be sent
	
		ownerAppendToMessageHistory(msg);	
	}
	
	/**
	 * Appends Text to the textArea (Conversation history)
	 * @param msg - The message
	 */
	private void ownerAppendToMessageHistory(String msg) {
		Platform.runLater(() -> textA.appendText("Me: "+msg + "\n"));
	}
	public void otherAppendToMessageHistory(String msg) {
		Platform.runLater(() -> textA.appendText( destID.substring(0, destID.length()-1)+": "+msg + "\n"));
	}
	
	/**
	 * Constructs chat owner's ID
	 */
	private void createID() {
		id = name+""+curInstanceCount;
	}
	
// NAME REQUESTS AND RESPONSES
	/**
	 * Sends message (request) to the other user asking for their name
	 */
	public void sendNameRequest() {
		String msg = id+":NAME_REQUEST";
		requesting = true;
		setChanged();
		notifyObservers(msg);
		System.out.println(name + "init NR--" + System.currentTimeMillis());
	}
	
	/**
	 * Sends message (response) to other user asking for this user's name
	 */
	public void sendNameResponse() {
		System.out.println(name + "--N RES");
		setChanged();
		notifyObservers(id+":NAME_RESPONSE="+id);
	}
	
	/**
	 * Accept the incoming name response from other user
	 * @param name
	 */
	public void acceptNameResponse(String name) {
		destID = name;
		label.setText( destID.substring(0, destID.length()-1) );
		requesting = false;
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
	 * Sets the other user's ID/Name
	 * @param senderID
	 */
	public void setDestinationID(String senderID) {
		destID = senderID;
		label.setText(destID.substring(0, destID.length()-1));
	}

	public boolean isOpen() {
		return isOpen;
	}

	public boolean isRequesting() {
		return requesting;
	}

}
