import javafx.application.Platform;
import javafx.geometry.Point2D;

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
public class VisualizationThread extends Thread {
	private Double mSleepFactor;  // соотношение реального времени и моделируемого
	// TODO сделать такое же для пространства
	private MainView mView;

	private final double DEGREE = Math.PI/180;
	private double mStartSpeed;
	private double mAngle1, mAngle2, mAngle;
	private byte mSignumX, mSignumY;
	
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
			while (Math.abs(mAngle2-mAngle1)>DEGREE){
				mSignumX = 0;
				mSignumY = 0;
				while (mView.getPacket().getPosition().getY()>=0) {
					try {
						Thread.sleep((long)(mSleepFactor*mView.getPacket().getTimeDelta()*1000));
					} catch (Exception e) {
						e.printStackTrace();
					}
					Platform.runLater(mView); // way to run submitted Runnable in a JavaFX application thread
					
					Double gx = mView.getGoal().getX();
					Double gy = mView.getGoal().getY();
					Double px = mView.getPacket().getPosition().getX();
					Double py = mView.getPacket().getPosition().getY();
					Double dx = Math.abs(gx-px);
					Double dy = Math.abs(gy-py);
					if (Math.sqrt(dx*dx+dy*dy)<mView.getPacket().RADIUS)
						break start;
					if (dx<=2)
						mSignumY = (byte)Math.signum(gy-py);
					if (dy<=2)
						mSignumX = (byte)Math.signum(gx-px);
				}
				nextAngle();
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
	
	public void nextAngle(){
		if (mSignumY == 1)//mAngle1 = mAngle;
			if (mSignumX == 1)
				mAngle2 = mAngle;
			else
				mAngle1 = mAngle;
		if (mSignumY == -1)
			mAngle2 = mAngle;
		if (mSignumY == 0)
			if (mSignumX == 1)
				mAngle2 = mAngle;
			else
				mAngle1 = mAngle;
		mAngle = (mAngle1 + mAngle2) / 2;
		mView.getPacket().setSpeed(new Point2D(Math.cos(mAngle)*mStartSpeed, Math.sin(mAngle)*mStartSpeed));
		mView.getPacket().resetTime();
		mView.getPacket().setPosition(new Point2D(0, 0));
	}
}
