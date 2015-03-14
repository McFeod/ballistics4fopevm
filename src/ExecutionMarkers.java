import javafx.geometry.Point2D;

public class ExecutionMarkers {
	private boolean yReached = false;
	Point2D mTarget;
	Double dx, dy, px, py, eps;
	Double lastX = 0.0; // 1 точка, в которой достигнута высота цели.
	Double firstX = Double.MAX_VALUE; // 2 точка, в которой достигнута высота цели. Вряд ли их больше.

	public ExecutionMarkers(Point2D target, double radius) {
		mTarget = target;
		eps = radius;
	}

	public void refresh(Point2D pos){
		px = pos.getX();
		py = pos.getY();
		dx = Math.abs(mTarget.getX()-px);
		dy = Math.abs(mTarget.getY()-py);

		if (dx<=eps) {
			if (dy <= eps) {
				VisualizationThread.targetReached = true;
			}
		}else {
			if (dy <= eps) {
				if (yReached){
					lastX = px;
				}else {
					yReached = true;
					firstX = px;
				}
			}
		}
	}

	public Boolean summarize(){
		if (yReached){
			if (firstX <= mTarget.getX()) {
				if (lastX <= mTarget.getX()){
					return null;  // цель правее параболы - неопределённость
					// выбираем новый угол в зависимости от старого и запоминаем выбор
				} else {
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
