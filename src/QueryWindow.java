import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class QueryWindow {
	
	String name;
	int port;
	
	public QueryWindow() {
		name = "";
		port = -1;
	}

	public void launch() {
		Stage stage = new Stage();
		VBox vbox = new VBox();
		Label nameLabel = new Label("Choose your name: ");
		TextField nameInputField = new TextField();
		Button btn = new Button("Open Messenger App");
		
		// Apply style IDs and click events
		nameLabel.setId("qLabel");
		btn.setOnAction( (e) -> { 
			this.name = nameInputField.getText();	// Extract name from input field
			stage.close(); 							// Close this window
		});
		btn.setDefaultButton(true);
		btn.setId("queryButton");
		
		// Position and apply style
		vbox.getChildren().addAll(nameLabel, nameInputField, btn);
		vbox.setAlignment(Pos.CENTER);
		vbox.getStylesheets().add("style.css");
		
		// Make actual stage pretty and set sizes
		Scene scene = new Scene(vbox);
		scene.getStylesheets().add("style.css");
		stage.setScene(scene);
		stage.setWidth(330);
		stage.setHeight(300);
		stage.setTitle("Port Selection");
		stage.initStyle(StageStyle.UTILITY);
		stage.setOnCloseRequest((e) -> System.exit(0)); 
		stage.showAndWait();	// <---------- OPENS THE WINDOW and holds the Javafx thread from opening other Stages (windows)
	}
	
	public String getName() {
		return name;
	}
	public int getPort() {
		return port;
	}
}
