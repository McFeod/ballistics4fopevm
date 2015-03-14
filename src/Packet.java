import javafx.geometry.Point2D;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Математическая модель снаряда
 */
public class Packet {
	public final double RADIUS = 10.0;
	volatile private Point2D mPosition;
	volatile private Point2D mSpeed, mAcceleration, mAirResistance, mGravity;
	private Double mWeight;
	private final Double G = 9.8;
	private Double mTimeDelta = 0.0;  // время в секундах между двумя состояниями
	private Double mTime; //общее время
	private Double mSleepFactor;
	private Double mStartSpeed;
	private Point2D mTarget;
	private ExecutionMarkers mMarkers;
	private Queue<Point2D> mLastPositions;
	private Queue<Double> mLastDeltas;

	/**Наименьший прямоугольник, в который может быть вписана траектория полёта
	* Вычисляется для нужд масштабирования*/
	private Point2D flightRectangle;

	public Packet(Double speed, Double weight, Double sleepFactor) {
		mStartSpeed = speed;

		mPosition = new Point2D(0.0, 0.0);
		mWeight = weight;
		mAcceleration = new Point2D(0.0, 0.0);
		mAirResistance = new Point2D(0.0, 0.0);
		mGravity =  new Point2D(0.0, -mWeight* G);
		mTime = 0.0;
		mSleepFactor = sleepFactor;
		mLastDeltas = new ArrayDeque<>();
		mLastPositions = new ArrayDeque<>();

		//TODO уточнить формулы: при наличии сил сопротивления подгонка под экран не работает
		double ascentTime = mStartSpeed/Math.sqrt(2)/G; //mSpeed.getY() / G;
		double maxHeightInVacuum = G * Math.pow(ascentTime, 2) / 2;
		double  distanceInVacuum = mStartSpeed/Math.sqrt(2) * ascentTime * 2;//mSpeed.getX() * ascentTime * 2;
		flightRectangle = new Point2D(distanceInVacuum, maxHeightInVacuum);
	}
	
	private void calcResistance(){
		//TODO
	}
	
	private void calcAcceleration(){
		calcResistance();
		mAcceleration = mGravity.add(mAirResistance).multiply(1.0/mWeight);
	}

	/**
	 * Основной шаг логической части программы
	 * @param dS - расстояние (в метрах) между двумя состояниями снаряда
	 */
	public void update(Double dS){
		calcAcceleration();

		//Жалкая попытка вспомнить физику
		//TODO обработка особых случаев, например, с делением на ноль
		//Да займётся этой формулой её автор
		//mTimeDelta = (Math.sqrt(mSpeed.getX()*mSpeed.getX()+2*mAcceleration.getX()*dX)-mSpeed.getX());///mAcceleration.getX();
		mLastDeltas.add(mTimeDelta);
		mTimeDelta = dS / mSpeed.magnitude();
		mTime += mTimeDelta*mSleepFactor;

		mSpeed = mSpeed.add(mAcceleration.multiply(mTimeDelta));
		mLastPositions.add(mPosition);
		mPosition = mPosition.add(mSpeed.multiply(mTimeDelta));

		mMarkers.refresh(mPosition);
	}

	public Point2D getFlightRectangle() {
		return flightRectangle;
	}

	public Point2D getSpeed() {
		return mSpeed;
	}

	public Double getTime() {
		return mTime;
	}
	
	public void setSpeed(Point2D speed) {
		mSpeed = speed;
	}

	@Deprecated
	public Point2D getPosition() {
		if (mLastPositions.isEmpty())
			return mPosition;
		return mLastPositions.poll();
	}

	public void setPosition(Point2D position) {
		mPosition = position;
	}

	public void resetTime(){
		mTime = 0.0;
	}

	public void resetSpeed(Double angle){
		Double v = mSpeed.magnitude();
		setSpeed(new Point2D(Math.cos(angle)*v, Math.sin(angle)*v));
	}

	public Point2D getTarget() {
		return mTarget;
	}

	public void setTarget(Point2D target) {
		mTarget = target;
		mSpeed = new Point2D(Math.cos(Math.atan(target.getY()/target.getX()))*mStartSpeed,
				Math.sin(Math.atan(target.getY() / target.getX()))*mStartSpeed);
	}

	public void resetMarkers(){
		mMarkers = new ExecutionMarkers(mTarget, RADIUS);
	}

	public Boolean getSummarize(){
		return mMarkers.summarize();
	}

	public long getSleepTime(){
		if (mLastDeltas.isEmpty())
			return 1;
		return (long)(mSleepFactor*mLastDeltas.poll()*1000)+1;
	}

	public boolean inTheAir(){
		return (mPosition.getY()>=0);
	}
}
