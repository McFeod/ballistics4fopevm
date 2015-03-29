import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

/**
 * Холст с методом отрисовки одного шага
 */
class MainView extends Canvas implements Runnable {
	public static final double PACKET_GAGE = 10;
	private static final Color BACKGROUND = Color.rgb(60, 105, 117);
	private static final int TAIL_GAGE = 2;
	private final GraphicsContext mTopContext, mBottomContext;
	private Packet mPacket;
	private double scale;
	private Slider vSlider, hSlider;
	private Label infoLabel;
	private Color mTailColor;
	private Point2D mCurrentPoint = new Point2D(0.0, 0.0);
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
		fillBackground();
	}

	/**
	 * Убираем следы от снаряда
	 */
	public void fillBackground() {
		mBottomContext.setFill(BACKGROUND);
		mBottomContext.fillRect(0, 0, getWidth(), getHeight());
		mTopContext.clearRect(0, 0, getWidth(), getHeight());
		if (VisualizationThread.isRunning) {
			drawTarget();
		}
	}

	public Packet getPacket() {
		return mPacket;
	}

	public double getScale() {
		return scale;
	}

	private double countMaxDistance(){
		final double otherDegree = Math.PI / 180;
		Packet tmpPacket = new Packet(mPacket.getStartSpeed());
		tmpPacket.setTarget(new Point2D(-5, -5));
		tmpPacket.setupMarkers(1.0);

		System.out.println("************");
		double maxDistance = 0;
		for(int i = 5; i <= 90; i+=5){
			tmpPacket.reset(otherDegree * i);
			while (tmpPacket.inTheAir()) tmpPacket.update();
			maxDistance = Math.max(maxDistance, tmpPacket.getPosition().getX());
		}
		return maxDistance;
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
		context.setFill(color);
		context.fillOval((position.getX()) / scale - radius / 2,
				getHeight() - (position.getY()) / scale - radius / 2, radius, radius);
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
								"%s\n%s\n%s\n%s\n%s\n%s\n%s",
								"%.4f м/с",
								"%.4f м/с",
								"%.4f м/с",
								"%.4f м",
								"%.4f м",
								"%.3f с",
								"%.4f"),
						mPacket.getSpeed().getX(),
						mPacket.getSpeed().getY(),
						mPacket.getSpeed().magnitude(),
						mCurrentPoint.getX(),
						mCurrentPoint.getY(),
						mPacket.getTime(),
						Math.atan(mPacket.getSpeed().getY() / mPacket.getSpeed().getX()) * 180 / Math.PI)
		);
		vSlider.setValue(mCurrentPoint.getY());
		hSlider.setValue(mCurrentPoint.getX());
	}

	/**
	 * собственно, отрисовка
	 */
	@Override
	public void run() {
		plaster();
		mCurrentPoint = mPacket.getPosition();
		while (!mTailBuffer.isEmpty()) { // fix для следа от снаряда
			drawCircle(mBottomContext, mTailBuffer.poll(), mTailColor, TAIL_GAGE);
		}
		drawCircle(mTopContext, mCurrentPoint, Color.BLACK, PACKET_GAGE);
		refreshObjects();
		if(!mPacket.inTheAir()){
			Random random = new Random();
			mTailColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
			VisualizationThread.shotPermission.release();
		}
	}

	public void setBuffer(Queue<Point2D> buffer){
		mTailBuffer = buffer;
	}

	public void setPacket(Packet packet) {
		// #4
		this.mPacket = (VisualizationThread.TEST_RUN) ? new Packet53ОФ350(packet.getStartSpeed()) : packet;
		//Point2D drawingArea = mPacket.getFlightRectangle();
		scale = countMaxDistance() / getWidth();

		//scale = Math.max(drawingArea.getX() / getWidth(), drawingArea.getY() / getHeight());
	}

	public void setRefreshableObjects(Label infoLabel,
	                                  Slider hScale, Slider vScale) {
		this.infoLabel = infoLabel;
		this.hSlider = hScale;
		this.vSlider = vScale;
	}
}
