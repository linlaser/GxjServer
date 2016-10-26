package com.ywangwang.gxjserver;

import com.ywangwang.gxjserver.update.UpdateManager;
import com.ywangwang.gxjserver.update.UpdateManager.onStatusChangeListener;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;

public class GxjService extends Service {
	private static final String TAG = "GxjService";
	private static final String BROADCAST_GXJ_MAIN_ACTIVITY_ACTION = "com.ywangwang.gxj.MainActivity.broadcast";
	private static final String BROADCAST_GXJ_SERVER_GXJ_SERVER_ACTION = "com.ywangwang.gxjserver.GxjService.broadcast";
	private static final String BROADCAST_HAVE_NEW_GXJ_VERSION = "HAVE_NEW_VERSION";
	private static final String BROADCAST_UPDATE_GXJ = "UPDATE_GXJ";
	private static final String BROADCAST_UPDATE_GXJ_CHECK_NOW = "UPDATE_GXJ_CHECK_NOW";
	private static final String BROADCAST_UPDATE_GXJ_SERVER_CHECK_NOW = "UPDATE_GXJ_SERVER_CHECK_NOW";

	private static final String GXJ_PACKAGE_NAME = "com.ywangwang.gxj";

	UpdateManager updateManagerGxj, updateManagerGxjServer;
	final boolean FLAG_GXJ = true;
	final boolean FLAG_GXJ_SERVER = false;
	boolean updateFlag = FLAG_GXJ;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		registerReceiver(broadcastReceiver, new IntentFilter(BROADCAST_GXJ_SERVER_GXJ_SERVER_ACTION));
		updateManagerGxj = new UpdateManager(this, "http://192.168.0.123:88/update/gxjUpdate.xml", GXJ_PACKAGE_NAME, null);
		updateManagerGxj.setCheckMD5(false);
		updateManagerGxjServer = new UpdateManager(this, "http://192.168.0.123:88/update/gxjServerUpdate.xml", null, null);
		updateManagerGxjServer.setCheckMD5(false);
		updateManagerGxj.setOnStatusChangeListener(statusChangeListener);
		handlerUpdate.post(runnableUpdate);
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		unregisterReceiver(broadcastReceiver);
		handlerUpdate.removeCallbacks(runnableUpdate);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand()");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "onBind()");
		return null;
	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getExtras().getBoolean(BROADCAST_UPDATE_GXJ, false)) {
				if (updateManagerGxj.getStatus() == UpdateManager.DOWNLOADED) {
					updateManagerGxj.install();
				}
			} else if (intent.getExtras().getBoolean(BROADCAST_UPDATE_GXJ_CHECK_NOW, false)) {
				updateGxj();
			} else if (intent.getExtras().getBoolean(BROADCAST_UPDATE_GXJ_SERVER_CHECK_NOW, false)) {
				updateGxjServer();
			}
		}
	};
	onStatusChangeListener statusChangeListener = new onStatusChangeListener() {
		@Override
		public void onChange(int status) {
			Log.d(TAG, status + "");
			switch (status) {
			case UpdateManager.NO_UPDATE_APK:
				break;
			case UpdateManager.UPDATE_APK:
				break;
			case UpdateManager.GET_UNDATEINFO_ERROR:
				break;
			case UpdateManager.INSTALL_APK:
				break;
			case UpdateManager.INSTALL_APK_SUCCESS:
				break;
			case UpdateManager.INSTALL_APK_FAIL:
				break;
			case UpdateManager.DOWNLOAD_SUCCESS:
				sendBroadcast(new Intent(BROADCAST_GXJ_MAIN_ACTIVITY_ACTION).putExtra(BROADCAST_HAVE_NEW_GXJ_VERSION, true));
				break;
			case UpdateManager.DOWNLOAD_ERROR:
				break;
			}
		}
	};

	void updateGxj() {
		if (updateManagerGxj.getStatus() == UpdateManager.STANDBY || updateManagerGxj.getStatus() == UpdateManager.DOWNLOADED) {
			updateManagerGxj.setServiceMode(true);
			updateManagerGxj.setCancelable(true);
			// updateManagerGxj.setShowDialog(false);
			// updateManagerGxj.setAutoDownload(true);
			updateManagerGxj.setBackgroundDownload(true);
			// updateManagerGxj.setAutoInstall(true);
			updateManagerGxj.setInstallApkOnBackground(true);
			updateManagerGxj.update();
		}
	}

	void updateGxjServer() {
		if (updateManagerGxjServer.getStatus() == UpdateManager.STANDBY || updateManagerGxjServer.getStatus() == UpdateManager.DOWNLOADED) {
			// updateManagerGxjServer.setServiceMode(true);
			// updateManagerGxjServer.setShowDialog(false);
			// updateManagerGxjServer.setInstallApkOnBackground(true);
			// updateManagerGxjServer.setAutoInstall(true);

			// TEST******************************************************<<<<<<<<<<<<<<<<<<<<<<<<
			updateManagerGxjServer.setServiceMode(true);
			// updateManagerGxjServer.setCancelable(true);
			// updateManagerGxjServer.setShowDialog(false);
			// updateManagerGxjServer.setAutoDownload(true);
			updateManagerGxjServer.setBackgroundDownload(true);
			updateManagerGxjServer.setAutoInstall(true);
			updateManagerGxjServer.setInstallApkOnBackground(true);
			// TEST******************************************************>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

			updateManagerGxjServer.update();
		}
	}

	Handler handlerUpdate = new Handler(); // 更新时间Handler
	Runnable runnableUpdate = new Runnable() {
		@Override
		public void run() {
			long delayTime = 2 * 60 * 60 * 1000L;
			handlerUpdate.removeCallbacks(runnableUpdate);
			if (DateAndTime.isNetworkAvailable(GxjService.this)) {
				if (updateFlag == FLAG_GXJ) {
					updateGxj();
				} else {
					updateGxjServer();
				}
				updateFlag = !updateFlag;
			} else {
				delayTime = 2 * 60 * 1000L;
			}
			handlerUpdate.postAtTime(runnableUpdate, System.currentTimeMillis() + delayTime);
			Log.d(TAG, "现在时间=" + DateFormat.format("yyyy年MM月dd日 EEEE HH:mm:ss", System.currentTimeMillis()) + "；定时更新时间=" + DateFormat.format("yyyy年MM月dd日 EEEE HH:mm:ss", System.currentTimeMillis() + delayTime));
		}
	}; // 更新时间线程
}
