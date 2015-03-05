import javafx.application.Platform;
import javafx.concurrent.Task;

import javafx.geometry.Point2D;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
public class VisualizationThread extends Thread {
	private int mSleepTime;
	private MainView mView;
	private BlockingDeque<Point2D> points = new LinkedBlockingDeque<Point2D>();
	
	public void start(int sleepTime, MainView view){
		mView = view;
		mSleepTime = sleepTime;
		setDaemon(true); // lazy & dangerous(for IO) way to stop a thread when closing app
		start();

		mView.setCurvePoints(points);
		Packet packet = mView.getPacket();

		Task task = new Task(){
			@Override
			protected Object call() throws Exception{
				for (int i = 0; i < 100 ; i++) {
					Thread.sleep(mSleepTime);
					packet.changeSpeed(Math.cos(i/4),Math.sin(i/4));
					packet.update(2.0);
					points.putLast(packet.getPosition());
				}
				return true;
			}
		};
		new Thread(task).start();
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(mSleepTime);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Platform.runLater(mView); // way to run submitted Runnable in a JavaFX application thread
		}
	}
}
