
/**
 * Пока что примерный набросок
 */

import java.util.Random;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainForm extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		HBox root = new HBox(); // окно из левой и правой частей
		VBox controls = new VBox(); // в одной из них всё подряд вертикально
		Canvas mainView = new Canvas(512,512); // в другой - canvas
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
		GraphicsContext gc = mainView.getGraphicsContext2D();

		primaryStage.show();

		Random random = new Random();
		Thread thread = new Thread() {
			@Override
			public void run() {
				while(true){
					try{
						Thread.sleep(100);
					}catch (Exception e){}
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							gc.setFill(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
							gc.fillRect(0, 0, 512, 512);
						}
					});
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}
}
