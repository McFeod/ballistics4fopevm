package com.github.remoteControl;

import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.Socket;

public class InputClient extends Thread{
	
	private int mPort = 6001;
	private String mIP = null;
	private Socket mSocket;
	private DataInputStream mDataInputStream;
	private MainActivity mMainActivity;

	public double speedX, speedY, x, y, time;

	public InputClient(MainActivity main) {
		mMainActivity = main;
		this.setDaemon(true);
	}
	
	public void setIP(String IP){
		mIP = IP;
	}
	
	@Override
	public void run() {
		try{
			mSocket = new Socket(InetAddress.getByName(mIP), mPort);
			mDataInputStream = new DataInputStream(mSocket.getInputStream());
			while (true){
				String input = mDataInputStream.readUTF();
				if (input.equals("settings")){
					speedX = mDataInputStream.readDouble();
					speedY = mDataInputStream.readDouble();
					x = mDataInputStream.readDouble();
					y = mDataInputStream.readDouble();
					time = mDataInputStream.readDouble();
					mMainActivity.runOnUiThread(mMainActivity.refreshObjects);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}