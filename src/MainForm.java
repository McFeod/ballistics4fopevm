
/**
 * Пока что примерный набросок
 */

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
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
	@FXML private Slider speedSlider;
	@FXML private Label selectedSpeed;
	private Double mSleepFactor = 0.1;

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
		nameLabel.setText("SpeedX\nSpeedY\nSpeed\nX\nY\nTime\nAngle");

		packetView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			private boolean isStarted = false;
			@Override
			public void handle(MouseEvent event) {
				if (!isStarted) {
					speedSlider.setDisable(true);
					mainView.getPacket().setTarget(
							new Point2D(event.getX(),
									mainView.getHeight() - event.getY())
									.multiply(mainView.getScale()));
					mainView.setAngleBisectionEnabled(true);
					new VisualizationThread().start(mainView);
					isStarted = true;
				}
			}
		});

		speedSlider.valueProperty().addListener((observable, oldV, newV) -> {
			mainView.setPacket(new Packet(newV.doubleValue(), mSleepFactor));
			buildScales();
		});
		selectedSpeed.textProperty().bind(speedSlider.valueProperty().asString());
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

	private void buildScale(Slider scale, double scaleSize, double interval){;
		double markNumber = Math.floor(scaleSize / interval);
		double maxMark = scaleSize * mainView.getScale();

		// Last digit "2" means how mush significant digits after first
		// we want to keep in maxTick.
		// For usual ticks we keep (this number + 2) significant digits.
		double pow10 = Math.pow(10, (Math.floor(Math.log10(maxMark)) - 1));
		double beautyMark = Math.round(maxMark / pow10) * pow10;
		scale.setMax(beautyMark);
		scale.setMajorTickUnit(beautyMark / markNumber);
	}

	private void buildScales(){
		buildScale(horizontalScale, mainView.getWidth(),  50);
		buildScale(verticalScale,   mainView.getHeight(), 50);

		/*double scaleMark = (mainView.getScale()) * 50;
		horizontalScale.setMax(scaleMark * (mainView.getWidth() / 50));
		horizontalScale.setMajorTickUnit(scaleMark);
		verticalScale.setMax(scaleMark * (mainView.getWidth() / 100));
		verticalScale.setMajorTickUnit(scaleMark);

		// Красивая шкала с одинаковыми цифрами для малых скоростей
		StringConverter converter = new StringConverter<Double>() {
			@Override
			public String toString(Double object) {
				int n = ((int) Math.round(object)/10)*10;
				return String.valueOf(n);
			}

			@Override
			public Double fromString(String string) {
				return null;
			}
		};
		verticalScale.setLabelFormatter(converter);
		horizontalScale.setLabelFormatter(converter);*/
	}
}
