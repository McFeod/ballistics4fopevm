import javafx.application.Platform;
import javafx.geometry.Point2D;

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
public class VisualizationThread extends Thread {
	private Double mSleepFactor;  // соотношение реального времени и моделируемого
	private MainView mView;

	private final double DEGREE = Math.PI/180;
	private double mStartSpeed;
	private double mAngle1, mAngle2, mAngle;

	public void start(Double sleepFactor, MainView view){
		mSleepFactor = sleepFactor;
		mView = view;
		
		mStartSpeed = mView.getPacket().getSpeed().magnitude();
		mAngle1 = 0;
		mAngle2 = Math.PI/2;
		mAngle = Math.atan(mView.getPacket().getSpeed().getY()/mView.getPacket().getSpeed().getX());
		
		setDaemon(true); // lazy & dangerous(for IO) way to stop a thread when closing app
		start();
	}
	
	@Override
	public void run() {
		mView.getPacket().update(1.0);
		if (mView.isAngleBisectionEnabled()){
			start:
			while (Math.abs(mAngle2-mAngle1)>0.5*DEGREE){
				boolean yReached = false;
				Double gx = mView.getGoal().getX();
				Double gy = mView.getGoal().getY();
				Double lastX = 0.0; // 1 точка, в которой достигнута высота цели.
				Double firstX = 100500.0; // 2 точка, в которой достигнута высота цели. Вряд ли их больше.
				Double lastSpeedTg = 2.0;
				
				while (mView.getPacket().getPosition().getY()>=0) {
					Double px = mView.getPacket().getPosition().getX();
					Double py = mView.getPacket().getPosition().getY();
					Double dx = Math.abs(gx-px);
					Double dy = Math.abs(gy-py);

					try {
						Thread.sleep((long)(mSleepFactor*mView.getPacket().getTimeDelta()*1000)+1);
					} catch (Exception e) {
						e.printStackTrace();
					}
					Platform.runLater(mView); // way to run submitted Runnable in a JavaFX application thread

					if (dx<=mView.mPacket.RADIUS) {
						lastSpeedTg = -mView.getPacket().getSpeed().getY()/mView.getPacket().getSpeed().getX();
						if (dy <= mView.getPacket().RADIUS) {
							break start;
						}
					}else {
						if (dy <= mView.getPacket().RADIUS) {
							if (yReached){
								lastX = px;
							}else {
								yReached = true;
								firstX = px;
							}
						}
					}
				}
				// (цель "под аркой") либо (слева ниже вершины арки, причём угол большой)
				boolean xOver = (((lastX >= gx)||lastSpeedTg>=1) && (firstX <= gx));
				nextAngle(yReached&&(xOver));
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
	
	public void nextAngle(boolean down){
		if (down)
			mAngle2 = mAngle;
		else
			mAngle1 = mAngle;
		mAngle = (mAngle1 + mAngle2) / 2;
		mView.getPacket().setSpeed(new Point2D(Math.cos(mAngle)*mStartSpeed, Math.sin(mAngle)*mStartSpeed));
		mView.reset();
	}
}
