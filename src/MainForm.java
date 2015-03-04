
/**
 * Пока что примерный набросок
 */

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MainForm extends Application {

	private static MainView mainView;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception{

		HBox root = FXMLLoader.load(getClass().getResource("main_form.fxml"));
		mainView = new MainView(512,512);
		root.getChildren().add(0,mainView);

		primaryStage.setTitle("Packet fly");
		Scene scene = new Scene(root, 640, 512);
		primaryStage.setScene(scene);
		scene.getStylesheets().add("main_form.css");
		primaryStage.show();

		VisualizationThread thread = new VisualizationThread();
		thread.start(100, mainView);
	}

	@FXML
	public void repaintBackground(){
		mainView.fillBackground();
	}
}
