import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Холст с методом отрисовки одного шага
 */
public class MainView extends Canvas implements Runnable {
	public static final Color BACKGROUND = Color.rgb(60, 105, 117);
	private static final int TAIL_GAGE = 2;
	private GraphicsContext mTopContext, mBottomContext;
	Packet mPacket;
	private double scale;

	private Label speedXLabel, speedYLabel, speedLabel, xLabel, yLabel, timeLabel;
	
	//<UPRT>
	private int r=3, g=2, b=1, stepB=1, stepR=3, stepG=2;
	private Image mImage;
	//</UPRT>

	public MainView(Canvas canvas, int sizeX, int sizeY){
		super(sizeX, sizeY);
		mTopContext = canvas.getGraphicsContext2D();
		mBottomContext = getGraphicsContext2D();
		fillBackground();
		mPacket = new Packet(new Point2D(0.0, 0.0), new Point2D(100.0, 100.0), 1.0);
		Point2D drawingArea = mPacket.getFlightRectangle();
		scale = Math.max(drawingArea.getX()/sizeX, drawingArea.getY()/sizeY);
		//<UPRT>
		mImage = new Image("chrome.png");
		//</UPRT>
	}

	@Override
	public void run() {
		//<UPRT>
		if (b==255 || b==0)
			stepB=-stepB;
		if (g>=254 || g<=1)
			stepG=-stepG;
		if (r>=253 || r<=2)
			stepR=-stepR;
		b += stepB;
		g += stepG;
		r += stepR;
		//</UPRT>
		drawAll(Color.BLACK, /*Color.WHITESMOKE*/
				Color.rgb(r, g, b));
		
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
		mPacket.update(5.0);
		drawCircle(mBottomContext, mPacket.getPosition(), tailColor, TAIL_GAGE);
		drawCircle(mTopContext, mPacket.getPosition(), packetColor, mPacket.RADIUS);
		/*
		//<UPRT>
		mContext.drawImage(mImage, mPacket.getPosition().getX()/scale,
				getHeight()-mPacket.getPosition().getY()/scale, mPacket.RADIUS, mPacket.RADIUS);
		//</UPRT>
		*/
	}
	
	private void drawCircle(GraphicsContext context, Point2D position, Color color, int radius){
		context.setFill(color);
		context.fillOval((position.getX())/scale-radius,
				getHeight()-(position.getY())/scale-radius, radius*2, radius*2);
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
	
	public void setRefreshableObjects(Label speedXLabel, Label speedYLabel, Label speedLabel,
			Label xLabel, Label yLabel, Label timeLabel){
		this.speedXLabel = speedXLabel;
		this.speedYLabel = speedYLabel;
		this.speedLabel = speedLabel;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.timeLabel = timeLabel;
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
	}
	
}
