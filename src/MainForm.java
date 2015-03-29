import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class MainForm extends Application implements Initializable {

	private static final int WIND_PICKER_SIZE = 180 ;
	@FXML private GridPane root;
	private static MainView mainView;
	private static Canvas packetView;
	@FXML private Slider verticalScale;
	@FXML private Slider horizontalScale;
	@FXML private Label infoLabel;
	@FXML private Label nameLabel;
	@FXML private Slider speedSlider;
	@FXML private Label selectedSpeed;
	@FXML private Slider sleepSlider;
	@FXML private Label selectedSleep;
	@FXML private Button refresher;
	@FXML private CheckBox bisectionBox;
	@FXML private GridPane windSpeedBox;
	@FXML private Label xWindSpeedLabel;
	@FXML private Label yWindSpeedLabel;
	private static WindPicker mWindPicker;

	/**
	 * Страдания со шкалами
	 * @param scale - на самом деле никакая не шкала, а слайдер О_о
	 * @param scaleSize - размер шкалы в пикселах
	 * @param interval - размер главного деления в пикселах
	 */
	private void buildScale(Slider scale, double scaleSize, double interval){
		double markNumber = Math.floor(scaleSize / interval);
		double maxMark = scaleSize * mainView.getScale();

		scale.setMax(maxMark);
		scale.setMajorTickUnit(maxMark / markNumber);
		scale.setDisable(true);

		// Пришлось это вернуть, т.к. при размерах поля, отличных от 1024*512, всё очень плохо
		StringConverter<Double> converter = new StringConverter<Double>() {
			@Override
			public String toString(Double object) {
				int n = ((int) Math.round(object / 5) * 5);
				return String.valueOf(n);
			}
			@Override
			public Double fromString(String string) {
				return null;
			}
		};
		scale.setLabelFormatter(converter);
	}

	/**
	 * Из start() и initialize() нельзя получить width и height() элементов,
	 * т.к. они ещё не созданы. Даже из слушателя на WindowEvent.OnShown нельзя.
	 * */
	private void buildScales(){
		buildScale(horizontalScale, mainView.getWidth(), 50);
		buildScale(verticalScale, mainView.getHeight(), 50);
	}

	/**Теперь в наличии 3 функции инициализации.
	* Проблема в том, что обращение к FXML элементам из start() даёт NullPointer,
	* т.к. они ещё не загрузились.
	* А обращение к mainView, определённому в start() из initialize() тоже даёт NullPointer,
	* т.к. initialize вызывается строчкой FXMLLoader.load(getClass().getResource("main_form.fxml"));
	* При этом в неё FXML элементы уже загружены))
	* */

	@FXML @Override
	public void initialize(URL location, ResourceBundle resources) {
		double canvasWidth = Screen.getPrimary().getVisualBounds().getWidth() - 280;
		double canvasHeight = canvasWidth/2;
		packetView = new Canvas(canvasWidth, canvasHeight);
		mainView = new MainView(packetView, canvasWidth, canvasHeight);
		mainView.setRefreshableObjects(infoLabel, horizontalScale, verticalScale);
		root.add(mainView, 1, 1);
		root.add(packetView, 1, 1);
		nameLabel.setText("SpeedX\nSpeedY\nSpeed\nX\nY\nTime\nGravity\nAero Force\nAcceleration");

		packetView.setOnMouseClicked((MouseEvent event) -> {
			speedSlider.setDisable(true);
			mainView.getPacket().setTarget(
					new Point2D(event.getX(),
							mainView.getHeight() - event.getY())
							.multiply(mainView.getScale()));
			VisualizationThread.showOnlySolution = bisectionBox.isSelected();
			mainView.drawTarget();

			new VisualizationThread().start(mainView, ()->{ // перевод фокуса на кнопку - пашет не всегда O_o
				refresher.setDisable(false);
				refresher.requestFocus();
			});
			packetView.setDisable(true);
		});

		speedSlider.valueProperty().addListener((observable, oldV, newV) -> {
			mainView.setPacket(new Packet(newV.doubleValue()));
			buildScales();
		});
		sleepSlider.valueProperty().addListener((observable, oldV, newV) -> {
			mainView.setSleepFactor(newV.doubleValue());
		});
		selectedSpeed.textProperty().bind(speedSlider.valueProperty().asString("Speed: %.2f"));
		selectedSleep.textProperty().bind(sleepSlider.valueProperty().asString("Sleep: %.2f"));
		//to avoid mismatch between default slider value & default speed
		mainView.setPacket(new Packet(speedSlider.valueProperty().doubleValue()));
		//causes NullPointer in the old places
		buildScales();
		
		Canvas canvas = new Canvas(WIND_PICKER_SIZE, WIND_PICKER_SIZE);
		windSpeedBox.add(canvas, 0, 0);
		mWindPicker = new WindPicker(WIND_PICKER_SIZE, xWindSpeedLabel, yWindSpeedLabel,
				canvas.getGraphicsContext2D(), mainView.getPacket().getWindSpeed());
		windSpeedBox.add(mWindPicker, 0, 0);
		refresher.setDisable(true);
	}


	public static void main(String[] args) {
		launch(args);
	}

	@FXML
	void repaintBackground(){
		mainView.fillBackground();
	}

	/**
	 * Сброс всего и вся
	 */
	public void restart(){
		refresher.setDisable(true);
		repaintBackground();
		horizontalScale.setValue(0.0);
		verticalScale.setValue(0.0);
		speedSlider.setDisable(false);
		Marksman.targetReached = false;
		packetView.setDisable(false);
		mainView.setPacket(new Packet(speedSlider.getValue()));
		mainView.getPacket().setWindSpeed(mWindPicker.getValue());
		mainView.setSleepFactor(sleepSlider.getValue());
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

	/**
	 * "На следующей остановите!"
	 */
	public void stopTrying(){
		if (VisualizationThread.isRunning)
			Marksman.targetReached = true;
	}
}
