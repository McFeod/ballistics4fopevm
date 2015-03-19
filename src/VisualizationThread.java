import javafx.application.Platform;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
public class VisualizationThread extends Thread {
	private MainView mView;

	private AngleChoice mCurrentChoice;
	private Queue<AngleChoice> mChoices;

	public static boolean targetReached = false;

	public void start(MainView view){
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
		double updateStep = 5.0;// 2.0 * mView.getScale();
		mView.getPacket().setupMarkers(mView.PACKET_GAGE);
		mView.getPacket().update(1.0);
		if (mView.isAngleBisectionEnabled()){
			start:
			while (mCurrentChoice.isMatter()){
				mView.getPacket().resetMarkers();
				while (mView.getPacket().inTheAir()) {
					while (!mView.isReady); //volatile же
					mView.getPacket().update(updateStep);
					try {
						Thread.sleep(mView.getSleepTime());
					} catch (Exception ignore) {}
					Platform.runLater(mView);
					if (targetReached){
						break start;
					}
				}
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
		}else
			while (mView.getPacket().inTheAir()) {
				try {
					Thread.sleep(mView.getSleepTime());
				} catch (Exception e) {
					e.printStackTrace();
				}
				Platform.runLater(mView);
			}
	}
}
