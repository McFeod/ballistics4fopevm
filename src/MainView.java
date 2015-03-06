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
	Packet mPacket;
	private BlockingDeque<Point2D> curvePoints;
	private Point2D currentPoint = new Point2D(0,0);
	private double scale;

	public MainView(int sizeX, int sizeY){
		super(sizeX, sizeY);

		mContext = getGraphicsContext2D();
		fillBackground();
		mPacket = new Packet(new Point2D(0.0, 0.0), new Point2D(10.0, 20.0), 1.0);
		Point2D drawingArea = mPacket.getFlightRectangle();
		scale = Math.max(drawingArea.getX()/sizeX, drawingArea.getY()/sizeY);
	}

	@Override
	/*public void run() {
		mPacket.draw(mContext, Color.WHITESMOKE);
		mPacket.update(2.0);
		mPacket.draw(mContext, Color.BLACK);
	}*/
	public void run(){
		try{
			while(!curvePoints.isEmpty()){
					drawPacket(currentPoint, Color.WHITESMOKE);
					currentPoint = curvePoints.takeFirst();
					drawPacket(currentPoint, Color.BLACK);
			}
		}catch(InterruptedException unimportant){}
	}
	public void setCurvePoints(BlockingDeque<Point2D> curvePoints){
		this.curvePoints = curvePoints;
	}

	public Packet getPacket() {
		return mPacket;
	}

	public void drawPacket(Point2D position, Color color){
		mContext.setFill(color);
		mContext.fillOval(position.getX()/scale, getHeight()-position.getY()/scale, 10, 10); //3 3
	}
	
	public void fillBackground(){
		mContext.setFill(Color.LIGHTSKYBLUE);
		mContext.fillRect(0,0,getWidth(),getHeight());
	}
}
