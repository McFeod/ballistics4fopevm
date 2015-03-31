
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class OutputServer extends Thread{
	
	private int mPort = 6001;
	private ServerSocket mServerSocket;
	private Socket mSocket;
	private DataOutputStream mDataOutputStream;
	private boolean toSend = false;
	private double mSpeedX, mSpeedY, mX, mY, mTime;
	
	public OutputServer() {
		this.setDaemon(true);
		try{
			mServerSocket = new ServerSocket(mPort);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try{
			mSocket = mServerSocket.accept();
			mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
			while (true){
				try{
					Thread.sleep(50);
				}catch(InterruptedException e){}
				if (toSend){
					toSend = false;
					mDataOutputStream.writeUTF("settings");
					mDataOutputStream.writeDouble(mSpeedX);
					mDataOutputStream.writeDouble(mSpeedY);
					mDataOutputStream.writeDouble(mX);
					mDataOutputStream.writeDouble(mY);
					mDataOutputStream.writeDouble(mTime);
					mDataOutputStream.flush();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void send(double speedX, double speedY, double x, double y, double time){
		mSpeedX = speedX;
		mSpeedY = speedY;
		mX = x;
		mY = y;
		mTime = time;
		toSend = true;
	}
	
}
