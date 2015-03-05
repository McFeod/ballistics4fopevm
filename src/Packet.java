import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Математическая модель снаряда
 */
public class Packet {
	private Point2D mPosition;
	private Point2D mSpeed;
	private Point2D mAcceleration;
	private Double mWeight;
	private Point2D mAirResistance;
	private Point2D mCoriolis;
	private Point2D mGravity;
	private final Double g = 0.2;  //TODO масштаб не позволяет пока что выставить реальное значение
	private Double mTimeDelta;  // время в секундах между двумя состояниями
	

	public Packet(Point2D position, Point2D speed, Double weight) {
		mSpeed = speed;
		mPosition = position;
		mWeight = weight;
		mAcceleration = new Point2D(0.0, 0.0);
		mAirResistance = new Point2D(0.0001, 0.0);
		mCoriolis = new Point2D(0.0, 0.0);
		mGravity =  new Point2D(0.0, -mWeight*g);
	}
	
	private void calcResistance(){
	}

	private void calcCoriolis(){
	}
	
	private void calcAcceleration(){
		calcResistance();
		calcCoriolis();
		mAcceleration = mGravity.add(mAirResistance.add(mCoriolis)).multiply(1.0/mWeight);
	}

	/**
	 * Основной шаг логической части программы
	 * @param dX - расстояние (в метрах) между двумя состояниями снаряда
	 */
	public void update(Double dX){
		calcAcceleration();

		//Жалкая попытка вспомнить физику
		//TODO обработка особых случаев, например, с делением на ноль
		mTimeDelta = (Math.sqrt(mSpeed.getX()*mSpeed.getX()+2*mAcceleration.getX()*dX)-mSpeed.getX())/mAcceleration.getX();

		mSpeed = mSpeed.add(mAcceleration.multiply(mTimeDelta));
		mPosition = mPosition.add(mSpeed.multiply(mTimeDelta));
	}
	
	public void draw(GraphicsContext context, Color color){
		context.setFill(color);
		//TODO подбор адекватного масштабирования
		context.fillOval(mPosition.getX()/3, context.getCanvas().getHeight()-mPosition.getY()/2, 10, 10); //3 3
		
	}
	public Point2D getSpeed() {
		return mSpeed;
	}

	public Double getTimeDelta() {
		return mTimeDelta;
	}

	public void setSpeed(Point2D speed) {
		mSpeed = speed;
	}

	public Point2D getPosition() {
		return mPosition;
	}

	public void setPosition(Point2D position) {
		mPosition = position;
	}
}
