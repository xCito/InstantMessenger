import java.net.InetAddress;
import java.util.Observable;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
		
	private BorderPane root;		// UI stuff
	private Button sendBtn;			// UI stuff
	private TextField textF;		// UI stuff
	private TextArea textA;			// UI stuff
	private Label label;			// UI stuff
	private ImageView imgView;		// UI stuff
	
	private String id;				// Chat owner's ID
	private String name;			// Chat owner's Username
	
	private String destID;			// Recipient's ID
	private InetAddress destIP;		// Recipient's IP address
	private int destPort;			// Recipient's Port number
	
	private boolean requesting;		// True if waiting for a response to a name request 
	private boolean isOpen;			// True if the chat window is open
	private boolean internalComm;	// True if the ChatWindow is chatting with another ChatWindow
	private boolean darkTheme;
	
	public ChatWindow( String username, InetAddress srcIP, int srcPort ) {
		curInstanceCount = instanceCount++;
		destID = "Unknown";
		internalComm = false;
		darkTheme = false;
		this.name = username;
		this.requesting = false;
		this.isOpen = false;
		
		createView();
		createID();
	}

// Chat Window core functions
	/**
	 * Launches Chat Window
	 */
	public void openNewChatWindow() {
		Stage stage = new Stage();
		Scene scene = new Scene( root );
		scene.getStylesheets().add("style.css");
		stage.setScene(scene);
		stage.setHeight(270);
		stage.setWidth(380);
		stage.setTitle("Chat #" + curInstanceCount);
		stage.getIcons().add(new Image("LetterM.png"));
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
	private void createView() {
		root = new BorderPane();
		AnchorPane anchor = new AnchorPane();
		AnchorPane topAnchor = new AnchorPane();
		Image img = new Image("darksunicon.png");
		imgView = new ImageView(img);
		imgView.setOnMouseClicked( (e) -> toggleTheme() );
		imgView.setFitWidth(33);
		imgView.setFitHeight(30);
		textA = new TextArea();
		textA.setFocusTraversable(false);
		textF = new TextField();
		sendBtn = new Button("send");
		label = new Label( "To " + destID );
		
		
		label.setId("otherNameLabel");
		sendBtn.setOnAction( e -> sendButtonEvent() );
		sendBtn.setDefaultButton(true);
		textA.setEditable(false);
		
		anchor.getChildren().add(sendBtn);
		topAnchor.getChildren().addAll(label, imgView);
		AnchorPane.setRightAnchor(sendBtn, 1.0);
		AnchorPane.setTopAnchor(imgView, 0.0);
		AnchorPane.setRightAnchor(imgView, 0.0);
		AnchorPane.setTopAnchor(label, 1.0);
		AnchorPane.setLeftAnchor(label, 1.0);
		
		HBox lowHbox = new HBox();
		HBox.setHgrow(textF, Priority.ALWAYS);
		lowHbox.getChildren().addAll(textF, anchor);
		root.setTop(topAnchor);
		root.setCenter(textA);
		root.setBottom(lowHbox);
		root.setId("chatWindowLightBP");
		BorderPane.setMargin(textA, new Insets(3));
		toggleTheme();
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
		
		if(internalComm)
			notifyObservers(id+":"+msg);	// Notify "main" message is going to be sent
		else
			notifyObservers(msg);
		
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
	
	private void toggleTheme() {
		if(darkTheme) {
			root.setId("chatWindowLightBP");
			textA.setId("textAreaLight");
			label.setId("nameLabelLight");
			textF.setId("textFieldLight");
			sendBtn.setId("");
			imgView.setImage( new Image("DarkSunIcon.png"));
			
			darkTheme = false;
		} else {
			root.setId("chatWindowDarkBP");
			textA.setId("textAreaDark");
			label.setId("nameLabelDark");
			textF.setId("textFieldDark");
			sendBtn.setId("sendButtonDark");
			imgView.setImage( new Image("WhiteMoonIcon.png"));
			
			darkTheme = true;
		}
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
		Platform.runLater( () -> label.setText("To " + destID.substring(0, destID.length()-1) ));
		requesting = false;
		internalComm = true;
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
	 * Retrieves the IP address of the message destination
	 * @return ip address
	 */
	public String getIPString() {
		return destIP.getHostAddress();
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
	
	public void isInternalCommunication( boolean internal ) {
		internalComm = internal;
	}
	/**
	 * Set the destination IP address and port to where 
	 * messages will be sent.
	 * @param destIP   - IP address of destination
	 * @param destPort - Port number of destination
	 */
	public void setDestination( InetAddress destIP, int destPort) {
		this.destIP = destIP;
		this.destPort = destPort;
		setDestinationID( destIP.getHostAddress() +"  |  "+ destPort + " " );
	}
	
	
	/**
	 * Sets the other user's ID/Name
	 * @param senderID
	 */
	public void setDestinationID(String senderID) {
		destID = senderID;
		String displayName = destID.substring(0, destID.length()-1);
		Platform.runLater( () -> label.setText("To " + displayName));
	}

	public boolean isOpen() {
		return isOpen;
	}

	public boolean isRequesting() {
		return requesting;
	}

}
