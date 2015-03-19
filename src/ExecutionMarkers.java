import javafx.geometry.Point2D;

public class ExecutionMarkers {
	private boolean yReached = false, isReachable = false;
	private Point2D mTarget;
	private Double dx, dy, px, py, eps;

	private Double lastX = 0.0; // 1 точка, в которой достигнута высота цели.
	private Double firstX = Double.MAX_VALUE; // 2 точка, в которой достигнута высота цели. Вряд ли их больше.

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

	public void reset(){
		yReached = false;
		lastX = 0.0;
		firstX = Double.MAX_VALUE;
	}

	public void setTarget(Point2D target) {
		this.mTarget = target;
	}

	public Boolean summarize(){
		if (yReached){
			if (firstX <= mTarget.getX()) {
				if (lastX <= mTarget.getX()){
					if (isReachable){
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
