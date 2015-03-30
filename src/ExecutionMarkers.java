import javafx.geometry.Point2D;

/**
 * Набор проверок взаимного расположения объектов. Нужен для AngleChoice
 */
class ExecutionMarkers {
	private final Point2D mTarget;
	private final Double eps;
	private boolean yReached = false, isReachable = false;
	private Double lastX = 0.0; // 1 точка, в которой достигнута высота цели.
	private Double firstX = Double.MAX_VALUE; // 2 точка, в которой достигнута высота цели. Вряд ли их больше.

	public ExecutionMarkers(Point2D target, double radius) {
		mTarget = target;
		eps = radius;
	}

	/**
	 * Проверки на взаимное расположение снаряда и цели
	 *
	 * @param pos - координаты снаряда
	 * @return true - если нужно остановить запуски
	 */
	public boolean refresh(Point2D pos) {
		Double px = pos.getX();
		Double py = pos.getY();
		Double dx = Math.abs(mTarget.getX() - px);
		Double dy = Math.abs(mTarget.getY() - py);

		if (dx <= eps / 2) {
			if (dy <= eps) {
				VisualizationThread.mustDie = true;
				VisualizationThread.targetReached = true;
				return true;  // цель достигнута
			}
		} else {
			if (dy <= eps) {
				if (yReached) {
					lastX = px;
				} else {
					yReached = true;
					firstX = px;
				}
			}
		}
		return false;
	}

	public void reset() {
		yReached = false;
		lastX = 0.0;
		firstX = Double.MAX_VALUE;
	}

	/**
	 * Итог одного полёта снаряда
	 *
	 * @return true, если надо уменьшать угол, false - увеличивать, null - НЕ ОПРЕДЕЛЕНО, нужно пробовать оба варианта
	 */
	public Boolean summarize() {
		if (yReached) {
			if (firstX <= mTarget.getX()) {
				if (lastX <= mTarget.getX()) {
					if (isReachable) {
						return false; // уже пролетало над целью, а теперь не долетело - поднимаем
					}
					return null;  // цель правее параболы - неопределённость
					// выбираем новый угол в зависимости от старого и запоминаем выбор
				} else {
					isReachable = true;
					return true;  // цель "под аркой" - опускаем
				}
			} else {
				return false;  // цель левее параболы - поднимаем
			}
		} else {
			return false;  // недолёт по y - поднимаем
		}
	}
}
