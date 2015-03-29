import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
class VisualizationThread extends Thread {
	public  static final boolean TEST_RUN = false;  //// #4
	private static final double RENDER_PAUSE = 10;
	private static final double SLEEP_FACTOR = 0.01;

	public static boolean showOnlySolution = false;
	public static boolean isRunning = false;

	// MainView permits thread to make next shot after it completes drawing previous one
	public static Semaphore shotPermission = new Semaphore(1);

	private MainView mView;
	private Packet mPacket;
	private Runnable callback;

	private Queue<Point2D> mPath = new ArrayDeque<Point2D>();
	private double mTimeBuffer = 0.0;

	@Override
	public void run() {
		isRunning = true;
		mView.setBuffer(mPath);
		mPacket.setupMarkers(MainView.PACKET_GAGE * mView.getScale() * 0.7); //#1
		Marksman marksman = new Marksman(mPacket, mView.getScale());
		Double currentAngle = marksman.getAngle();

		while (currentAngle != null){
			if(!showOnlySolution){
				try   {shotPermission.acquire();}
				catch (Exception ignore){}
			}
			mPacket.reset(currentAngle);
			singleFly(!showOnlySolution);
			if(!showOnlySolution) mySleep(1);
			currentAngle = marksman.selectNewAngle(mPacket.getSummarize());
		}
		if(showOnlySolution){
			mPath.clear();
			mPacket.reset(marksman.getAngle());
			singleFly(true);
		}
		Platform.runLater(callback);
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
		/*#7
		* 1) To avoid special handle of current position when drawing tail
		* 2) To give MainView possibility to determine, that packet ha landed
		* and change tail color.*/
		mPath.add(mPacket.getPosition());
	}

	public void start(MainView view, Runnable callback){
		mView = view;
		mPacket = view.getPacket();
		this.callback = callback;

		setDaemon(true); // lazy & dangerous(for IO) way to stop a thread when closing app
		start();
	}
}
