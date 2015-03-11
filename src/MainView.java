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
	public static final byte DIAMETER = 10;
	
	private GraphicsContext mContext;
	Packet mPacket;
	private double scale;

	private Label speedXLabel;
	private Label speedYLabel;
	private Label speedLabel;
	private Label xLabel;
	private Label yLabel;
	private Label timeLabel;
	private Label angleLabel;
	
	private boolean isAngleBisectionEnabled = false;
	private Point2D mGoal;
	
	
	//<UPRT>
	private int r=3, g=2, b=1, stepB=1, stepR=3, stepG=2;
	private Image mImagePacket;
	private Image mImageGoal;
	//</UPRT>

	public MainView(int sizeX, int sizeY){
		super(sizeX, sizeY);

		mGoal = new Point2D(700, 700);
		mContext = getGraphicsContext2D();
		fillBackground();
		mPacket = new Packet(new Point2D(0.0, 0.0), new Point2D(100.0, 100.0), 1.0);
		Point2D drawingArea = mPacket.getFlightRectangle();
		scale = Math.max(drawingArea.getX()/sizeX, drawingArea.getY()/sizeY);
		//<UPRT>
		mImagePacket = new Image("chrome.png");
		mImageGoal = new Image("IE.png");
		//</UPRT>
	}

	@Override
	public void run() {
		//drawPacket(mPacket.getPosition(), Color.WHITESMOKE);
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
		drawPacket(mPacket.getPosition(), Color.rgb(r, g, b));
		//</UPRT>
		mPacket.update(2.0);
		//drawPacket(mPacket.getPosition(), Color.BLACK);
		//<UPRT>
		mContext.drawImage(mImagePacket, mPacket.getPosition().getX()/scale,
				getHeight()-mPacket.getPosition().getY()/scale, DIAMETER, DIAMETER);
		//</UPRT>
		refreshObjects();
	}

	public Packet getPacket() {
		return mPacket;
	}

	public double getScale() {
		return scale;
	}

	public void drawPacket(Point2D position, Color color){
		mContext.setFill(color);
		mContext.fillOval(position.getX()/scale, getHeight()-position.getY()/scale, DIAMETER, DIAMETER); //3 3
	}
	
	public void fillBackground(){
		mContext.setFill(Color.LIGHTGRAY);
		mContext.fillRect(0,0,getWidth(),getHeight());
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
			if (isAngleBisectionEnabled)
				mContext.drawImage(mImageGoal, mGoal.getX()/scale, getHeight()-mGoal.getY()/scale, DIAMETER, DIAMETER);
	}
	
	public Point2D getGoal(){
		return mGoal;
	}
}
