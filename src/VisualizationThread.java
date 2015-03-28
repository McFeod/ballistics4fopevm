import javafx.application.Platform;
import javafx.scene.control.Button;



/**
 * Поток, в котором раз в mSleepTime мс вызывается mView.run()
 */
class VisualizationThread extends Thread {
	// #4
	public static final boolean TEST_RUN = false;
	public static boolean targetReached = false;
	public static boolean isRunning = false;

	private MainView mView;
	private Packet mPacket;
	private Button mRefresher;

	@Override
	public void run() {
		isRunning = true;
		mPacket.setupMarkers(MainView.PACKET_GAGE * mView.getScale() * 0.7); //#1
		Marksman marksman = new Marksman(mPacket, mView.getScale());
		AngleChoice currentAngle = marksman.getAngle();

		while (currentAngle != null){
			mView.reset(currentAngle.mAngle);

			// промежуточные шаги отрисовываются в зависимости от режима
			if (singleFly(mView.isAngleBisectionEnabled())){
				break;  // попадание
			}
			// sleep between launches
			if (mView.isAngleBisectionEnabled()) {
				try { Thread.sleep(200); }
				catch (Exception ignore) {}
			}
			currentAngle = marksman.selectNewAngle(mPacket.getSummarize());
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
			if (targetReached){
				return true;  // прекрщаем считать: попали
			}
		}
		return false;
	}

	public void start(MainView view, Button refresher){
		mView = view;
		mPacket = view.getPacket();
		mRefresher = refresher;

		setDaemon(true); // lazy & dangerous(for IO) way to stop a thread when closing app
		start();
	}
}
