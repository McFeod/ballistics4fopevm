import javafx.application.Platform;
import javafx.scene.control.Button;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
class VisualizationThread extends Thread {
	// #4
	public static final boolean TEST_RUN = false;

	private MainView mView;
	private Button mRefresher;
	private AngleChoice mCurrentChoice;
	private Queue<AngleChoice> mChoices;

	public static boolean targetReached = false;
	public static boolean isRunning = false;
	private final double DEGREE = Math.PI/180;
	public void start(MainView view, Button refresher){
		mView = view;
		mRefresher = refresher;
		mChoices = new ArrayDeque<>();
		mCurrentChoice = new AngleChoice(0.0, Math.PI/2,
				Math.atan(mView.getPacket().getSpeed().getY()/mView.getPacket().getSpeed().getX())
				,true, DEGREE/mView.getScale());
		setDaemon(true); // lazy & dangerous(for IO) way to stop a thread when closing app
		start();
	}
	
	@Override
	public void run() {
		isRunning = true;
		mView.getPacket().setupMarkers(MainView.PACKET_GAGE * mView.getScale() * 0.7); //#1
		mView.getPacket().update();
		if (mView.isAngleBisectionEnabled()){
			start:
			while (mCurrentChoice.isMatter()){
				mView.getPacket().resetMarkers();
				while (mView.getPacket().inTheAir()) {
					mView.getPacket().update();
					try {
						Thread.sleep(mView.getSleepTime());
					} catch (Exception ignore) {}
					Platform.runLater(mView);
					if (targetReached){
						break start;
					}
				}
				// sleep between launches
				try{
					Thread.sleep(200);
				} catch (Exception ignore){}
				Boolean result = mView.getPacket().getSummarize();
				if (result==null){
					mChoices.add(mCurrentChoice.getAnother(mView.getPacket().helpToChoose()));
				} else {
					mCurrentChoice.next(result);
				}

				if (!mChoices.isEmpty()&&(!mCurrentChoice.isMatter())){
					mCurrentChoice = mChoices.poll();
				}
				mView.reset(mCurrentChoice.getAngle());
			}
		}else{
			mView.getPacket().resetSpeed(Math.PI * 3.0 / 180);
			while (mView.getPacket().inTheAir()) {
				try {
					Thread.sleep(mView.getSleepTime());
				} catch (Exception e) {
					e.printStackTrace();
				}
				mView.getPacket().update();
				Platform.runLater(mView);
			}
		}
		Platform.runLater(() -> {
			mRefresher.setDisable(false);
			mRefresher.requestFocus();
		});
		isRunning = false;
	}
}
