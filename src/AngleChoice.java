public class AngleChoice {
	private Double mAngle1, mAngle2, mAngle;
	private boolean isDown;
	private final double DEGREE = Math.PI/180;
	public AngleChoice(Double angle1, Double angle2, Double angle, boolean isDown) {
		mAngle1 = angle1;
		mAngle2 = angle2;
		mAngle = angle;
		this.isDown = isDown;
	}

	public Double getAngle() {
		return mAngle;
	}

	public void next(){
		if (isDown)
			mAngle2 = mAngle;
		else
			mAngle1 = mAngle;
		mAngle = (mAngle1 + mAngle2) / 2;
	}

	public void next(Boolean down){
		isDown = down;
		next();
	}

	public AngleChoice getAnother(boolean down){
		isDown = down;
		AngleChoice choice = new AngleChoice(mAngle1, mAngle2, mAngle, !isDown);
		next();
		choice.next();
		return choice;
	}

	public boolean isMatter(){
		return Math.abs(mAngle2-mAngle1)>0.5*DEGREE;
	}

}