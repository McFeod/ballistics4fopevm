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
	private static final int TAIL_GAGE = 2;
	private GraphicsContext mTopContext, mBottomContext;
	private Packet mPacket;
	private double scale;
	public volatile boolean isReady = true;
	private Slider vSlider, hSlider;
	private Label infoLabel;
	private boolean isAngleBisectionEnabled = false;
	private Color mTailColor;
	private Point2D mCurrentPoint;

	public MainView(Canvas canvas, int sizeX, int sizeY, Double sleepFactor){
		super(sizeX, sizeY);

		mTopContext = canvas.getGraphicsContext2D();
		mBottomContext = getGraphicsContext2D();
		fillBackground();
		mPacket = new Packet(212.0, sleepFactor);
		Point2D drawingArea = mPacket.getFlightRectangle();
		scale = Math.max(drawingArea.getX()/sizeX, drawingArea.getY()/sizeY);

	}

	@Override
	public void run() {
			isReady = false;
			mCurrentPoint = mPacket.getPosition();
			drawAll(Color.BLACK, mTailColor);
			refreshObjects();
			isReady = true;
	}

	public Packet getPacket() {
		return mPacket;
	}

	public double getScale() {
		return scale;
	}

	public void drawAll(Color packetColor, Color tailColor){
		plaster();
		drawCircle(mBottomContext, mCurrentPoint, tailColor, TAIL_GAGE);
		drawCircle(mTopContext, mCurrentPoint, packetColor, mPacket.RADIUS_PIX);
	}
	
	private void drawCircle(GraphicsContext context, Point2D position, Color color, double radius){
		context.setFill(color);
		context.fillOval((position.getX())/scale-radius/2,
				getHeight()-(position.getY())/scale-radius/2, radius, radius);
	}
	
	private void plaster(){
		mTopContext.clearRect((mCurrentPoint.getX())/scale-mPacket.RADIUS_PIX,
				getHeight() - (mCurrentPoint.getY())/scale-mPacket.RADIUS_PIX,
				mPacket.RADIUS_PIX*2, mPacket.RADIUS_PIX*2);
	}
	
	
	public void fillBackground() {
		mBottomContext.setFill(BACKGROUND);
		mBottomContext.fillRect(0, 0, getWidth(), getHeight());
	}

	/**
	 * Отрисовка мишени вручную
	 */
	private void drawTarget() {
		mBottomContext.setStroke(Color.RED);
		mBottomContext.setFill(Color.RED);
		mBottomContext.strokeOval(mPacket.getTarget().getX() / scale - mPacket.RADIUS_PIX,
				getHeight() - mPacket.getTarget().getY() / scale - mPacket.RADIUS_PIX, mPacket.RADIUS_PIX*2, mPacket.RADIUS_PIX*2);
		mBottomContext.strokeOval(mPacket.getTarget().getX() / scale - mPacket.RADIUS_PIX/2,
				getHeight() - mPacket.getTarget().getY() / scale - mPacket.RADIUS_PIX/2, mPacket.RADIUS_PIX, mPacket.RADIUS_PIX);
		mBottomContext.fillOval(mPacket.getTarget().getX() / scale - 1,
				getHeight() - mPacket.getTarget().getY() / scale - 1, 2, 2);
		mBottomContext.moveTo(mPacket.getTarget().getX() / scale, getHeight() - mPacket.getTarget().getY() / scale - mPacket.RADIUS_PIX-2);
		mBottomContext.lineTo(mPacket.getTarget().getX() / scale, getHeight() - mPacket.getTarget().getY() / scale + mPacket.RADIUS_PIX+2);
		mBottomContext.moveTo(mPacket.getTarget().getX() / scale - mPacket.RADIUS_PIX-2, getHeight() - mPacket.getTarget().getY() / scale);
		mBottomContext.lineTo(mPacket.getTarget().getX() / scale + mPacket.RADIUS_PIX+2, getHeight() - mPacket.getTarget().getY() / scale);
		mBottomContext.stroke();
	}
	
	public void setRefreshableObjects(Label infoLabel,
			Slider hScale, Slider vScale){
		this.infoLabel = infoLabel;
		this.hSlider = hScale;
		this.vSlider = vScale;
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
	
	public boolean isAngleBisectionEnabled(){
		return isAngleBisectionEnabled;
	}
	
	public void setAngleBisectionEnabled(boolean value){
		isAngleBisectionEnabled = value;
		if (value)
			if (isAngleBisectionEnabled){ //todo wtf?
				drawTarget();
			}
	}
	
	public void reset(Double angle){
		mPacket.resetTime();
		mPacket.resetSpeed(angle);
		mPacket.setPosition(new Point2D(0, 0));
		Random random = new Random();
		random.nextInt(256);
		mTailColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}

}
