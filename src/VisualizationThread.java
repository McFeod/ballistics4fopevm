import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
class VisualizationThread extends Thread {
	public  static final boolean TEST_RUN = false;  //// #4
	private static final double RENDER_PAUSE = 10;
	private static final double SLEEP_FACTOR = 0.01;

	public static boolean showOnlySolution = false;
	public static boolean targetReached = false;
	public static boolean isRunning = false;

	private MainView mView;
	private Packet mPacket;
	private Button mRefresher;

	private Queue<Point2D> mPath = new ArrayDeque<Point2D>();
	private double mTimeBuffer = 0.0;

	@Override
	public void run() {
		isRunning = true;
		mView.setBuffer(mPath);
		mPacket.setupMarkers(MainView.PACKET_GAGE * mView.getScale() * 0.7); //#1
		Marksman marksman = new Marksman(mPacket, mView.getScale());
		AngleChoice currentAngle = marksman.getAngle();

		while (!(currentAngle == null || targetReached)){
			mView.reset(currentAngle.mAngle);
			singleFly(!showOnlySolution);
			if(!showOnlySolution) mySleep(200);
			currentAngle = marksman.selectNewAngle(mPacket.getSummarize());
		}
		if(showOnlySolution){
			mPath.clear();
			mView.reset(marksman.getAngle().mAngle);
			singleFly(true);
		}
		Platform.runLater(() -> {  // перевод фокуса на кнопку - пашет не всегда O_o
			mRefresher.setDisable(false);
			mRefresher.requestFocus();
		});
		isRunning = false;
	}

	private void mySleep(long time){
		try{ Thread.sleep(time);
		} catch (Exception ignore) {}
	}

	private void singleFly(boolean draw){
		while(!mPacket.update() && mPacket.inTheAir()){
			mPath.add(mPacket.getPosition()); // из пустого в порожнее
			mTimeBuffer += SLEEP_FACTOR * mPacket.getTimeDelta() * 1000;
			if(mTimeBuffer < RENDER_PAUSE) continue;

			//if RENDER_PAUSE finished
			long result = (long) mTimeBuffer;
			mTimeBuffer -= result;
			if(draw){
				mySleep(result);
				Platform.runLater(mView);
			}
		}
	}

	public void start(MainView view, Button refresher){
		mView = view;
		mPacket = view.getPacket();
		mRefresher = refresher;

		setDaemon(true); // lazy & dangerous(for IO) way to stop a thread when closing app
		start();
	}
}
