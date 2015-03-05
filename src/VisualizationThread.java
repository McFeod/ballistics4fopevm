import javafx.application.Platform;/*
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;*/

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
public class VisualizationThread extends Thread {
	private Double mSleepFactor;  // соотношение реального времени и моделируемого
	// TODO сделать такое же для пространства
	private MainView mView;
	//private BlockingDeque<Point2D> points = new LinkedBlockingDeque<Point2D>();

	public void start(Double sleepFactor, MainView view){
		mSleepFactor = sleepFactor;
		mView = view;
		setDaemon(true); // lazy & dangerous(for IO) way to stop a thread when closing app
		start();
		/*mView.setCurvePoints(points);


		Task task = new Task(){
			@Override
			protected Object call() throws Exception{

				for (int i = 0; i < 100 ; i++) {
					Thread.sleep(mSleepTime);
					mView.getPacket().update(2.0);
					points.putLast(mView.getPacket().getPosition());
				}
				return true;
			}
		};
		new Thread(task).start();*/
	}
	
	
	@Override
	public void run() {
		mView.getPacket().update(1.0);
		while (true) {
			try {
				Thread.sleep((long)(mSleepFactor*mView.getPacket().getTimeDelta()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			Platform.runLater(mView); // way to run submitted Runnable in a JavaFX application thread

		}
	}
}
