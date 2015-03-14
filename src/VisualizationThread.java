import javafx.application.Platform;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
public class VisualizationThread extends Thread {
	private Double mSleepFactor;  // соотношение реального времени и моделируемого
	private MainView mView;

	private AngleChoice mCurrentChoice;
	private Queue<AngleChoice> mChoices;

	public static boolean targetReached = false;

	public void start(Double sleepFactor, MainView view){
		mSleepFactor = sleepFactor;
		mView = view;
		mChoices = new ArrayDeque<>();
		mCurrentChoice = new AngleChoice(0.0, Math.PI/2,
				Math.atan(mView.getPacket().getSpeed().getY()/mView.getPacket().getSpeed().getX())
				,true);
		setDaemon(true); // lazy & dangerous(for IO) way to stop a thread when closing app
		start();
	}
	
	@Override
	public void run() {
		mView.getPacket().resetMarkers();
		mView.getPacket().update(1.0);
		if (mView.isAngleBisectionEnabled()){
			start:
			while (mCurrentChoice.isMatter()){
				mView.getPacket().resetMarkers(); // асинхронный момент ещё остался(

				while (mView.getPacket().getPosition().getY()>=0) {
					try {
						Thread.sleep((long)(mSleepFactor*mView.getPacket().getTimeDelta()*1000)+1);
					} catch (Exception e) {
						e.printStackTrace();
					}
					Platform.runLater(mView); // way to run submitted Runnable in a JavaFX application thread
					if (targetReached){
						break start;
					}
				}

				Boolean result = mView.getPacket().getSummarize();
				if (result==null){
					mChoices.add(mCurrentChoice.getAnother());
				} else {
					mCurrentChoice.next(result);
				}

				if (!mChoices.isEmpty()&&(!mCurrentChoice.isMatter())){
					mCurrentChoice = mChoices.poll();
				}
				mView.reset(mCurrentChoice.getAngle());
			}
		}else
			while (mView.getPacket().getPosition().getY()>=0) {
				try {
					Thread.sleep((long)(mSleepFactor*mView.getPacket().getTimeDelta()*1000));
				} catch (Exception e) {
					e.printStackTrace();
				}
				Platform.runLater(mView);
			}
	}
}
