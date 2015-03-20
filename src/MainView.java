import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * Холст с методом отрисовки одного шага
 */
public class MainView extends Canvas implements Runnable {
	public static final Color BACKGROUND = Color.rgb(60, 105, 117);
	public static final double PACKET_GAGE = 10;
	private static final int TAIL_GAGE = 2;
	private GraphicsContext mTopContext, mBottomContext;
	private Packet mPacket;
	private double scale;
	private Slider vSlider, hSlider;
	private Label infoLabel;
	private boolean isAngleBisectionEnabled = false;
	private Color mTailColor;
	private Point2D mCurrentPoint = new Point2D(0.0, 0.0);
	private Double mSleepFactor;

	public MainView(Canvas canvas, int sizeX, int sizeY){
		super(sizeX, sizeY);
		mTopContext = canvas.getGraphicsContext2D();
		mBottomContext =     getGraphicsContext2D();
		fillBackground();
		setPacket(new Packet(212.0));
	}

	public void fillBackground() {
		mBottomContext.setFill(BACKGROUND);
		mBottomContext.fillRect(0, 0, getWidth(), getHeight());
	}

	public Packet getPacket() {
		return mPacket;
	}

	public double getScale() {
		return scale;
	}

	public boolean isAngleBisectionEnabled(){
		return isAngleBisectionEnabled;
	}

	public void reset(Double angle){
		mPacket.resetTime();
		mPacket.resetSpeed(angle);
		mPacket.resetMarkers();
		mPacket.setPosition(new Point2D(0, 0));
		Random random = new Random();
		random.nextInt(256);
		mTailColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}

	@Override
	public void run() {
		plaster();
		mCurrentPoint = mPacket.getPosition();
		drawCircle(mBottomContext, mCurrentPoint, mTailColor, TAIL_GAGE);
		drawCircle(mTopContext, mCurrentPoint, Color.BLACK, PACKET_GAGE);
		refreshObjects();
	}

	public void setAngleBisectionEnabled(boolean value){
		isAngleBisectionEnabled = value;
		drawTarget();
	}

	public void setRefreshableObjects(Label infoLabel,
	                                  Slider hScale, Slider vScale){
		this.infoLabel = infoLabel;
		this.hSlider = hScale;
		this.vSlider = vScale;
	}

	public void setPacket(Packet packet) {
		this.mPacket = packet;
		Point2D drawingArea = mPacket.getFlightRectangle();
		scale = Math.max(drawingArea.getX()/getWidth(), drawingArea.getY()/getHeight());
		mSleepFactor = 400/mPacket.getStartSpeed()/mPacket.getStartSpeed()/scale;
		packet.setupMarkers(Math.min(PACKET_GAGE * scale, PACKET_GAGE));
	}

	private void drawCircle(GraphicsContext context, Point2D position, Color color, double radius){
		context.setFill(color);
		context.fillOval((position.getX())/scale-radius/2,
				getHeight()-(position.getY())/scale-radius/2, radius, radius);
	}

	/**
	 * Отрисовка мишени вручную
	 */
	private void drawTarget() {
		mBottomContext.setStroke(Color.RED);
		mBottomContext.setFill(Color.RED);
		mBottomContext.strokeOval(mPacket.getTarget().getX() / scale - PACKET_GAGE,
				getHeight() - mPacket.getTarget().getY() / scale - PACKET_GAGE, PACKET_GAGE*2, PACKET_GAGE*2);
		mBottomContext.strokeOval(mPacket.getTarget().getX() / scale - PACKET_GAGE/2,
				getHeight() - mPacket.getTarget().getY() / scale - PACKET_GAGE/2, PACKET_GAGE, PACKET_GAGE);
		mBottomContext.fillOval(mPacket.getTarget().getX() / scale - 1,
				getHeight() - mPacket.getTarget().getY() / scale - 1, 2, 2);
		mBottomContext.moveTo(mPacket.getTarget().getX() / scale, getHeight() - mPacket.getTarget().getY() / scale - PACKET_GAGE-2);
		mBottomContext.lineTo(mPacket.getTarget().getX() / scale, getHeight() - mPacket.getTarget().getY() / scale + PACKET_GAGE+2);
		mBottomContext.moveTo(mPacket.getTarget().getX() / scale - PACKET_GAGE-2, getHeight() - mPacket.getTarget().getY() / scale);
		mBottomContext.lineTo(mPacket.getTarget().getX() / scale + PACKET_GAGE+2, getHeight() - mPacket.getTarget().getY() / scale);
		mBottomContext.stroke();
	}

	private void plaster(){
		mTopContext.clearRect((mCurrentPoint.getX())/scale-PACKET_GAGE,
				getHeight() - (mCurrentPoint.getY())/scale-PACKET_GAGE,
				PACKET_GAGE*2, PACKET_GAGE*2);
	}

	/**
	 * Вывод информации о полёте
	 */
	private void refreshObjects(){
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
					Math.atan(mPacket.getSpeed().getY()/mPacket.getSpeed().getX())*180/Math.PI)
				);
		vSlider.setValue(mCurrentPoint.getY());
		hSlider.setValue(mCurrentPoint.getX());
	}

	public long getSleepTime(){
		return (long)(mSleepFactor*mPacket.getLastDelta()*1000)+1;
	}
}
