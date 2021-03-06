import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.DoubleSummaryStatistics;
import java.util.Queue;
import java.util.Random;

/**
 * Холст с методом отрисовки одного шага
 */
class MainView extends Canvas implements Runnable {
	public static final double PACKET_GAGE = 10;
	private static final Color BACKGROUND = Color.rgb(60, 105, 117);
	private static final int TAIL_GAGE = 2;
	private static final double RENDER_PAUSE = 10; // 100 FPS
	private final GraphicsContext mTopContext, mBottomContext;
	private Packet mPacket;
	private double scale;
	private Slider vSlider, hSlider;
	private Label infoLabel;
	private boolean isAngleBisectionEnabled = false;
	private Color mTailColor;
	private Point2D mCurrentPoint = new Point2D(0.0, 0.0);
	private double mSleepFactor = 0.1, mTimeBuffer = 0.0;
	private Queue<Point2D> mTailBuffer; // хранение неотрисованного следа

	/**
	 * @param canvas - Холст для шарика
	 * @param sizeX
	 * @param sizeY
	 */
	public MainView(Canvas canvas, double sizeX, double sizeY) {
		super(sizeX, sizeY);
		mTopContext = canvas.getGraphicsContext2D();
		mBottomContext = getGraphicsContext2D();
		fillBackground(false);
		mTailBuffer = new ArrayDeque<>();
	}

	public double getSleepFactor() {
		return mSleepFactor;
	}

	public void setSleepFactor(double sleepFactor) {
		mSleepFactor = sleepFactor;
	}

	/**
	 * Убираем следы от снаряда
	 */
	public void fillBackground(boolean withTarget) {
		mBottomContext.setFill(BACKGROUND);
		mBottomContext.fillRect(0, 0, getWidth(), getHeight());
		mTopContext.clearRect(0, 0, getWidth(), getHeight());
		if (withTarget) drawTarget();
	}

	public Packet getPacket() {
		return mPacket;
	}

	public double getScale() {
		return scale;
	}

	public boolean isAngleBisectionEnabled() {
		return isAngleBisectionEnabled;
	}

	/**
	 * Обёртка для fillOval
	 *
	 * @param context  - gc нужного холста
	 * @param position
	 * @param color
	 * @param radius
	 */
	private void drawCircle(GraphicsContext context, Point2D position, Color color, double radius) {
		try {
			context.setFill(color);
			context.fillOval((position.getX()) / scale - radius / 2,
					getHeight() - (position.getY()) / scale - radius / 2, radius, radius);
		} catch (NullPointerException e){System.out.print("caught");} // как это получить?
	}

	/**
	 * Отрисовка мишени вручную
	 */
	void drawTarget() {
		mBottomContext.setStroke(Color.RED);
		mBottomContext.setFill(Color.RED);
		mBottomContext.strokeOval(mPacket.getTarget().getX() / scale - PACKET_GAGE,
				getHeight() - mPacket.getTarget().getY() / scale - PACKET_GAGE, PACKET_GAGE * 2, PACKET_GAGE * 2);
		mBottomContext.strokeOval(mPacket.getTarget().getX() / scale - PACKET_GAGE / 2,
				getHeight() - mPacket.getTarget().getY() / scale - PACKET_GAGE / 2, PACKET_GAGE, PACKET_GAGE);
		mBottomContext.fillOval(mPacket.getTarget().getX() / scale - 1,
				getHeight() - mPacket.getTarget().getY() / scale - 1, 2, 2);
		mBottomContext.strokeLine(mPacket.getTarget().getX() / scale,
				getHeight() - mPacket.getTarget().getY() / scale - PACKET_GAGE - 2,
				mPacket.getTarget().getX() / scale, getHeight() - mPacket.getTarget().getY() / scale + PACKET_GAGE + 2);
		mBottomContext.strokeLine(mPacket.getTarget().getX() / scale - PACKET_GAGE - 2,
				getHeight() - mPacket.getTarget().getY() / scale,
				mPacket.getTarget().getX() / scale + PACKET_GAGE + 2, getHeight() - mPacket.getTarget().getY() / scale);
	}

	/**
	 * Затираем шарик
	 */
	private void plaster() {
		mTopContext.clearRect((mCurrentPoint.getX()) / scale - PACKET_GAGE,
				getHeight() - (mCurrentPoint.getY()) / scale - PACKET_GAGE,
				PACKET_GAGE * 2, PACKET_GAGE * 2);
	}

	/**
	 * Вывод информации о полёте
	 */
	private void refreshObjects() {
		infoLabel.setText(String.format(String.format(
								"%s\n%s\n%s\n%s\n%s\n\n%s\n\n%s\n",
								"%.4f м/с",
								"%.4f м",
								"%.4f м",
								"%.3f с",
								"%.3f Н",
								"%.3f Н",
								"%.3f м/с^2"),
						mPacket.getSpeed().magnitude(),
						mCurrentPoint.getX(),
						mCurrentPoint.getY(),
						mPacket.getTime(),
						mPacket.getGravity().magnitude(),
						mPacket.getAirForce().magnitude(),
						mPacket.getAcceleration().magnitude())
		);
		vSlider.setValue(mCurrentPoint.getY());
		hSlider.setValue(mCurrentPoint.getX());
	}

	/**
	 * Смена цвета следа + вызов методов очистки Packet
	 *
	 * @param angle - тангенс нового стартового угла
	 */
	public void reset(Double angle) {
		mPacket.resetTime();
		mPacket.resetSpeed(angle);
		mPacket.resetMarkers();
		mPacket.setPosition(new Point2D(0, 0));
		Random random = new Random();
		mTailColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}

	/**
	 * собственно, отрисовка
	 */
	@Override
	public void run() {
		plaster();
		mCurrentPoint = mPacket.getUnrendered();
		while (!mTailBuffer.isEmpty()) { // fix для следа от снаряда
			drawCircle(mBottomContext, mTailBuffer.poll(), mTailColor, TAIL_GAGE);
		}
		drawCircle(mBottomContext, mCurrentPoint, mTailColor, TAIL_GAGE);
		drawCircle(mTopContext, mCurrentPoint, Color.BLACK, PACKET_GAGE);
		refreshObjects();
	}

	public void setAngleBisectionEnabled(boolean value) {
		isAngleBisectionEnabled = value;
	}

	public void setPacket(Packet packet) {
		// #4
		this.mPacket = packet;
		scale = mPacket.calcMaxDistance() / getWidth() * 1.1; //по высоте запас всё равно слишком мал
	}

	public void setRefreshableObjects(Label infoLabel,
	                                  Slider hScale, Slider vScale) {
		this.infoLabel = infoLabel;
		this.hSlider = hScale;
		this.vSlider = vScale;
	}

	/**
	 * Вызывает необходимое число раз packet.update и сохраняет точки
	 *
	 * @return необходимое время сна в мс
	 */
	public long someUpdates() {
		while (mTimeBuffer < RENDER_PAUSE) {
			if (mPacket.update(true)) break; // попали
			mTailBuffer.add(mPacket.getUnrendered()); // из пустого в порожнее
			mTimeBuffer += mSleepFactor * mPacket.getLastDelta() * 1000;
		}
		long result = (long) mTimeBuffer;
		mTimeBuffer -= result;
		return result;
	}
}
