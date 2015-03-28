import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;


public class WindPicker extends Canvas {

	private GraphicsContext mContext;
	private Point2D mCenter;
	private Label xWindSpeedLabel, yWindSpeedLabel;
	private Packet mPacket;
	
	public WindPicker(int length, Packet packet, Label xWindSpeedLabel, Label yWindSpeedLabel,
			GraphicsContext context) {
		super(length, length);
		this.xWindSpeedLabel = xWindSpeedLabel;
		this.yWindSpeedLabel = yWindSpeedLabel;
		mPacket = packet;
		
		mContext = getGraphicsContext2D();
		mContext.setStroke(Color.LIGHTGREEN);
		mContext.setLineWidth(3);
		mCenter = new Point2D(getWidth()/2, getHeight()/2);

		context.setStroke(Color.GREEN);
		for (int i=0; i<getWidth()/2; i+=10)
			context.strokeOval(1+i, 1+i, getWidth()-2-i*2, getHeight()-2-i*2);
		
		drawLine();
		refreshText();
		this.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				mPacket.setWindSpeed(new Point2D((arg0.getX() - mCenter.getX())*10,
						(mCenter.getY() - arg0.getY())*10));
				refreshText();
				drawLine();
			}
		});
	}
	
	public void drawLine(){
		mContext.clearRect(0, 0, getWidth(), getHeight());
		mContext.strokeLine(mCenter.getX(), mCenter.getY(), mPacket.mWindSpeed.getX()/10 + mCenter.getX(),
				mCenter.getY() - mPacket.mWindSpeed.getY()/10);
	}
	
	public void refreshText(){
		xWindSpeedLabel.setText(String.format("WindSpeedX = %.0f м/с", mPacket.getWindSpeed().getX()));
		yWindSpeedLabel.setText(String.format("WindSpeedY = %.0f м/с", mPacket.getWindSpeed().getY()));
	}
	
}
