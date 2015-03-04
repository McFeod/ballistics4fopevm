import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Холст с методом отрисовки одного шага
 */
public class MainView extends Canvas implements Runnable {
	private GraphicsContext mContext;
	Packet mPacket;

	public MainView(int sizeX, int sizeY){
		super(sizeX, sizeY);

		mContext = getGraphicsContext2D();
		fillBackground();
		mPacket = new Packet(new Point2D(0.0, 0.0), new Point2D(1.0, 1.0));
	}

	@Override
	public void run() {
		mPacket.draw(mContext, Color.WHITESMOKE);
		mPacket.update(2.0);
		mPacket.draw(mContext, Color.BLACK);
	}

	public void fillBackground(){
		mContext.setFill(Color.LIGHTSKYBLUE);
		mContext.fillRect(0,0,getWidth(),getHeight());
	}
}
