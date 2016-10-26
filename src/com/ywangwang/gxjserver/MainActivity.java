package com.ywangwang.gxjserver;

import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static final String HOME_URL = "http://www.ywangwang.com";

	Handler handlerUpdateTimeAndDateDisplay = new Handler();
	Runnable runnableUpdateTimeAndDateDisplay;
	TextView tvNowTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tvNowTime = (TextView) findViewById(R.id.tvNowTime);
		findViewById(R.id.btnExit).setOnClickListener(ButtonListener);
		findViewById(R.id.button1).setOnClickListener(ButtonListener);
		findViewById(R.id.button2).setOnClickListener(ButtonListener);
		findViewById(R.id.button3).setOnClickListener(ButtonListener);

		runnableUpdateTimeAndDateDisplay = new Runnable() {
			@Override
			public void run() {
				handlerUpdateTimeAndDateDisplay.removeCallbacks(runnableUpdateTimeAndDateDisplay);
				handlerUpdateTimeAndDateDisplay.postDelayed(runnableUpdateTimeAndDateDisplay, 1000);
				tvNowTime.setText(DateFormat.format("yyyyƒÍMM‘¬dd»’ EEEE HH:mm:ss", System.currentTimeMillis()));
				tvNowTime.append("  " + TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT));
			}
		};
		handlerUpdateTimeAndDateDisplay.post(runnableUpdateTimeAndDateDisplay);
	}

	OnClickListener ButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnExit:
				finish();
				break;
			case R.id.button1:
				DateAndTime.syncTime(MainActivity.this);
				break;
			case R.id.button2:
				PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
				pManager.reboot("");
				break;
			case R.id.button3:
				DateAndTime.setTimeZone(MainActivity.this, "GMT-08:00");
				DateAndTime.setDateAndTime(MainActivity.this, 2008, 07, 8, 8, 8, 8);
				break;
			default:
			}
		}
	};

}
