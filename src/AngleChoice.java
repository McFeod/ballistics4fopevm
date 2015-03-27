/**
 * Реализация половинного деления
 */
public class AngleChoice {
	private final double mEps;
	private Double mAngle1, mAngle2, mAngle;
	private boolean isDown;

	public AngleChoice(Double angle1, Double angle2, Double angle, boolean isDown, Double eps) {
		mAngle1 = angle1;
		mAngle2 = angle2;
		mAngle = angle;
		mEps = eps;
		this.isDown = isDown;
	}

	public Double getAngle() {
		return mAngle;
	}

	/**
	 * Копирование состояния половинного деления в любой непонятонй ситуации)
	 *
	 * @param down - текущий выбор, в котором есть сомнения
	 * @return - выбор, противоположный текущему
	 */
	public AngleChoice getAnother(boolean down) {
		isDown = down;
		AngleChoice choice = new AngleChoice(mAngle1, mAngle2, mAngle, !isDown, mEps);
		next();
		choice.next();
		return choice;
	}

	/**
	 * Для принудительного завершения деления в случае неудачи
	 *
	 * @return false если всё плохо
	 */
	public boolean isMatter() {
		return Math.abs(mAngle2 - mAngle1) > mEps;
	}

	/**
	 * Само половинное деление
	 */
	private void next() {
		if (isDown)
			mAngle2 = mAngle;
		else
			mAngle1 = mAngle;
		mAngle = (mAngle1 + mAngle2) / 2;
	}

	/**
	 * Задание нового угла
	 *
	 * @param down - выбор направления деления
	 */
	public void next(Boolean down) {
		isDown = down;
		next();
	}

}
