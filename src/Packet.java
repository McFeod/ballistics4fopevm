import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Математическая модель снаряда
 */
public class Packet {
	private Point2D mPosition;
	private Point2D mSpeed;

	public Packet(Point2D position, Point2D speed) {
		mSpeed = speed;
		mPosition = position;
	}

	public void update(Double time){
		mPosition = mPosition.add(mSpeed.multiply(time));
	}
	
	public void draw(GraphicsContext context, Color color){
		context.setFill(color);
		context.fillOval(mPosition.getX(), context.getCanvas().getHeight()-mPosition.getY(), 10, 10); //3 3
		
	}
	public Point2D getSpeed() {
		return mSpeed;
	}

	public void setSpeed(Point2D speed) {
		mSpeed = speed;
	}

	public void changeSpeed(double dX, double dY){
		mSpeed = mSpeed.add(dX, dY);
	}

	public Point2D getPosition() {
		return mPosition;
	}

	public void setPosition(Point2D position) {
		mPosition = position;
	}
}
