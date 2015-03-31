package com.github.remoteControl;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.graphics.Point;

public class OutputClient extends Thread{
	
	private int mPort = 6000;
	private String mIP = null;
	private Socket mSocket;
	private DataOutputStream mDataOutputStream;
	private MainActivity mMainActivity;
	
	private boolean toSendStart = false;
	private boolean toSendStop = false;
	private boolean toSendReset = false;
	private boolean toSendSpeed = false;
	private Point mWindSpeed;
	private boolean toSendSleep = false;
	private double mSleep;
	private boolean toSendStartSpeed = false;
	private int mStartSpeed;
	private boolean toSendRadius = false;
	private double mRadius;
	private boolean toSendDensity = false;
	private int mDensity;

	public OutputClient(MainActivity main) {
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
			mMainActivity.runOnUiThread(mMainActivity.connected);
			mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
			while (true){
				try{
					Thread.sleep(50);
				}catch(InterruptedException e){}
				if (toSendStart){
					toSendStart = false;
					mDataOutputStream.writeUTF("start");
					mDataOutputStream.flush();
				}
				if (toSendStop){
					toSendStop = false;
					mDataOutputStream.writeUTF("stop");
					mDataOutputStream.flush();
				}
				if (toSendReset){
					toSendReset = false;
					mDataOutputStream.writeUTF("reset");
					mDataOutputStream.flush();
				}
				if (toSendSpeed){
					toSendSpeed = false;
					mDataOutputStream.writeUTF("windSpeed");
					mDataOutputStream.writeInt(mWindSpeed.x);
					mDataOutputStream.writeInt(mWindSpeed.y);
					mDataOutputStream.flush();
				}
				if (toSendSleep){
					toSendSleep = false;
					mDataOutputStream.writeUTF("sleep");
					mDataOutputStream.writeDouble(mSleep);
					mDataOutputStream.flush();
				}
				if (toSendStartSpeed){
					toSendStartSpeed = false;
					mDataOutputStream.writeUTF("startSpeed");
					mDataOutputStream.writeInt(mStartSpeed);
					mDataOutputStream.flush();
				}
				if (toSendRadius){
					toSendRadius = false;
					mDataOutputStream.writeUTF("radius");
					mDataOutputStream.writeDouble(mRadius);
					mDataOutputStream.flush();
				}
				if (toSendDensity){
					toSendDensity = false;
					mDataOutputStream.writeUTF("density");
					mDataOutputStream.writeInt(mDensity);
					mDataOutputStream.flush();
				}

			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void sendStop(){
		toSendStop = true;
	}
	
	public void sendReset(){
		toSendReset = true;
	}

	public void sendStart(){
		toSendStart = true;
	}
	
	public void sendWindSpeed(Point speed){
		mWindSpeed = speed;
		toSendSpeed = true;
	}
	
	public void sendSleep(double sleep){
		mSleep = sleep;
		toSendSleep = true;
	}
	
	public void sendStartSpeed(int speed){
		mStartSpeed = speed;
		toSendStartSpeed = true;
	}
	
	public void sendRadius(double radius){
		mRadius = radius;
		toSendRadius = true;
	}
	
	public void sendDensity(int density){
		mDensity = density;
		toSendDensity = true;
	}

}
