
/**
 * Пока что примерный набросок
 */

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class MainForm extends Application implements Initializable {

	@FXML private GridPane root;
	private static MainView mainView;
	@FXML private Slider verticalScale;
	@FXML private Slider horizontalScale;
	@FXML private Label infoLabel;
	@FXML private Label nameLabel;
	private boolean isStarted = false;
	private Double mSleepFactor = 0.01;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception{

		root = FXMLLoader.load(getClass().getResource("main_form.fxml"));

		primaryStage.setTitle("Кликни мышкой на поле");
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		scene.getStylesheets().add("main_form.css");
		primaryStage.show();
	}

	/*Теперь в наличии 3 функции инициализации.
	* Проблема в том, что обращение к FXML элементам из start() даёт NullPointer,
	* т.к. они ещё не загрузились.
	* А обращение к mainView, определённому в start() из initialize() тоже даёт NullPointer,
	* т.к. initialize вызывается строчкой FXMLLoader.load(getClass().getResource("main_form.fxml"));
	* При этом в неё FXML элементы уже загружены))
	* */

	@FXML @Override
	public void initialize(URL location, ResourceBundle resources) {
		Canvas packetView = new Canvas(1024, 512);
		mainView = new MainView(packetView, 1024, 512, mSleepFactor);
		mainView.setRefreshableObjects(infoLabel, horizontalScale, verticalScale);
		root.add(mainView, 1, 0);
		root.add(packetView, 1, 0);
		buildScales();
		nameLabel.setText("SpeedX\nSpeedY\nSpeed\nX\nY\nAngle");
		packetView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (!isStarted) {
					mainView.getPacket().setTarget(
							new Point2D(event.getX() * mainView.getScale(),
							(mainView.getHeight() - event.getY()) * mainView.getScale()));
					mainView.setAngleBisectionEnabled(true);
					VisualizationThread thread = new VisualizationThread();
					thread.start(mainView);
					isStarted = true;
				}
			}
		});
	}

	/*Из start() и initialize() нельзя получить width и height() элементов,
	* т.к. они ещё не созданы. Даже из слушателя на WindowEvent.OnShown нельзя.
	* */

	@FXML
	public void repaintBackground(){
		System.out.println(verticalScale.getHeight());
		System.out.println(horizontalScale.getWidth());
		System.out.println(mainView.getScale());
		mainView.fillBackground();
	}

	private void buildScales(){
		int scaleMark = (int)Math.round(mainView.getScale()) * 50;
		horizontalScale.setMax(scaleMark * (int)(mainView.getWidth() / 50));
		horizontalScale.setMajorTickUnit(scaleMark);
		verticalScale.setMax(scaleMark * (int)(mainView.getWidth() / 100));
		verticalScale.setMajorTickUnit(scaleMark);
	}
}
