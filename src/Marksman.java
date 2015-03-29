import javafx.geometry.Point2D;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Отвечает за бинарный поиск и хранение сопутствующих данных
 */
public class Marksman{
	public class AngleChoice {
		public Double mAngle1, mAngle2, mAngle;
		public boolean isDown;

		public AngleChoice(Double angle1, Double angle2, Double angle, boolean isDown) {
			mAngle1 = angle1;
			mAngle2 = angle2;
			mAngle = angle;
			this.isDown = isDown;
		}

		public boolean isMatter(){
			return Math.abs(mCurrentChoice.mAngle2 - mCurrentChoice.mAngle1) > mEps;
		}

		/** Само половинное деление */
		public AngleChoice next() {
			if (isDown)
				mAngle2 = mAngle;
			else
				mAngle1 = mAngle;
			mAngle = (mAngle1 + mAngle2) / 2;
			return this;
		}
	}

	private static final double DEGREE = Math.PI / 180;

	//is target reached during previous shot. Changed by Execution Markers of the packet.
	public static boolean targetReached = false;

	private AngleChoice mCurrentChoice;
	private Queue<AngleChoice> mChoices = new ArrayDeque<>();
	private final double mEps; // binary search accuracy
	private Packet mPacket;

	public Marksman(Packet packet, double scale){
		mCurrentChoice = new AngleChoice(0.0, Math.PI/2, packet.getSpeed().angle(1,0)*DEGREE, true);
		mEps = DEGREE/scale;
		mPacket = packet;
	}

	/**@return last selected angle*/
	public Double getAngle(){
		return mCurrentChoice.mAngle;
	}

	/**@param shotResult - value, returned by Execution Markers' method summarize()
	 * @return angle, selected for next shot or null, if there is no sense in further shooting:
	 * target reached or all the possibilities tried.*/
	public Double selectNewAngle(Boolean shotResult){
		if(targetReached)
			return null;
		if (shotResult == null){                       // if packet hasn't reached target.x and has fallen to the left
			Point2D speed = mPacket.getSpeed();
			boolean isSharp = Math.abs(speed.getY() /  //is shot angle sharp (< 45 degrees). is abs() necessary?
			                  (speed.getX() + 1e-5)) < 1;

			// at first we try to make angle closer to 45
			mCurrentChoice.isDown = !isSharp;
			// Копирование состояния половинного деления в любой непонятной ситуации)
			mChoices.add(new AngleChoice(mCurrentChoice.mAngle1,
			                             mCurrentChoice.mAngle2,
			                             mCurrentChoice.mAngle,
			                             isSharp).next());
		} else mCurrentChoice.isDown = shotResult;

		mCurrentChoice.next();
		if(!mCurrentChoice.isMatter() && !mChoices.isEmpty()) {
			mCurrentChoice = mChoices.poll();
		}
		return (mCurrentChoice.isMatter()) ? mCurrentChoice.mAngle : null;
	}
}
