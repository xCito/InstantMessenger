import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
		Label label = new Label("Choose Port for Messenger App: ");
		TextField field = new TextField();
		
		Label label2 = new Label("Choose a nickname: ");
		TextField field2 = new TextField();
		Button btn = new Button("Start Chatting");
		
		btn.setOnAction( (e) -> { 
			this.port = Integer.valueOf(field.getText()); 
			this.name = field2.getText();
			stage.close(); 
		});
		
		vbox.getChildren().addAll(label, field, label2, field2, btn);
		vbox.setAlignment(Pos.CENTER);
		
		Scene scene = new Scene(vbox);
		stage.setScene(scene);
		stage.setWidth(350);
		stage.setHeight(200);
		stage.setTitle("Port Selection");
		stage.showAndWait();
	}
	
	public String getName() {
		return name;
	}
	public int getPort() {
		return port;
	}
}
