import javafx.geometry.Point2D;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Отвечает за бинарный поиск и хранение сопутствующих данных
 */
public class Marksman{
	private static final double DEGREE = Math.PI / 180;

	private AngleChoice mCurrentChoice;
	private Queue<AngleChoice> mChoices = new ArrayDeque<>();
	private final double mEps; // binary search accuracy
	private Packet mPacket;

	public Marksman(Packet packet, double scale){
		mCurrentChoice = new AngleChoice(0.0, Math.PI/2, packet.getSpeed().angle(1,0)*DEGREE, true);
		mEps = DEGREE/scale;
		mPacket = packet;
	}

	public AngleChoice getAngle(){
		return mCurrentChoice;
	}

	public AngleChoice selectNewAngle(Boolean shotResult){
		if (shotResult==null){                         // if packet hasn't reached target.x and has fallen to the left
			Point2D speed = mPacket.getSpeed();
			boolean isSharp = Math.abs(speed.getY() /  //is shot angle sharp (>45). is abs() necessary?
			                  (speed.getX() + 1e-5)) > 1;

			// at first we try to make angle closer to 45
			mCurrentChoice.isDown = !isSharp;
			// Копирование состояния половинного деления в любой непонятонй ситуации)
			mChoices.add(new AngleChoice(mCurrentChoice.mAngle1,
			                             mCurrentChoice.mAngle2,
			                             mCurrentChoice.mAngle,
			                             isSharp).next());

		} else mCurrentChoice.isDown = shotResult;
		mCurrentChoice.next();

		if(Math.abs(mCurrentChoice.mAngle2 - mCurrentChoice.mAngle1) < mEps){
			if(!mChoices.isEmpty())
				 mCurrentChoice = mChoices.poll();
			else mCurrentChoice = null;
		}
		return mCurrentChoice;
	}
}
