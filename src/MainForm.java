import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
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
	@FXML private Slider verticalScale, horizontalScale, temperatureSlider, speedSlider,
						sleepSlider, densitySlider, radiusSlider;
	@FXML private Label infoLabel, nameLabel, windSpeedLabel, selectedSpeed,
						selectedTemperature, selectedSleep, selectedDensity, selectedRadius;
	@FXML private Button refresher, refiller, stopButton;
	@FXML private CheckBox bisectionBox;
	@FXML private GridPane windSpeedBox;
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
			mainView.getPacket().setTarget(
					new Point2D(event.getX(),
							mainView.getHeight() - event.getY())
							.multiply(mainView.getScale()));
			mainView.setAngleBisectionEnabled(bisectionBox.isSelected());
			mainView.drawTarget();
			new VisualizationThread().start(mainView, new Node[]{refresher, mWindPicker,
					bisectionBox, densitySlider, radiusSlider, temperatureSlider, speedSlider},
					new Node[]{refiller, stopButton});
			lockControls();
		});

		SliderListener listener = new SliderListener();
		speedSlider.valueProperty().addListener(listener);
		densitySlider.valueProperty().addListener(listener);
		radiusSlider.valueProperty().addListener(listener);
		temperatureSlider.valueProperty().addListener(listener);
		sleepSlider.valueProperty().addListener((observable, oldV, newV) -> {
			mainView.setSleepFactor(newV.doubleValue());
		});

		selectedSpeed.textProperty().bind(speedSlider.valueProperty().asString("Speed: %.2f"));
		selectedSleep.textProperty().bind(sleepSlider.valueProperty().asString("Sleep: %.3f"));
		selectedTemperature.textProperty().bind(
				temperatureSlider.valueProperty().asString("%.2f °C, at the ground"));
		selectedDensity.textProperty().bind(densitySlider.valueProperty().asString("Density: %.1f"));
		selectedRadius.textProperty().bind(radiusSlider.valueProperty().asString("Radius: %.3f"));
		//to avoid mismatch between default slider value & default speed
		updateSettings();
		//causes NullPointer in the old places
		buildScales();
		
		Canvas canvas = new Canvas(WIND_PICKER_SIZE, WIND_PICKER_SIZE);
		windSpeedBox.add(canvas, 0, 0);
		mWindPicker = new WindPicker(WIND_PICKER_SIZE, windSpeedLabel,
				canvas.getGraphicsContext2D(), mainView.getPacket().getWindSpeed());
		windSpeedBox.add(mWindPicker, 0, 0);
	}

	/**
	 * Чтобы юзер не шалил
	 */
	private void lockControls(){
		speedSlider         .setDisable(true);
		refresher           .setDisable(true);
		bisectionBox        .setDisable(true);
		mWindPicker         .setDisable(true);
		packetView          .setDisable(true);
		densitySlider       .setDisable(true);
		radiusSlider        .setDisable(true);
		temperatureSlider   .setDisable(true);
		refiller            .setDisable(false);
		stopButton          .setDisable(false);
	}

	public static void main(String[] args) {
		launch(args);
	}

	@FXML
	void repaintBackground(){
		mainView.fillBackground(true);
	}

	/**
	 * Сброс всего и вся
	 */
	public void restart(){
		mainView.fillBackground(false);
		horizontalScale.setValue(0.0);
		verticalScale.setValue(0.0);
		VisualizationThread.mustDie = false;
		VisualizationThread.targetReached = false;
		packetView.setDisable(false);
		updateSettings();
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
		VisualizationThread.mustDie = true;
	}

	private void updateSettings(){
		mainView.setPacket(new Packet(
				speedSlider.getValue(),
				radiusSlider.getValue(),
				densitySlider.getValue(),
				temperatureSlider.getValue()
		));
	}

	private class SliderListener implements ChangeListener{
		@Override
		public void changed(ObservableValue observableValue, Object old, Object next) {
			updateSettings();
			buildScales();
		}
	}
}
