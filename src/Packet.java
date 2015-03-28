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
	private final double RADIUS = 1.0; //радиус шара в метрах
	private final double S = RADIUS * RADIUS * Math.PI; //площадь сечения шара
	private final double DENSITY = 7800; //плотность шара
	private final double WEIGHT = 4 / 3 * RADIUS * S * DENSITY;
	private final double L = 0.0065; //просто константа
	private final double R = 8.314; //еще одна константа
	private final double T0 = 288.15; //температура на уровне моря
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
	private final Point2D flightRectangle;
	volatile Point2D mAcceleration;
	volatile Point2D mAirForce;
	volatile Point2D mGravity;
	volatile Point2D mWindSpeed;
	private volatile Point2D mPosition;
	private volatile Point2D mSpeed;
	private Double mTimeDelta = 0.0;  // время в секундах между двумя состояниями
	private Double mTime; //общее время
	private Point2D mTarget;
	private ExecutionMarkers mMarkers;

	public Packet(Double speed) {
		mStartSpeed = speed;

		mPosition = new Point2D(0.0, 0.0);
		mAcceleration = new Point2D(0.0, 0.0);
		mAirForce = new Point2D(0.0, 0.0);
		mWindSpeed = new Point2D(-20.0, 0.0);
		mGravity = new Point2D(0.0, -WEIGHT * G);
		mTime = 0.0;
		mLastDeltas = new ArrayDeque<>();
		mLastPositions = new ArrayDeque<>();

		//TODO уточнить формулы: при наличии сил сопротивления подгонка под экран не работает
		double extraFactor = 1 - 0.5 * mStartSpeed / 1000;
		double ascentTime = mStartSpeed / Math.sqrt(2) / G; //mSpeed.getY() / G;
		double maxHeightInVacuum = G * Math.pow(ascentTime, 2) / 2;
		double distanceInVacuum = mStartSpeed / Math.sqrt(2) * ascentTime * 2 * extraFactor;//mSpeed.getX() * ascentTime * 2;
		flightRectangle = new Point2D(distanceInVacuum, maxHeightInVacuum);
	}

	void calcAcceleration() {
		calcAirResistance();
		mAcceleration = mGravity.add(mAirForce).multiply(1.0 / WEIGHT);
	}

	void calcAirResistance() {
		double t = T0 - mPosition.getY() * L;
		double p = P0 * Math.pow(1 - L * mPosition.getY() / T0, G * M / R / L);
		double thickness = p * M / R / t;
		// ветер и сопротивление воздуха
		mAirForce = resistance(thickness, mWindSpeed).add(resistance(thickness, mSpeed.multiply(-1.0)));

	}

	public Point2D getFlightRectangle() {
		return flightRectangle;
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

	/*
	Использовать только 1 раз за цикл отрисовки!
	#2
	 */
	public synchronized Point2D getUnrendered() {
		if (mLastPositions.isEmpty())
			return mPosition;
		return mLastPositions.poll();
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
		return speed.normalize().multiply(Cf * thickness * speed.magnitude() * speed.magnitude() / 2 * S);
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
	 * dS = 2*RADIUS - расстояние (в метрах) между двумя состояниями снаряда
	 *
	 * @param keepTrack - включение/отключение очереди точек и timeDeltas
	 */
	public synchronized boolean update(boolean keepTrack) {
		calcAcceleration();
		if (keepTrack) {
			mLastDeltas.add(mTimeDelta);
			mLastPositions.add(mPosition);
		}
		mTimeDelta = 2 * RADIUS / mSpeed.magnitude();
		mTime += mTimeDelta;
		mSpeed = mSpeed.add(mAcceleration.multiply(mTimeDelta));
		mPosition = mPosition.add(mSpeed.multiply(mTimeDelta));
		return mMarkers.refresh(mPosition);
	}
}
