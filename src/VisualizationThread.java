import javafx.application.Platform;
import javafx.geometry.Point2D;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
public class VisualizationThread extends Thread {
	private Double mSleepFactor;  // соотношение реального времени и моделируемого
	private MainView mView;

	private final double DEGREE = Math.PI/180;
	private double mStartSpeed;
	private double mAngle1, mAngle2, mAngle;
	private Queue<Choice> mChoices;
	public void start(Double sleepFactor, MainView view){
		mSleepFactor = sleepFactor;
		mView = view;
		mChoices = new ArrayDeque<>();
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
				Double firstX = Double.MAX_VALUE; // 2 точка, в которой достигнута высота цели. Вряд ли их больше.

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
				//TODO refactor that
				if (yReached){
					if (firstX <= gx) {
						if (lastX<=gx){
							boolean down = mAngle >= 45*DEGREE;  // цель правее параболы - неопределённость
							remember(down);  // выбираем новый угол в зависимости от старого и запоминаем выбор
							nextAngle(down);
						} else {
							nextAngle(true);  // цель "под аркой" - опускаем
						}
					} else {
						nextAngle(false);  // цель левее параболы - поднимаем
					}
				} else {
					nextAngle(false);  // недолёт по y - поднимаем
				}

				if (!mChoices.isEmpty()&&(Math.abs(mAngle2-mAngle1)<=0.5*DEGREE)){
					Choice choice = mChoices.poll();
					mAngle1 = choice.getAngle1();
					mAngle2 = choice.getAngle2();
					mAngle = choice.getAngle();
					nextAngle(choice.isDown());
				}
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

	private void remember(boolean isDown){
		mChoices.add(new Choice(mAngle1, mAngle2, mAngle, !isDown));
	}


	private class Choice{
		private Double mAngle1, mAngle2, mAngle;
		private boolean isDown;

		public Choice(Double angle1, Double angle2, Double angle, boolean isDown) {
			mAngle1 = angle1;
			mAngle2 = angle2;
			mAngle = angle;
			this.isDown = isDown;
		}

		public Double getAngle1() {
			return mAngle1;
		}

		public Double getAngle2() {
			return mAngle2;
		}

		public Double getAngle() {
			return mAngle;
		}

		public boolean isDown() {
			return isDown;
		}
	}
}
