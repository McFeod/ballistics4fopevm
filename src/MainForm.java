
/**
 * Пока что примерный набросок
 */

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class MainForm extends Application implements Initializable {

	@FXML private GridPane root;
	private static MainView mainView;
	@FXML private VBox verticalScale;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception{

		GridPane root = FXMLLoader.load(getClass().getResource("main_form.fxml"));

		primaryStage.setTitle("Packet fly");
		Scene scene = new Scene(root, 640, 512);
		primaryStage.setScene(scene);
		scene.getStylesheets().add("main_form.css");
		primaryStage.show();

		VisualizationThread thread = new VisualizationThread();
		thread.start(50.0, mainView);
	}

	@FXML @Override
	public void initialize(URL location, ResourceBundle resources) {
		mainView = new MainView(512,512);
		root.add(mainView,1,0);
		buildScale(verticalScale, mainView.getHeight());
	}

	@FXML
	public void repaintBackground(){
		mainView.fillBackground();
	}

	private void buildScale(Pane pane, double markLimit){
		int scaleMark = (int)Math.floor(mainView.getScale()) * 50;
		int currentMark = 0;
		for (int i = 0; i < markLimit / 50; i++) {
			pane.getChildren().add(new Label(Integer.toString(currentMark)));
			currentMark+=scaleMark;
		}
	}
}
