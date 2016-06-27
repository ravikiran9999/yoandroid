//package com.yo.android.voip;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.BitmapShader;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Shader.TileMode;
//import android.graphics.Typeface;
//import android.media.MediaPlayer;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.provider.Settings;
//import android.support.v7.app.ActionBar.LayoutParams;
//import android.util.Log;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.WindowManager;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//
//import org.greenrobot.eventbus.EventBus;
//
//import java.sql.SQLException;
//
//
//public class IncomingCall extends Activity implements OnClickListener {
//
//	private EventBus bus = EventBus.getDefault();
//	SipCallModel callModel = null;
//
//	private SlideToUnlock slidetounlock;
//	private SlideToUnlock2 slidetounlock2;
//	int width, height;
//	ImageView callerImage, callDisconnect;
//	TextView speaker, message, mic, timer;
//	TextView callerName, callType;
//	RelativeLayout middleLayout, bottomLayout;
//	LinearLayout callRecvLayout, callAcceptedLayout;
//	ImageButton msg;
//	private Handler handler;
//	int sec = 0, min = 0, hr = 0;
//	MediaPlayer player;
//
//	public static final int NOEVENT = 0;
//	public static final int MUTE_ON = 1;
//	public static final int MUTE_OFF = 2;
//	public static final int SPEAKER_ON = 3;
//	public static final int SPEAKER_OFF = 4;
//	public static final int CALL_ACCEPTED_START_TIMER = 10;
//
//	//Database
//	private MagazineDatabaseHelper databaseHelper = null;
//	CallLogsModel log;
//	private List<CallLogsModel> logList;
//
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.incomingcall);
//
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//
//		callModel = new SipCallModel();
//		log = new CallLogsModel();
//		log.callerName = "Nitin Pol";
//		log.callTime = System.currentTimeMillis() / 1000L;
//
//		initialise();
//
//		if (getIntent().getStringExtra("CallNo") != null) {
//
//			callerName.setText(getIntent().getStringExtra("CallNo"));
//			timer.setText("Outgoing Call...");
//			changeToSecondLayout();
//			Intent broadcastIntent = new Intent();
//			broadcastIntent.setAction("android.yo.OUTGOING_CALL");
//			Bundle callBundle = new Bundle();
//			callBundle.putString("callerNo", getIntent().getStringExtra("CallNo"));
//			broadcastIntent.putExtras(callBundle);
//			sendBroadcast(broadcastIntent);
//			callModel.setOnCall(true);
//
//			log.callerNo = getIntent().getStringExtra("CallNo");
//			log.callType = DatabaseConstant.callType_Outgoing;
//			log.callMode = DatabaseConstant.callMode_Voip;
//
//		} else {
//			callerName.setText(getIntent().getStringExtra("caller"));
//
//			log.callerNo = getIntent().getStringExtra("caller");
//			log.callType = DatabaseConstant.callType_Missed;
//			log.callMode = DatabaseConstant.callMode_Voip;
//
//			slidetounlock.setOnUnlockListener(new SlideToUnlock.OnUnlockListener() {
//
//				@Override
//				public void onUnlock() {
//					// TODO Auto-generated method stub
//					onCallAccepted();
//				}
//			});
//
//			slidetounlock2.setOnUnlockListener(new SlideToUnlock2.OnUnlockListener() {
//
//				@Override
//				public void onUnlock() {
//					// TODO Auto-generated method stub
//					// Toast.makeText(IncomingCall.this, "Call Rejected",
//					// Toast.LENGTH_SHORT).show();
//					log.callType = DatabaseConstant.callType_Incoming;
//
//					//Log.d("BUS", "SIDETOUNLOCK2");
//					callModel.setOnCall(false);
//					bus.post(callModel);
//				}
//			});
//			player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
//			player.start();
//		}
//
//		callDisconnect.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				callModel.setOnCall(false);
//				bus.post(callModel);
//				//Log.d("BUS", "CALL DISCONNECT");
//				finish();
//			}
//		});
//
//		callerImage = (ImageView) findViewById(R.id.CallerImage);
//		Bitmap img = BitmapFactory.decodeResource(this.getResources(), R.drawable.yo_magazin_1);
//		callerImage.setImageBitmap(getCircularBitmapWithWhiteBorder(img, 2));
//
//		Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
//		speaker = (TextView) findViewById(R.id.incomingcall_speaker);
//		message = (TextView) findViewById(R.id.incomingcall_inCallMessage);
//		mic = (TextView) findViewById(R.id.incomingcall_mic);
//		speaker.setTypeface(font);
//		message.setTypeface(font);
//		mic.setTypeface(font);
//		mic.setOnClickListener(this);
//		message.setOnClickListener(this);
//		speaker.setOnClickListener(this);
//	}
//
//	private void initialise() {
//		// TODO Auto-generated method stub
//		middleLayout = (RelativeLayout) findViewById(R.id.Incomingcall_middle_layout);
//		bottomLayout = (RelativeLayout) findViewById(R.id.Incomingcall_bottom_layout);
//		callRecvLayout = (LinearLayout) findViewById(R.id.IncomingCall_acceptReject_layout);
//		callAcceptedLayout = (LinearLayout) findViewById(R.id.IncomingCall_utils_layout);
//		callDisconnect = (ImageView) findViewById(R.id.IncomingCall_Reject);
//		msg = (ImageButton) findViewById(R.id.IncomingCall_message);
//		timer = (TextView) findViewById(R.id.Call_Title);
//
//		callerName = (TextView) findViewById(R.id.INCOMING_CALLER_NAME);
//		slidetounlock = (SlideToUnlock) findViewById(R.id.slidetounlock);
//		slidetounlock2 = (SlideToUnlock2) findViewById(R.id.slidetounlock2);
//	}
//
//
//	@Override
//	protected void onResume() {
//		// TODO Auto-generated method stub
//		if (!bus.isRegistered(this))
//			bus.register(this);
//		super.onResume();
//	}
//
//	public void onEvent(SipCallModel model) {
//		Log.d("INCOMING_CALL BUSS", "<><> INCOMING-CALL BUS CALLED <><>");
//		Log.d("<---- Model Event is ---->", Integer.toString(model.getEvent()));
//		if (!model.isOnCall()) {
//			Log.d("BUS", "FINISHING ACTIVITY");
//			try {
//				if (player != null && player.isPlaying()) {
//					player.stop();
//					player.release();
//				}
//			} catch (IllegalStateException ie) {
//				Log.e("IncomingCall/OnEvent()", "MediaPlayer already released");
//			}
//			///this.finish();
//		}
//		if(model.isOnCall() && model.getEvent()==CALL_ACCEPTED_START_TIMER)
//		{
//			Activity mActivity = IncomingCall.this;
//
//		    mActivity.runOnUiThread(new Runnable() {
//		     public void run() {
//		     startTimer();
//		     }
//		   });
//		}
//	}
//
//	public static Bitmap getCircularBitmapWithWhiteBorder(Bitmap bitmap, int borderWidth) {
//		if (bitmap == null || bitmap.isRecycled()) {
//			return null;
//		}
//
//		final int width = bitmap.getWidth() + borderWidth;
//		final int height = bitmap.getHeight() + borderWidth;
//
//		Bitmap canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//		BitmapShader shader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
//		Paint paint = new Paint();
//		paint.setAntiAlias(true);
//		paint.setShader(shader);
//
//		Canvas canvas = new Canvas(canvasBitmap);
//		float radius = width > height ? ((float) height) / 2f : ((float) width) / 2f;
//		canvas.drawCircle(width / 2, height / 2, radius, paint);
//		paint.setShader(null);
//		paint.setStyle(Paint.Style.STROKE);
//		paint.setColor(Color.WHITE);
//		paint.setStrokeWidth(borderWidth);
//		canvas.drawCircle(width / 2, height / 2, radius - borderWidth / 2, paint);
//		return canvasBitmap;
//	}
//
//	public void onCallAccepted() {
//
//		log.callType = DatabaseConstant.callType_Incoming;
//
//		player.stop();
//		player.release();
//		Log.d("BUS", "ONCALLACCEPTED");
//		callModel.setOnCall(true);
//		bus.post(callModel);
//		changeToSecondLayout();
//		startTimer();
//	}
//
//	private void startTimer() {
//		// TODO Auto-generated method stub
//		handler = new Handler(Looper.getMainLooper());
//
//		final Runnable r = new Runnable() {
//			public void run() {
//				sec++;
//				min += sec / 60;
//				hr += min / 60;
//				sec = sec % 60;
//				timer.setText(String.format("%02d:%02d:%02d", hr, min, sec));
//				handler.postDelayed(this, 1000);
//			}
//		};
//		handler.postDelayed(r, 1000);
//
//	}
//
//	private void changeToSecondLayout() {
//		// TODO Auto-generated method stub
//		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0, 8f);
//		LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0, 1f);
//		middleLayout.setLayoutParams(layoutParams);
//		bottomLayout.setLayoutParams(layoutParams2);
//		callRecvLayout.setVisibility(View.GONE);
//		callAcceptedLayout.setVisibility(View.VISIBLE);
//		msg.setVisibility(View.GONE);
//		callDisconnect.setVisibility(View.VISIBLE);
//	}
//
//	@Override
//	public void onClick(View v) {
//		// TODO Auto-generated method stub
//
//		switch (v.getId()) {
//		case R.id.incomingcall_mic:
//			if (mic.getText().equals(getResources().getString(R.string.mic_on))) {
//				mic.setText(R.string.mic_off);
//				// callModel.setMute(true);
//				callModel.setEvent(MUTE_ON);
//			} else {
//				mic.setText(R.string.mic_on);
//				// callModel.setMute(false);
//				callModel.setEvent(MUTE_OFF);
//			}
//			bus.post(callModel);
//			break;
//		case R.id.incomingcall_speaker:
//			if (speaker.getText().equals(getResources().getString(R.string.volume_down))) {
//				speaker.setText(R.string.volume_up);
//				// callModel.setSpeaker(true);
//				callModel.setEvent(SPEAKER_ON);
//			} else {
//				speaker.setText(R.string.volume_down);
//				// callModel.setSpeaker(false);
//				callModel.setEvent(SPEAKER_OFF);
//			}
//			bus.post(callModel);
//			break;
//		case R.id.IncomingCall_message:
//			break;
//		}
//	}
//
//	@Override
//	protected void onPause() {
//		// TODO Auto-generated method stub
//		try {
//			bus.unregister(this);
//			if (player.isPlaying()) {
//				player.stop();
//				player.release();
//			}
//		} catch (Exception e) {
//			Log.d("IncomingCall.OnPause()", e.toString());
//		}
//		super.onPause();
//	}
//
//	@Override
//	protected void onDestroy() {
//		// TODO Auto-generated method stub
//		super.onDestroy();
//		if (databaseHelper != null) {
//			OpenHelperManager.releaseHelper();
//			databaseHelper = null;
//		}
//	}
//
//	private MagazineDatabaseHelper getHelper() {
//		if (databaseHelper == null) {
//			databaseHelper = OpenHelperManager.getHelper(this,MagazineDatabaseHelper.class);
//		}
//		return databaseHelper;
//	}
//
//	@Override
//	protected void onStop() {
//		// TODO Auto-generated method stub
//		super.onStop();
//
//		try {
//			final Dao<CallLogsModel,Integer> logDao = getHelper().getCallLogDao();
//			logDao.create(log);
//			//Log.d("IncomingCall/CallLog", "LOGGED SUCCESSFULLY");
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		// Temporary Code to check output
//
//		Dao<CallLogsModel, Integer> logDao;
//		try {
//			logDao = getHelper().getCallLogDao();
//			logList = logDao.queryForAll();
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//		/*if(logList==null)
//			return;
//		for(CallLogsModel m:logList)
//		{
//			Log.d("Records", "Name: " + m.callerName + "No: " +m.callerNo + "type: " + m.callType + "mode: " + m.callMode);
//		}*/
//	}
//}
