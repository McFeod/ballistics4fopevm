package com.github.remoteControl;

import com.example.client.R;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

	private OutputClient mOutputClient;
	private InputClient mInputClient;
	
	private TextView mConnectedTextView;
	private EditText mIPEditText;
	private Button mConnectButton;
	private Button mStartButton;
	private Button mStopButton;
	private Button mResetButton;
	
	private ImageView mBottomImageView;
	private ImageView mTopImageView;
	private Bitmap mBottomBitmap;
	private Bitmap mTopBitmap;
	private Canvas mBottomCanvas;
	private Canvas mTopCanvas;
	
	private Point mCenter;
	private int mLength = 250;
	private Paint mPaint;
	private Point mTouchPos;
	private Point mWindSpeed;

	private SeekBar mSleepSeekBar;
	private TextView mSleepTextView;
	private SeekBar mSpeedSeekBar;
	private TextView mSpeedTextView;
	private SeekBar mRadiusSeekBar;
	private TextView mRadiusTextView;
	private SeekBar mDensitySeekBar;
	private TextView mDensityTextView;

	private TextView mXWindSpeedTextView;
	private TextView mYWindSpeedTextView;
	private TextView mXSpeedTextView;
	private TextView mYSpeedTextView;
	private TextView mXTextView;
	private TextView mYTextView;
	private TextView mTimeTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		mConnectedTextView = (TextView)findViewById(R.id.connected_text);
		mXWindSpeedTextView = (TextView)findViewById(R.id.wind_speed_x_text_view);
		mYWindSpeedTextView = (TextView)findViewById(R.id.wind_speed_y_text_view);
		mBottomImageView = (ImageView)findViewById(R.id.bottom_image_view);
		mTopImageView = (ImageView)findViewById(R.id.top_image_view);
		mConnectButton = (Button)findViewById(R.id.IP_button);
		mIPEditText = (EditText)findViewById(R.id.IP_edit);
		mStartButton = (Button)findViewById(R.id.start_button);
		mStopButton = (Button)findViewById(R.id.stop_button);
		mResetButton = (Button)findViewById(R.id.reset_button);

		mXSpeedTextView = (TextView)findViewById(R.id.speed_x_text_view);
		mYSpeedTextView = (TextView)findViewById(R.id.speed_y_text_view);
		mXTextView = (TextView)findViewById(R.id.x_text_view);
		mYTextView = (TextView)findViewById(R.id.y_text_view);
		mTimeTextView = (TextView)findViewById(R.id.time_text_view);
		
		mSleepSeekBar = (SeekBar)findViewById(R.id.sleep_seek_bar);
		mSleepTextView = (TextView)findViewById(R.id.sleep_text_view);
		mSpeedSeekBar = (SeekBar)findViewById(R.id.speed_seek_bar);
		mSpeedTextView = (TextView)findViewById(R.id.speed_text_view);
		mDensitySeekBar = (SeekBar)findViewById(R.id.density_seek_bar);
		mDensityTextView = (TextView)findViewById(R.id.density_text_view);
		mRadiusSeekBar = (SeekBar)findViewById(R.id.radius_seek_bar);
		mRadiusTextView = (TextView)findViewById(R.id.radius_text_view);
		
		mOutputClient = new OutputClient(this);
		mInputClient = new InputClient(this);
		
		//соотношение времени
		mSleepSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mSleepTextView.setText(String.format(getString(R.string.sleep),
						progress/1000.0+0.001));
				mOutputClient.sendSleep(progress/1000.0+0.001);
			}
		});
		//скорость снаряда
		mSpeedSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mSpeedTextView.setText(String.format(getString(R.string.start_speed),
						progress+100));
				mOutputClient.sendStartSpeed(progress+100);
			}
		});
		//плотность снаряда
		mDensitySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mDensityTextView.setText(String.format(getString(R.string.density),
						progress+4000));
				mOutputClient.sendDensity(progress+4000);
			}
		});
		//радиус снаряда
		mRadiusSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mRadiusTextView.setText(String.format(getString(R.string.radius),
						progress/100.0+0.05));
				mOutputClient.sendRadius(progress/100.0+0.05);
			}
		});
		//start
		mStartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mOutputClient.sendStart();
			}
		});
		//stop
		mStopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mOutputClient.sendStop();
			}
		});
		//reset
		mResetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mOutputClient.sendReset();
			}
		});
		//connect
		mConnectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mOutputClient.setIP(mIPEditText.getText().toString());
				mOutputClient.start();
				mInputClient.setIP(mIPEditText.getText().toString());
				mInputClient.start();
			}
		});

		mBottomBitmap = Bitmap.createBitmap(mLength, mLength, Bitmap.Config.ARGB_8888);
		mBottomCanvas = new Canvas(mBottomBitmap);
		mBottomCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		mPaint = new Paint();
		mPaint.setColor(Color.GREEN);
		mPaint.setStyle(Paint.Style.STROKE);
		RectF oval = new RectF();
		for (int i=0; i<mLength/2; i+=10){
			oval.set(1+i, 1+i, mLength-(1+i), mLength-(1+i));
			mBottomCanvas.drawOval(oval, mPaint);
		}
		mBottomImageView.setImageBitmap(mBottomBitmap);


		mTopBitmap = Bitmap.createBitmap(mLength, mLength, Bitmap.Config.ARGB_8888);
		mTopCanvas = new Canvas(mTopBitmap);
		mTopCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		mPaint.setColor(Color.GREEN);
		mPaint.setStrokeWidth(3);
		mCenter = new Point(mLength/2, mLength/2);
		mTopCanvas.drawLine(mCenter.x, mCenter.y, 100, 100, mPaint);
		mTopImageView.setImageBitmap(mTopBitmap);

		mTopImageView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE)
					mTouchPos.set((int)event.getX(), (int)event.getY());
					mWindSpeed.set((mTouchPos.x-mCenter.x), (mCenter.y-mTouchPos.y));
					refresh();
					mOutputClient.sendWindSpeed(mWindSpeed);
				return true;
			}
		});

		mWindSpeed = new Point(10, 10);
		mTouchPos = new Point(mWindSpeed.x-mCenter.x, mCenter.y-mWindSpeed.y);
		refresh();
	}

	public void refresh(){
		mTopCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		mTopCanvas.drawLine(mCenter.x, mCenter.y, mTouchPos.x, mTouchPos.y, mPaint);
		mTopImageView.setImageBitmap(mTopBitmap);
		mXWindSpeedTextView.setText(String.format(getString(R.string.wind_speed_x),
				mWindSpeed.x));
		mYWindSpeedTextView.setText(String.format(getString(R.string.wind_speed_y),
				mWindSpeed.y));
	}

	Runnable connected = new Runnable() {
		@Override
		public void run() {
			mConnectedTextView.setText(getString(R.string.connected));
			mConnectedTextView.setTextColor(Color.GREEN);
		}
	};
	
	Runnable refreshObjects = new Runnable() {
		@Override
		public void run() {
			mXSpeedTextView.setText(String.format(getString(R.string.speed_x),
					mInputClient.speedX));
			mYSpeedTextView.setText(String.format(getString(R.string.speed_y),
					mInputClient.speedY));
			mXTextView.setText(String.format(getString(R.string.x),
					mInputClient.x));
			mYTextView.setText(String.format(getString(R.string.speed_y),
					mInputClient.y));
			mTimeTextView.setText(String.format(getString(R.string.time),
					mInputClient.time));
		}
	};

	//<костыли>
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_MENU)
	        return true;
	    return super.onKeyDown(keyCode, event);
	}
	//</костыли>
}
