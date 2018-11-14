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
		Label label = new Label("Choose Port for Messenger App: ");
		Label label2 = new Label("Choose a nickname: ");
		label.setId("qLabel");
		label2.setId("qLabel");
		
		TextField field = new TextField();		
		TextField field2 = new TextField();
		Button btn = new Button("Open Messenger App");
		
		btn.setOnAction( (e) -> { 
			this.port = Integer.valueOf(field.getText()); 
			this.name = field2.getText();
			stage.close(); 
		});
		btn.setDefaultButton(true);
		btn.setId("queryButton");
		
		vbox.getChildren().addAll(label, field, label2, field2, btn);
		vbox.setAlignment(Pos.CENTER);
		vbox.getStylesheets().add("style.css");
		
		Scene scene = new Scene(vbox);
		scene.getStylesheets().add("style.css");
		stage.setScene(scene);
		stage.setWidth(330);
		stage.setHeight(300);
		stage.setTitle("Port Selection");
		stage.initStyle(StageStyle.UTILITY);
		stage.setOnCloseRequest((e) -> System.exit(0));
		stage.showAndWait();
	}
	
	public String getName() {
		return name;
	}
	public int getPort() {
		return port;
	}
}
