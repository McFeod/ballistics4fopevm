
import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.stage.Screen;


public class InputServer extends Thread{
	
	private int v1, v2;
	private double v;
	private int mPort = 6000;
	private ServerSocket mServerSocket;
	private Socket mSocket;
	private DataInputStream mDataInputStream;
	private MainForm mMainForm;
	private MainView mMainView;
	private Button mStopButton;
	private Button mResetButton;
	private WindPicker mWindPicker;
	private Slider mSleepSlider;
	private Slider mStartSpeedSlider;
	private Slider mRadiusSlider;
	private Slider mDensitySlider;
	
	
	public InputServer(MainForm mainForm, MainView mainView, Button stopButton, Button resetButton,
			WindPicker windPicker, Slider sleepSlider, Slider startSpeedSlider, Slider radiusSlider,
			Slider densitySlider) {
		mMainForm = mainForm;
		mMainView = mainView;
		mWindPicker = windPicker;
		mSleepSlider = sleepSlider;
		mStartSpeedSlider = startSpeedSlider;
		mRadiusSlider = radiusSlider;
		mDensitySlider = densitySlider;
		mStopButton = stopButton;
		mResetButton = resetButton;
		this.setDaemon(true);
		try{
			mServerSocket = new ServerSocket(mPort);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try{
			System.out.println("Ожидание клиента");
			mSocket = mServerSocket.accept();
			System.out.println("Клиент подключен");
			mDataInputStream = new DataInputStream(mSocket.getInputStream());
			while (true){
				String input = mDataInputStream.readUTF();
				switch (input){
					case "start":
						Platform.runLater(() -> {
							Random rand = new Random();
							int length = (int)(Screen.getPrimary().getVisualBounds().getWidth() - 285)/2-300;
							mMainForm.start(rand.nextInt(length)+200, rand.nextInt(length)+200);
						});
						break;
					case "stop":
						Platform.runLater(() -> {
							mStopButton.fire();
						});
						break;
					case "reset":
						Platform.runLater(() -> {
							mResetButton.fire();
						});
						break;
					case "windSpeed":
						v1 = mDataInputStream.readInt();
						v2 = mDataInputStream.readInt();
						mMainView.getPacket().setWindSpeed(new Point2D(v1, v2));
						Platform.runLater(() -> {
							mWindPicker.refreshAll(v1, v2);
						});
						break;
					case "sleep":
						v = mDataInputStream.readDouble();
						Platform.runLater(() -> {
							mSleepSlider.setValue(v);
						});
						break;
					case "startSpeed":
						v1 = mDataInputStream.readInt();
						Platform.runLater(() -> {
							mStartSpeedSlider.setValue(v1);
						});
						break;
					case "radius":
						v = mDataInputStream.readDouble();
						Platform.runLater(() -> {
							mRadiusSlider.setValue(v);
						});
						break;
					case "density":
						v1 = mDataInputStream.readInt();
						Platform.runLater(() -> {
							mDensitySlider.setValue(v1);
						});
						break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}