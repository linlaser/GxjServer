package com.ywangwang.gxjserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class BroadReceiver extends BroadcastReceiver {
	private static final String TAG = "BroadReceiver";

	// private static final String BROADCAST_GXJ_SERVER_BROAD_RECEIVER_ACTION = "com.ywangwang.gxjserver.BroadReceiver.broadcast";
	private static final String BROADCAST_UPDATE_SYSTEM_TIME = "UPDATE_SYSTEM_TIME";
	private static final String BROADCAST_REBOOT_SYSTEM = "REBOOT_SYSTEM";
	private static final String BROADCAST_SET_SYSTEM_TIME = "SET_SYSTEM_TIME";
	private static final String BROADCAST_SHOW_ACTIVITY = "SHOW_ACTIVITY";

	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(context, GxjService.class));
		if (intent.getExtras().getBoolean(BROADCAST_UPDATE_SYSTEM_TIME, false) == true) {
			Log.d(TAG, "UPDATE_SYSTEM_TIME");
			DateAndTime.syncTime(context);
		} else if (intent.getExtras().getBoolean(BROADCAST_REBOOT_SYSTEM, false) == true) {
			PowerManager pManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			pManager.reboot("");
		} else if (intent.getExtras().getLong(BROADCAST_SET_SYSTEM_TIME, 0L) != 0L) {
			DateAndTime.setDateAndTime(context, intent.getExtras().getLong(BROADCAST_SET_SYSTEM_TIME, 0L));

		} else if (intent.getExtras().getBoolean(BROADCAST_SHOW_ACTIVITY, false) == true) {
			Intent intent1 = new Intent();
			intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent1.setClass(context, MainActivity.class);
			context.startActivity(intent1);
		}
	}
}
