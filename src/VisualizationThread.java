import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
class VisualizationThread extends Thread {
	// #4
	public static boolean mustDie = false;
	public static boolean targetReached = false;
	private final double DEGREE = Math.PI/180;
	private MainView mView;
	private Packet mPacket;
	private Node[] mLockedControls;
	private Node[] mUnlockedControls;
	private AngleChoice mCurrentChoice;
	private Queue<AngleChoice> mChoices;

	@Override
	public void run() {
		mPacket.setupMarkers(MainView.PACKET_GAGE * mView.getScale() * 0.7); //#1

		while (mCurrentChoice.isMatter()){
			mView.reset(mCurrentChoice.getAngle());

			// промежуточные шаги отрисовываются в зависимости от режима
			if (singleFly(mView.isAngleBisectionEnabled())){
				break;  // попадание
			}
			// sleep between launches
			if (mView.isAngleBisectionEnabled()) {
				try { Thread.sleep(200); }
				catch (Exception ignore) {}
			}
			//  получаем направление деления
			Boolean result = mPacket.getSummarize();
			if (result==null){
				mChoices.add(mCurrentChoice.getAnother(mPacket.helpToChoose()));  // подстраховка в неявном случае
			} else {
				mCurrentChoice.next(result);  // меняем угол
			}
			if (!mChoices.isEmpty()&&(!mCurrentChoice.isMatter())){
				mCurrentChoice = mChoices.poll();  // запасы пригодились
			}
		}

		if (!mView.isAngleBisectionEnabled()){
			mustDie = false;
			targetReached = false;
			mView.reset(mCurrentChoice.getAngle()); // найденный заранее угол
			singleFly(true);  // повторяем последний полёт
		}

		Platform.runLater(() -> showResults());

	}

	/**
	 * Рассчёт одной траектории
	 * @param draw - включение/отключение отрисовки
	 * @return true при достижении цели, false при падении
	 */
	private boolean singleFly(boolean draw){
		while (mPacket.inTheAir()) {
			mPacket.update(draw);
			if (draw) {
				try {
					Thread.sleep(mView.someUpdates());  // основной логический шаг с отрисовкой
				} catch (Exception ignore) {}
				Platform.runLater(mView);
			}else{
				mPacket.update(false);  // без отрисовки
			}
			if (mustDie){
				return true;  // прекрщаем считать
			}
		}
		return false;
	}

	private void showResults() {
		for(Node node: mLockedControls){
			node.setDisable(false);
		}
		mLockedControls[0].requestFocus();
		for(Node node: mUnlockedControls){
			node.setDisable(true);
		}
		String msg = "";
		if (targetReached){
			double buffer = Math.toDegrees(Math.atan(mCurrentChoice.getAngle()));
			byte deg = (byte) buffer;
			buffer-= deg; buffer*=60;
			byte min = (byte) buffer;
			buffer-= min; buffer*=60;
			byte sec = (byte) Math.round(buffer);
			msg = String.format("при начальном угле α = %dº %d' %d\"",
					deg, min, sec);
		}
		Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
		alert.setHeaderText(String.format("Цель %sдостигнута", targetReached?"":"не "));
		alert.setTitle("Результат запуска");
		alert.showAndWait();
	}

	public void start(MainView view, Node[] locked, Node[] unlocked){
		mView = view;
		mPacket = view.getPacket();
		mLockedControls = locked;
		mUnlockedControls = unlocked;
		mChoices = new ArrayDeque<>();
		mCurrentChoice = new AngleChoice(0.0, Math.PI/2,
				Math.atan(mPacket.getSpeed().getY()/(Math.abs(mPacket.getSpeed().getX())+1e-5))
				,true, DEGREE/mView.getScale());
		setDaemon(true); // lazy & dangerous(for IO) way to stop a thread when closing app
		start();
	}
}
