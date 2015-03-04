import javafx.application.Platform;

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
public class VisualizationThread extends Thread {
	private int mSleepTime;
	private MainView mView;
	
	public void start(int sleepTime, MainView view){
		mView = view;
		mSleepTime = sleepTime;
		setDaemon(true); // lazy & dangerous(for IO) way to stop a thread when closing app
		start();
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
