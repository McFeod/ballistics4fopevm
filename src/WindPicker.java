import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class WindPicker extends Canvas {

	private GraphicsContext mContext;
	private Point2D mCenter;
	private Label xWindSpeedLabel, yWindSpeedLabel;
	private Point2D mValue;  // Чтобы не возникал соблазн баловаться с ветром :)

	public WindPicker(int length, Label xWindSpeedLabel, Label yWindSpeedLabel,
	                  GraphicsContext context, Point2D initialValue) {
		super(length, length);
		this.xWindSpeedLabel = xWindSpeedLabel;
		this.yWindSpeedLabel = yWindSpeedLabel;

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
			if (!VisualizationThread.isRunning){
				mValue = new Point2D((mouseEvent.getX() - mCenter.getX()),
						(mCenter.getY() - mouseEvent.getY()));
				refreshText();
				drawLine();
			}
		});
	}

	public void drawLine(){
		mContext.clearRect(0, 0, getWidth(), getHeight());
		mContext.strokeLine(mCenter.getX(), mCenter.getY(), mValue.getX() + mCenter.getX(),
				mCenter.getY() - mValue.getY());
	}

	public void refreshText(){
		xWindSpeedLabel.setText(String.format("WindSpeedX = %.0f м/с", mValue.getX()));
		yWindSpeedLabel.setText(String.format("WindSpeedY = %.0f м/с", mValue.getY()));
	}

	public Point2D getValue() {
		return mValue;
	}

	public void setValue(Point2D value) {
		mValue = value;
	}
}