import javafx.application.Platform;
import javafx.scene.control.Button;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
class VisualizationThread extends Thread {
	// #4
	public static final boolean TEST_RUN = false;

	private MainView mView;
	private Button mRefresher;
	private AngleChoice mCurrentChoice;
	private Queue<AngleChoice> mChoices;

	public static boolean targetReached = false;
	public static boolean isRunning = false;
	private final double DEGREE = Math.PI/180;

	public void start(MainView view, Button refresher){
		mView = view;
		mRefresher = refresher;
		mChoices = new ArrayDeque<>();
		mCurrentChoice = new AngleChoice(0.0, Math.PI/2,
				Math.atan(mView.getPacket().getSpeed().getY()/mView.getPacket().getSpeed().getX())
				,true, DEGREE/mView.getScale());
		setDaemon(true); // lazy & dangerous(for IO) way to stop a thread when closing app
		start();
	}
	
	@Override
	public void run() {
		isRunning = true;
		mView.getPacket().setupMarkers(MainView.PACKET_GAGE * mView.getScale() * 0.7); //#1

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
			Boolean result = mView.getPacket().getSummarize();
			if (result==null){
				mChoices.add(mCurrentChoice.getAnother(mView.getPacket().helpToChoose()));  // подстраховка в неявном случае
			} else {
				mCurrentChoice.next(result);  // меняем угол
			}
			if (!mChoices.isEmpty()&&(!mCurrentChoice.isMatter())){
				mCurrentChoice = mChoices.poll();  // запасы пригодились
			}
		}

		if (!mView.isAngleBisectionEnabled()){
			targetReached = false;
			mView.reset(mCurrentChoice.getAngle()); // найденный заранее угол
			singleFly(true);  // повторяем последний полёт
		}

		Platform.runLater(() -> {  // перевод фокуса на кнопку - пашет не всегда O_o
			mRefresher.setDisable(false);
			mRefresher.requestFocus();
		});
		isRunning = false;
	}

	/**
	 * Рассчёт одной траектории
	 * @param draw - включение/отключение отрисовки
	 * @return true при достижении цели, false при падении
	 */
	private boolean singleFly(boolean draw){
		while (mView.getPacket().inTheAir()) {
			mView.getPacket().update(draw);
			if (draw) {
				try {
					Thread.sleep(mView.someUpdates());  // основной логический шаг с отрисовкой
				} catch (Exception ignore) {}
				Platform.runLater(mView);
			}else{
				mView.getPacket().update(false);  // без отрисовки
			}
			if (targetReached){
				return true;  // прекрщаем считать: попали
			}
		}
		return false;
	}
}
