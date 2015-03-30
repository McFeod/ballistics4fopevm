import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;


public class WindPicker extends Canvas {

	private GraphicsContext mContext;
	private Point2D mCenter;
	private Label windSpeedLabel;
	private Point2D mValue;  // Чтобы не возникал соблазн баловаться с ветром :)
	
	public WindPicker(int length, MainForm root, // не хотелось возиться с observer
			GraphicsContext context, Point2D initialValue) {
		super(length, length);
		this.windSpeedLabel = root.windSpeedLabel;

		mValue = initialValue;
		mContext = getGraphicsContext2D();
		mContext.setStroke(Color.LIGHTGREEN);
		mContext.setLineWidth(3);
		mCenter = new Point2D(getWidth()/2, getHeight()/2);

		context.setStroke(Color.GREEN);
		for (int i=0; i<getWidth()/2; i+=10)
			context.strokeOval(1+i, 1+i, getWidth()-2-i*2, getHeight()-2-i*2);
		
		drawLine();
		refreshText();
		this.setOnMouseClicked(mouseEvent -> {
			mValue = new Point2D((mouseEvent.getX() - mCenter.getX()),
					(mCenter.getY() - mouseEvent.getY()));
			refreshText();
			drawLine();
			root.updateSettings();
			root.buildScales();
		});
	}
	
	public void drawLine(){
		mContext.clearRect(0, 0, getWidth(), getHeight());
		mContext.strokeLine(mCenter.getX(), mCenter.getY(), mValue.getX() + mCenter.getX(),
				mCenter.getY() - mValue.getY());
	}
	
	public void refreshText(){
		windSpeedLabel.setText(String.format("Скорость ветра: %.2f м/с",
				mValue.magnitude()));
	}

	public Point2D getValue() {
		return mValue;
	}

	public void setValue(Point2D value) {
		mValue = value;
	}
}
