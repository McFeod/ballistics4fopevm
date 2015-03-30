import javafx.geometry.Point2D;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Математическая модель снаряда
 */
public class Packet {
	//характеристика среды
	final double G = 9.80665;
	//характеристика шара
	private double mRadius; //радиус шара в метрах
	private double S; //площадь сечения шара
	private double mDensity; //плотность шара
	private double mWeight;
	private final double L = 0.0065; //просто константа
	private final double R = 8.314; //еще одна константа
	private double T0 = 288.15; //температура на уровне моря
	private final double P0 = 101325; //Давление на уровне моря
	private final double M = 0.029; //молярная масса воздуха
	private final double Cf = 0.47; //коэффициент для вычисления сопротивления воздуха
	private final Double mStartSpeed;
	private final Queue<Point2D> mLastPositions;
	private final Queue<Double> mLastDeltas;
	/**
	 * Наименьший прямоугольник, в который может быть вписана траектория полёта
	 * Вычисляется для нужд масштабирования
	 */
	//private final Point2D flightRectangle;
	protected Point2D mAcceleration;
	protected Point2D mAirForce;
	protected Point2D mGravity;
	protected Point2D mWindSpeed;
	private Point2D mPosition;
	private Point2D mSpeed;
	private Double mTimeDelta = 0.0;  // время в секундах между двумя состояниями
	private Double mTime; //общее время
	private Point2D mTarget;
	private ExecutionMarkers mMarkers;

	public Packet(Double speed, Double radius, Double density, Double temperature, Point2D wind) {
		mRadius = radius;
		mDensity = density;
		S = mRadius * mRadius * Math.PI;
		mWeight = 4 / 3 * mRadius * S * mDensity;

		mStartSpeed = speed;
		T0 = temperature + 237.15;
		mPosition = new Point2D(0.0, 0.0);
		mAcceleration = new Point2D(0.0, 0.0);
		mAirForce = new Point2D(0.0, 0.0);
		mWindSpeed = wind;
		mGravity = new Point2D(0.0, -mWeight * G);
		mTime = 0.0;
		mLastDeltas = new ArrayDeque<>();
		mLastPositions = new ArrayDeque<>();
	}

	void calcAcceleration() {
		calcAirResistance();
		mAcceleration = mGravity.add(mAirForce).multiply(1.0 / mWeight);
	}

	void calcAirResistance() {
		double t = T0 - mPosition.getY() * L;
		double p = P0 * Math.pow(1 - L * mPosition.getY() / T0, G * M / R / L);
		double thickness = p * M / R / t;
		// ветер и сопротивление воздуха
		mAirForce = resistance(thickness, mWindSpeed).add(resistance(thickness, mSpeed.multiply(-1.0)));

	}

	public Double calcMaxDistance() { //попробуем обойтись без копирования снаряда. Вроде, ничего не сломалось...
		setTarget(new Point2D(-5, -5));
		setupMarkers(10.0);
		while (inTheAir()) update(false);
		double distance = getPosition().getX();
		resetTime();
		resetSpeed(50 / 180 * Math.PI);
		resetMarkers();
		while (inTheAir()) update(false);
		return Math.max(distance, getPosition().getX());
	}

	/**
	 * Нужно для расчёта sleep time
	 *
	 * @return Время в секундах (в симуляции, а не реальное!)
	 */
	public Double getLastDelta() {
		if (mLastDeltas.isEmpty())
			return 0.0;
		return mLastDeltas.poll();
	}

	/*
	Использовать только в логической части. Для потока отрисовки
	эта информация ещё не актуальна.
	#2
	 */
	public Point2D getPosition() {
		return mPosition;
	}

	public Point2D getSpeed() {
		return mSpeed;
	}

	public Double getStartSpeed() {
		return mStartSpeed;
	}

	public Boolean getSummarize() {
		return mMarkers.summarize();
	}

	public Point2D getTarget() {
		return mTarget;
	}

	public Double getTime() {
		return mTime;
	}

	public Point2D getWindSpeed() {
		return mWindSpeed;
	}

	public void setWindSpeed(Point2D windSpeed) {
		mWindSpeed = windSpeed;
	}

	/*
	Использовать только 1 раз за цикл отрисовки!
	#2
	 */
	public synchronized Point2D getUnrendered() {
		if (mLastPositions.isEmpty())
			return mPosition;
		return mLastPositions.poll();
	}

	/**
	 * Способ выбора угла в неясной ситуации
	 *
	 * @return true - если нужно уменьшать угол
	 */
	public boolean helpToChoose() {
		return (Math.abs(mSpeed.getY()) / Math.abs(mSpeed.getX() + 1e-5)) > 1;
	}

	public boolean inTheAir() {
		return (mPosition.getY() >= 0);
	}

	public void resetMarkers() {
		mMarkers.reset();
	}

	public void resetSpeed(Double angle) {
		mSpeed = new Point2D(Math.cos(angle) * mStartSpeed, Math.sin(angle) * mStartSpeed);
	}

	public void resetTime() {
		mTime = 0.0;
		mLastPositions.clear();
		mLastDeltas.clear();
	}

	/**
	 * Формула подсчёта сопротивления.
	 *
	 * @param thickness - плотность (?)
	 * @param speed     - скорость снаряда относительно среды
	 * @return сила сопротивления (вектор)
	 */
	Point2D resistance(Double thickness, Point2D speed) {
		return speed.normalize().multiply(Cf * thickness * Math.pow(speed.magnitude(), calcExponent()) / 2 * S);
	}

	/**
	 * Очень грубая подгонка ф-и, которая монотонна, непрерывна и
	 * @return примерно 2 на дозвуковых, 1 на сверхзвуке, что-то между в переходном состоянии
	 */
	Double calcExponent(){
		return 2.0;   //1.5-Math.atan((mSpeed.magnitude()-340)/34)/Math.PI;
	}



	public void setPosition(Point2D position) {
		mPosition = position;
	}

	public void setTarget(Point2D target) {
		mTarget = target;
		mSpeed = new Point2D(Math.cos(Math.atan(target.getY() / target.getX())) * mStartSpeed,
				Math.sin(Math.atan(target.getY() / target.getX())) * mStartSpeed);
	}

	public void setupMarkers(Double hitRadius) {
		mMarkers = new ExecutionMarkers(mTarget, hitRadius);
	}

	/**
	 * Основной шаг логической части программы
	 * dS = 2*mRadius - расстояние (в метрах) между двумя состояниями снаряда
	 *
	 * @param keepTrack - включение/отключение очереди точек и timeDeltas
	 */
	public synchronized boolean update(boolean keepTrack) {
		calcAcceleration();
		if (keepTrack) {
			mLastDeltas.add(mTimeDelta);
			mLastPositions.add(mPosition);
		}
		mTimeDelta = 2 * mRadius / Math.abs(mSpeed.magnitude() + 1e-5);
		mTime += mTimeDelta;
		mSpeed = mSpeed.add(mAcceleration.multiply(mTimeDelta));
		mPosition = mPosition.add(mSpeed.multiply(mTimeDelta));
		return mMarkers.refresh(mPosition);
	}

	public Point2D getGravity() {
		return mGravity;
	}

	public Point2D getAirForce() {
		return mAirForce;
	}

	public Point2D getAcceleration() {
		return mAcceleration;
	}

}
