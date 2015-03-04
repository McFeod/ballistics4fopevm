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
		setDaemon(true);
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
			Platform.runLater(mView);
		}
	}
}
