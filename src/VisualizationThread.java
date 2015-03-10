import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
public class VisualizationThread extends Thread {
	private Double mSleepFactor;  // соотношение реального времени и моделируемого
	// TODO сделать такое же для пространства
	private MainView mView;
	
	public void start(Double sleepFactor, MainView view){
		mSleepFactor = sleepFactor;
		mView = view;
		setDaemon(true); // lazy & dangerous(for IO) way to stop a thread when closing app
		start();
	}
	@Override
	public void run() {
		mView.getPacket().update(1.0);
		while (mView.getPacket().getPosition().getY()>=0) {
			try {
				Thread.sleep((long)(mSleepFactor*mView.getPacket().getTimeDelta()*1000));
			} catch (Exception e) {
				e.printStackTrace();
			}
			Platform.runLater(mView); // way to run submitted Runnable in a JavaFX application thread

		}
	}
}
