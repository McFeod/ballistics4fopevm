
/**
 * Пока что примерный набросок
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class MainForm extends Application{

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		HBox root = new HBox(); // окно из левой и правой частей
		VBox controls = new VBox(); // в одной из них всё подряд вертикально
		MainView mainView = new MainView(512,512); // в другой - canvas
		Scene scene = new Scene(root, 640, 512);
		primaryStage.setScene(scene);
		scene.getStylesheets().add("main_form.css");
		controls.getStyleClass().add("control_panel");
		root.getChildren().addAll(mainView, controls);
		/* рыба для наполнения controls */
		Label exampleLabel = new Label("Smth");
		Button exampleButton = new Button("Smth2");
		ComboBox exampleCombo = new ComboBox<>();
		controls.getChildren().addAll(exampleLabel, exampleCombo, exampleButton);

		primaryStage.show();
		VisualizationThread thread = new VisualizationThread();
		thread.start(100, mainView);
	}

}
