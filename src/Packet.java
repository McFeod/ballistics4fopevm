import javafx.geometry.Point2D;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Математическая модель снаряда
 */
public class Packet {
	//характеристика шара
	public final double RADIUS = 1.0; //радиус шара в метрах
	public final double S = RADIUS * RADIUS * Math.PI; //площадь сечения шара
	public final double DENSITY = 7800; //плотность шара
	public final double WEIGHT = 4/3*RADIUS*S*DENSITY;
	//характеристика среды
	public final double G = 9.8;
	public final double L = 0.0065; //просто константа
	public final double R = 8.314; //еще одна константа
	public final double T0 = 288.15; //температура на уровне моря
	public final double P0 = 101325; //Давление на уровне моря
	public final double M = 0.029; //молярная масса воздуха
	public final double Cf = 0.47; //коэффициент для вычисления сопротивления воздуха
	
	volatile private Point2D mPosition;
	volatile private Point2D mSpeed, mAcceleration, mAirResistance, mGravity, mWindSpeed;
	private Double mTimeDelta = 0.0;  // время в секундах между двумя состояниями
	private Double mTime; //общее время
	private Double mStartSpeed;
	private Point2D mTarget;
	private ExecutionMarkers mMarkers;
	private Queue<Point2D> mLastPositions;
	private Queue<Double> mLastDeltas;

	/**Наименьший прямоугольник, в который может быть вписана траектория полёта
	* Вычисляется для нужд масштабирования*/
	private Point2D flightRectangle;

	public Packet(Double speed) {
		mStartSpeed = speed;

		mPosition = new Point2D(0.0, 0.0);
		mAcceleration = new Point2D(0.0, 0.0);
		mAirResistance = new Point2D(0.0, 0.0);
		mWindSpeed = new Point2D(-20.0, 0.0);
		mGravity =  new Point2D(0.0, -WEIGHT* G);
		mTime = 0.0;
		mLastDeltas = new ArrayDeque<Double>();
		mLastPositions = new ArrayDeque<Point2D>();

		//TODO уточнить формулы: при наличии сил сопротивления подгонка под экран не работает
		double ascentTime = mStartSpeed/Math.sqrt(2)/G; //mSpeed.getY() / G;
		double maxHeightInVacuum = G * Math.pow(ascentTime, 2) / 2;
		double distanceInVacuum = mStartSpeed/Math.sqrt(2) * ascentTime * 2;//mSpeed.getX() * ascentTime * 2;
		flightRectangle = new Point2D(distanceInVacuum, maxHeightInVacuum);
	}
	
	private void calcAirResistance(){
		double t = T0 - mPosition.getY()*L;
		double p = P0 * Math.pow(1-L*mPosition.getY()/T0, G*M/R/L);
		double thickness = p*M/R/t;
		//сопротивление воздуха
		mAirResistance = mSpeed.normalize().multiply(-Cf*thickness*mSpeed.magnitude()*mSpeed.magnitude()/2*S);
		//ветер
		//todo найти адекватную инфу по нему
		mAirResistance = mAirResistance.add(mWindSpeed.normalize().multiply(
				mWindSpeed.magnitude()*mWindSpeed.magnitude()*0.5*S*thickness));
	}
	
	private void calcAcceleration(){
		calcAirResistance();
		mAcceleration = mGravity.add(mAirResistance).multiply(1.0/WEIGHT);
	}
	/**
	 * Основной шаг логической части программы
	 * @param dS - расстояние (в метрах) между двумя состояниями снаряда
	 */
	public void update(Double dS){
		calcAcceleration();

		mLastDeltas.add(mTimeDelta);
		mTimeDelta = dS / mSpeed.magnitude();
		mTime += mTimeDelta;

		mSpeed = mSpeed.add(mAcceleration.multiply(mTimeDelta));
		mLastPositions.add(mPosition);
		mPosition = mPosition.add(mSpeed.multiply(mTimeDelta));

		mMarkers.refresh(mPosition);
	}

	public Point2D getFlightRectangle() {
		return flightRectangle;
	}

	@Deprecated
	public Point2D getPosition() {
		if (mLastPositions.isEmpty())
			return mPosition;
		return mLastPositions.poll();
	}

	public Double getLastDelta(){
		if (mLastDeltas.isEmpty())
			return 0.0;
		return mLastDeltas.poll();
	}

	public Double getStartSpeed() {
		return mStartSpeed;
	}

	public Point2D getSpeed() {
		return mSpeed;
	}

	public Boolean getSummarize(){
		return mMarkers.summarize();
	}

	public Point2D getTarget() {
		return mTarget;
	}

	public Double getTime() {
		return mTime;
	}

	public boolean helpToChoose(){
		return (Math.abs(mSpeed.getY()/(mSpeed.getX()+1e-5)) > 1);
	}

	public boolean inTheAir(){
		return (mPosition.getY()>=0);
	}

	public void resetMarkers(){
		mMarkers.reset();
	}

	public void resetSpeed(Double angle){
		mSpeed = new Point2D(Math.cos(angle)*mStartSpeed, Math.sin(angle)*mStartSpeed);
	}

	public void resetTime(){
		mTime = 0.0;
	}

	public void setPosition(Point2D position) {
		mPosition = position;
	}

	public void setTarget(Point2D target) {
		mTarget = target;
		mSpeed = new Point2D(Math.cos(Math.atan(target.getY()/target.getX()))*mStartSpeed,
				Math.sin(Math.atan(target.getY() / target.getX()))*mStartSpeed);
	}

	public void setupMarkers(Double hitRadius){
		mMarkers = new ExecutionMarkers(mTarget, hitRadius);
	}


}
