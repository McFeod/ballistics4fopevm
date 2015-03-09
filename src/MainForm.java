
/**
 * Пока что примерный набросок
 */

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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

	/*Теперь в наличии 3 функции инициализации.
	* Проблема в том, что обращение к FXML элементам из start() даёт NullPointer,
	* т.к. они ещё не загрузились.
	* А обращение к mainView, определённому в start() из initialize() тоже даёт NullPointer,
	* т.к. initialize вызывается строчкой FXMLLoader.load(getClass().getResource("main_form.fxml"));
	* При этом в неё FXML элементы уже загружены))
	* */

	@FXML @Override
	public void initialize(URL location, ResourceBundle resources) {
		mainView = new MainView(512,512);
		root.add(mainView,1,0);
		verticalScale.setAlignment(Pos.BASELINE_RIGHT);
		verticalScale.setSpacing(34);
		buildScale(verticalScale, mainView.getHeight());
	}

	/*Из start() и initialize() нельзя получить width и height() элементов,
	* т.к. они ещё не созданы. Даже из слушателя на WindowEvent.OnShown нельзя.
	* */

	@FXML
	public void repaintBackground(){
		System.out.println(verticalScale.getHeight());
		System.out.println(mainView.getScale());
		mainView.fillBackground();
	}

	/*Между label-ами внутри vbox остаются зазоры, которые не удаётся отключить.
	* Зависимость высоты зазора от высоты шрифта:
	* Шрифт   10   13   15   20   25   30
	* Зазор    4    3    4    5    7    6
	* Т.к. алгебра здесь бессильна, подогнал шрифт и spacing таким образом,
	* чтобы расстояния между делениями были по 50.
	* */

	private void buildScale(Pane pane, double markLimit){
		int scaleMark = (int)Math.round(mainView.getScale()) * 50;
		int currentMark = scaleMark * (int)(markLimit / 50);
		while(currentMark > 0){
			Label lbl = new Label(Integer.toString(currentMark));
			lbl.getStyleClass().add("scaleLabel");
			pane.getChildren().add(lbl);
			currentMark-=scaleMark;
		}
	}
}
