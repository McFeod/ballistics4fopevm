/**
 * Реализация половинного деления
 */
public class AngleChoice {
	public Double mAngle1, mAngle2, mAngle;
	public boolean isDown;

	public AngleChoice(Double angle1, Double angle2, Double angle, boolean isDown) {
		mAngle1 = angle1;
		mAngle2 = angle2;
		mAngle = angle;
		this.isDown = isDown;
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