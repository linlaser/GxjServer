package com.ywangwang.gxjserver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class DateAndTime {
	public static void setDate(Context context, int year, int month, int day) {
		Calendar c = Calendar.getInstance();

		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		long when = c.getTimeInMillis();

		if (when / 1000 < Integer.MAX_VALUE) {
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
		}
	}

	public static void setTime(Context context, int hourOfDay, int minute) {
		setTime(context, hourOfDay, minute, 0);
	}

	public static void setTime(Context context, int hourOfDay, int minute, int second) {
		Calendar c = Calendar.getInstance();

		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);
		c.set(Calendar.MILLISECOND, 0);
		long when = c.getTimeInMillis();

		if (when / 1000 < Integer.MAX_VALUE) {
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
		}
	}

	public static void setDateAndTime(Context context, long when) {
		if (when / 1000 < Integer.MAX_VALUE) {
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
		}
	}

	public static void setDateAndTime(Context context, int year, int month, int day, int hourOfDay, int minute) {
		setDateAndTime(context, year, month, day, hourOfDay, minute, 0);
	}

	public static void setDateAndTime(Context context, int year, int month, int day, int hourOfDay, int minute, int second) {
		setDate(context, year, month, day);
		setTime(context, hourOfDay, minute, second);
	}

	public static void setTimeZone(Context context, String timeZone) {
		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTimeZone(timeZone);
	}

	public static long getWebsiteDatetime() {
		try {

			URLConnection uc1 = new URL("http://www.qq.com").openConnection();// 生成连接对象
			uc1.connect();// 发出连接
			long ld1 = uc1.getDate();// 读取网站日期时间

			URLConnection uc2 = new URL("http://www.tmall.com").openConnection();// 生成连接对象
			uc2.connect();// 发出连接
			long ld2 = uc2.getDate();// 读取网站日期时间

			if (Math.abs(ld1 - ld2) < 600000L) {
				return ld1;
			}
			URLConnection uc3 = new URL("http://www.ywangwang.com").openConnection();// 生成连接对象
			uc3.connect();// 发出连接
			long ld3 = uc3.getDate();// 读取网站日期时间
			if (Math.abs(ld1 - ld3) < 600000L) {
				return ld1;
			} else if (Math.abs(ld2 - ld3) < 600000L) {
				return ld2;
			}
			return 0;
		} catch (MalformedURLException e) {
			Log.e("MalformedURLException", "MalformedURLException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("IOException", "IOException");
			e.printStackTrace();
		}
		return 0;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityMgr = (ConnectivityManager) context.getSystemService("connectivity");
		NetworkInfo _networkInfo = connectivityMgr.getActiveNetworkInfo();
		if (_networkInfo == null || !_networkInfo.isAvailable() || !_networkInfo.isConnected()) {
			return false;
		} else {
			return true;
		}
	}

	private static boolean SyncingTime = false;

	public static void syncTime(final Context context) {
		if (SyncingTime) {
			return;
		}
		SyncingTime = true;
		if (TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT).equals("GMT+08:00") == false) {
			DateAndTime.setTimeZone(context, "GMT+08:00");
		}
		new Thread() {
			@Override
			public void run() {
				int times = 10;
				long time = 0;
				while (times-- > 0) {
					if (isNetworkAvailable(context) == true) {
						time = DateAndTime.getWebsiteDatetime();
						if (time > 0) {
							DateAndTime.setDateAndTime(context, time);
							break;
						}
					}
					try {
						Thread.sleep(1800);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				SyncingTime = false;
			}
		}.start();
	}
}
