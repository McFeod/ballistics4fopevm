import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

/**
 * Холст с методом отрисовки одного шага
 */
public class MainView extends Canvas implements Runnable {
	public static final Color BACKGROUND = Color.rgb(60, 105, 117);
	private static final int TAIL_GAGE = 2;
	private GraphicsContext mTopContext, mBottomContext;
	Packet mPacket;
	private double scale;
	public volatile boolean isReady = false;

	private Label speedXLabel, speedYLabel, speedLabel, xLabel, yLabel, timeLabel, angleLabel;
	private boolean isAngleBisectionEnabled = false;
	private Color mTailColor;

	public MainView(Canvas canvas, int sizeX, int sizeY, Double sleepFactor){
		super(sizeX, sizeY);

		mTopContext = canvas.getGraphicsContext2D();
		mBottomContext = getGraphicsContext2D();
		fillBackground();
		mPacket = new Packet(212.0, 1.0, sleepFactor);
		Point2D drawingArea = mPacket.getFlightRectangle();
		scale = Math.max(drawingArea.getX()/sizeX, drawingArea.getY()/sizeY);

	}

	@Override
	public void run() {
			drawAll(Color.BLACK, mTailColor);
			isReady = false;
			mPacket.update(5.0);
			isReady = true;
			refreshObjects();
	}

	public Packet getPacket() {
		return mPacket;
	}

	public double getScale() {
		return scale;
	}

	public void drawAll(Color packetColor, Color tailColor){
		plaster();
		drawCircle(mBottomContext, mPacket.getPosition(), tailColor, TAIL_GAGE);
		drawCircle(mTopContext, mPacket.getPosition(), packetColor, mPacket.RADIUS);
	}
	
	private void drawCircle(GraphicsContext context, Point2D position, Color color, double radius){
		context.setFill(color);
		context.fillOval((position.getX())/scale-radius/2,
				getHeight()-(position.getY())/scale-radius/2, radius, radius);
	}
	
	private void plaster(){
		mTopContext.clearRect((mPacket.getPosition().getX())/scale-mPacket.RADIUS,
				getHeight() - (mPacket.getPosition().getY())/scale-mPacket.RADIUS,
				mPacket.RADIUS*2, mPacket.RADIUS*2);
	}
	
	
	public void fillBackground() {
		mBottomContext.setFill(BACKGROUND);
		mBottomContext.fillRect(0,0,getWidth(),getHeight());
	}

	/**
	 * Отрисовка мишени вручную
	 */
	private void drawTarget() {
		mBottomContext.setStroke(Color.RED);
		mBottomContext.setFill(Color.RED);
		mBottomContext.strokeOval(mPacket.getTarget().getX() / scale - mPacket.RADIUS,
				getHeight() - mPacket.getTarget().getY() / scale - mPacket.RADIUS, mPacket.RADIUS*2, mPacket.RADIUS*2);
		mBottomContext.strokeOval(mPacket.getTarget().getX() / scale - mPacket.RADIUS/2,
				getHeight() - mPacket.getTarget().getY() / scale - mPacket.RADIUS/2, mPacket.RADIUS, mPacket.RADIUS);
		mBottomContext.fillOval(mPacket.getTarget().getX() / scale - 1,
				getHeight() - mPacket.getTarget().getY() / scale - 1, 2, 2);
		mBottomContext.moveTo(mPacket.getTarget().getX() / scale, getHeight() - mPacket.getTarget().getY() / scale - mPacket.RADIUS-2);
		mBottomContext.lineTo(mPacket.getTarget().getX() / scale, getHeight() - mPacket.getTarget().getY() / scale + mPacket.RADIUS+2);
		mBottomContext.moveTo(mPacket.getTarget().getX() / scale - mPacket.RADIUS-2, getHeight() - mPacket.getTarget().getY() / scale);
		mBottomContext.lineTo(mPacket.getTarget().getX() / scale + mPacket.RADIUS+2, getHeight() - mPacket.getTarget().getY() / scale);
		mBottomContext.stroke();
	}
	
	public void setRefreshableObjects(Label speedXLabel, Label speedYLabel, Label speedLabel,
			Label xLabel, Label yLabel, Label timeLabel, Label angleLabel){
		this.speedXLabel = speedXLabel;
		this.speedYLabel = speedYLabel;
		this.speedLabel = speedLabel;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.timeLabel = timeLabel;
		this.angleLabel = angleLabel;
	}

	/**
	 * Вывод информации о полёте
	 */
	private void refreshObjects(){
		speedXLabel.setText(String.format("SpeedX = %.4f м/с", mPacket.getSpeed().getX()));
		speedYLabel.setText(String.format("SpeedY = %.4f м/с", mPacket.getSpeed().getY()));
		speedLabel.setText(String.format("Speed = %.4f м/с", mPacket.getSpeed().magnitude()));
		xLabel.setText(String.format("X = %.4f м", mPacket.getPosition().getX()));
		yLabel.setText(String.format("Y = %.4f м", mPacket.getPosition().getY()));
		timeLabel.setText(String.format("Time = %.3f с", mPacket.getTime()));
		angleLabel.setText(String.format("Angle = %.4f", 
				Math.atan(mPacket.getSpeed().getY()/mPacket.getSpeed().getX())*180/Math.PI));
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

	public void setGoal(Point2D goal) {
		mPacket.setTarget(goal);
	}

	public Point2D getGoal(){
		return mPacket.getTarget();
	}
}
