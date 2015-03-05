import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.concurrent.BlockingDeque;

/**
 * Холст с методом отрисовки одного шага
 */
public class MainView extends Canvas implements Runnable {
	private GraphicsContext mContext;
	private int sizeX, sizeY;
	private BlockingDeque<Point2D> curvePoints;
	private Point2D currentPoint = new Point2D(0,0);
	Packet mPacket;

	public MainView(int sizeX, int sizeY){
		super(sizeX, sizeY);
		this.sizeX = sizeX;
		this.sizeY = sizeY;

		mContext = getGraphicsContext2D();
		fillBackground();
		mPacket = new Packet(new Point2D(0.0, 0.0), new Point2D(0.0, 0.0));
	}
	
	public Packet getPacket(){
		return mPacket;
	}

	@Override
	public void run(){
		try{
			while(!curvePoints.isEmpty()){
				drawPacket(currentPoint, Color.WHITESMOKE);
				currentPoint = curvePoints.takeFirst();
				drawPacket(currentPoint, Color.BLACK);
			}
		}
		catch(InterruptedException unimportant){/**/}
	}

	public void setCurvePoints(BlockingDeque<Point2D> curvePoints){
		this.curvePoints = curvePoints;
	}

	public void fillBackground(){
		mContext.setFill(Color.LIGHTSKYBLUE);
		mContext.fillRect(0,0,sizeX,sizeY);
	}

	public void drawPacket(Point2D position, Color color){
		mContext.setFill(color);
		mContext.fillOval(position.getX(), mContext.getCanvas().getHeight()-position.getY(), 10, 10); //3 3
	}
}
