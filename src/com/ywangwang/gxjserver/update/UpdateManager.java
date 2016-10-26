package com.ywangwang.gxjserver.update;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

@SuppressLint("HandlerLeak")
public class UpdateManager {
	private String TAG = "UpdateManager";

	private UpdateInfo info;
	File file;
	private Context context;
	// 后台服务模式
	private boolean isServiceMode = false;
	// 显示对话框
	private boolean isShowDialog = true;
	// 下载完成后自动安装
	private boolean isAutoInstall = false;
	// 后台安装
	private boolean isInstallApkOnBackground = false;
	// 自动下载
	private boolean isAutoDownload = false;
	// 后台下载
	private boolean isBackgroundDownload = false;
	// 新版本APP下载完成
	// private boolean isDownloadSuccess = false;
	// 可取消
	private boolean isCancelable = true;
	// 下载完成后校验MD5
	private boolean isCheckMD5 = true;

	// 完成/等待
	public static final int STANDBY = 0;
	// 正在检查版本
	public static final int CHECKING_VERSION = 1;
	// 有新版本
	public static final int HAVE_NEW_VERSION = 2;
	// 正在下载
	public static final int DOWNLOADING = 3;
	// 下载完成
	public static final int DOWNLOADED = 4;
	// 安装中
	public static final int INSTALLING = 5;
	// 状态
	private int status = 0;

	// 无需更新
	public static final int NO_UPDATE_APK = 1;
	// 需要更新
	public static final int UPDATE_APK = 2;
	// 获取xml失败
	public static final int GET_UNDATEINFO_ERROR = 3;
	// 下载失败
	public static final int DOWNLOAD_ERROR = 4;
	// 下载成功
	public static final int DOWNLOAD_SUCCESS = 5;
	// 安装APK
	public static final int INSTALL_APK = 6;
	// 安装成功
	public static final int INSTALL_APK_SUCCESS = 7;
	// 安装失败
	public static final int INSTALL_APK_FAIL = 8;

	// APP包名
	private String appPackageName = null;
	// xml文件URL
	private String xmlFileUrl = null;
	// APP的versionCode
	private int versionCode = -1;
	// 下载文件保存名称
	private String fileName = null;

	public UpdateManager(Context context, String xmlFileUrl) {
		this(context, xmlFileUrl, null, null);
	}

	public UpdateManager(Context context, String xmlFileUrl, String appPackageName, String fileName) {
		this.context = context;
		this.xmlFileUrl = xmlFileUrl;
		this.appPackageName = appPackageName;
		this.fileName = fileName;
	}

	public void setServiceMode(boolean mode) {
		isServiceMode = mode;
	}

	public boolean isServiceMode() {
		return isServiceMode;
	}

	public void setShowDialog(boolean mode) {
		isShowDialog = mode;
	}

	public boolean isShowDialog() {
		return isShowDialog;
	}

	public void setAutoInstall(boolean mode) {
		isAutoInstall = mode;
	}

	public boolean isAutoInstall() {
		return isAutoInstall;
	}

	public void setInstallApkOnBackground(boolean mode) {
		isInstallApkOnBackground = mode;
	}

	public boolean isInstallApkOnBackground() {
		return isInstallApkOnBackground;
	}

	public void setAutoDownload(boolean mode) {
		isAutoDownload = mode;
	}

	public boolean isAutoDownload() {
		return isAutoDownload;
	}

	public void setCheckMD5(boolean mode) {
		isCheckMD5 = mode;
	}

	public boolean isCheckMD5() {
		return isCheckMD5;
	}

	public void setBackgroundDownload(boolean mode) {
		isBackgroundDownload = mode;
	}

	public boolean isBackgroundDownload() {
		return isBackgroundDownload;
	}

	// public boolean isDownloadSuccess() {
	// return isDownloadSuccess;
	// }

	public void setCancelable(boolean mode) {
		isCancelable = mode;
	}

	public boolean isCancelable() {
		return isCancelable;
	}

	public void setAppPackageName(String packageName) {
		appPackageName = packageName;
	}

	public void setXmlFileUrl(String url) {
		xmlFileUrl = url;
	}

	public int getStatus() {
		return status;
	}

	public void update() {
		status = CHECKING_VERSION;
		if (appPackageName == null || appPackageName == "") {
			appPackageName = context.getPackageName();
		}
		try {
			versionCode = context.getPackageManager().getPackageInfo(appPackageName, 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (versionCode < 0) {
			if (isShowDialog) {
				Log.e(TAG, "获取versionCode失败");
			}
			status = STANDBY;
			return;
		}
		if (xmlFileUrl == null || xmlFileUrl == "") {
			Log.e(TAG, "未设置xmlFileUrl");
			status = STANDBY;
			return;
		}
		new Thread(new CheckVersionTask()).start();
	}

	/*
	 * 从服务器获取xml解析并进行比对版本号
	 */
	public class CheckVersionTask implements Runnable {
		public void run() {
			Message msg = new Message();
			try {
				// 包装成url的对象
				URL url = new URL(xmlFileUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				InputStream is = conn.getInputStream();
				info = UpdateInfoParser.getUpdateInfo(is);

				if (info.getVersionCode() == versionCode) {
					Log.i(TAG, "版本号相同无需升级");
					msg.what = NO_UPDATE_APK;
				} else {
					Log.i(TAG, "版本号不同 ,提示用户升级 ");
					msg.what = UPDATE_APK;
				}
			} catch (Exception e) {
				msg.what = GET_UNDATEINFO_ERROR;
				e.printStackTrace();
			}
			if (msg.what == UPDATE_APK) {
				status = HAVE_NEW_VERSION;
			} else {
				status = STANDBY;
			}
			handler.sendMessage(msg);
		}
	}

	/*
	 * 
	 * 弹出对话框通知用户更新程序
	 * 
	 */
	protected void showUpdateDialog() {
		status = DOWNLOADING;
		AlertDialog.Builder builer = new Builder(context);
		builer.setTitle("版本升级");
		builer.setMessage(info.getDescription());
		builer.setCancelable(isCancelable && info.getMustUpdate() == 0);
		// 当点确定按钮时从服务器上下载 新的apk 然后安装
		builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				downLoadApk();
			}
		});
		if (isCancelable && info.getMustUpdate() == 0) {
			// 当点取消按钮时进行登录
			builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					status = STANDBY;
				}
			});
		}
		builer.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				status = STANDBY;
			}
		});
		AlertDialog dialog = builer.create();
		if (isServiceMode) {
			dialog.getWindow().setType(android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		}
		dialog.show();
	}

	/*
	 * 从服务器中下载APK
	 */
	protected void downLoadApk() {
		status = DOWNLOADING;
		// isDownloadSuccess = false;
		final ProgressDialog pd = new ProgressDialog(context);// 进度条对话框
		if (isServiceMode) {
			pd.getWindow().setType(android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		}
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setCancelable(false);
		pd.setMessage("正在下载更新");
		if (isShowDialog && !isBackgroundDownload) {
			pd.show();
		}
		new Thread() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					if (fileName == null || fileName == "") {
						file = DownLoadManager.getFileFromServer(info.getUrl(), pd, info.getFileName());
					} else {
						file = DownLoadManager.getFileFromServer(info.getUrl(), pd, fileName);
					}
					// File file1 = new File(Environment.getExternalStorageDirectory() + "", info.getFileName());
					// sleep(100);
					Log.d(TAG, sumMD5(file.getAbsolutePath()));
					Log.d(TAG, info.getMD5());
					if (sumMD5(file.getAbsolutePath()).equals(info.getMD5()) || isCheckMD5 == false) {
						if (isCheckMD5 == true) {
							Log.i(TAG, "下载文件校验成功！");
						}
						msg.what = DOWNLOAD_SUCCESS;
						status = DOWNLOADED;
					} else {
						Log.e(TAG, "下载文件校验失败！");
						msg.what = DOWNLOAD_ERROR;
						status = STANDBY;
					}
					pd.dismiss(); // 结束掉进度条对话框
					// isDownloadSuccess = true;
				} catch (Exception e) {
					msg.what = DOWNLOAD_ERROR;
					status = STANDBY;
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	protected String sumMD5(String fileName) {
		InputStream fis;
		byte[] buffer = new byte[1024];
		int numRead = 0;
		MessageDigest md5;
		try {
			fis = new FileInputStream(fileName);
			md5 = MessageDigest.getInstance("MD5");
			while ((numRead = fis.read(buffer)) > 0) {
				md5.update(buffer, 0, numRead);
			}
			fis.close();
			byte[] data = md5.digest();// 必须先赋值给另一个变量，否则计算出来的书不正确。
			String newString = new String();
			for (int i = 0; i < data.length; i++) {
				newString += String.format("%02X", data[i]);
			}
			return newString;
		} catch (Exception e) {
			return null;
		}
	}

	/*
	 * 
	 * 弹出对话框通知用户下载完成，是否安装
	 * 
	 */
	protected void showInstallDialog() {
		status = INSTALLING;
		AlertDialog.Builder builer = new Builder(context);
		builer.setTitle("版本升级");
		builer.setMessage("更新下载完成，现在安装吗？\n\n更新信息：\n  " + info.getDescription());
		builer.setCancelable(isCancelable && info.getMustUpdate() == 0);
		// 当点确定按钮时从服务器上下载 新的apk 然后安装
		builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (isInstallApkOnBackground) {
					installApkOnBackground();
				} else {
					installApk();
				}
			}
		});
		if (isCancelable && info.getMustUpdate() == 0) {
			// 当点取消按钮时进行登录
			builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					status = STANDBY;
				}
			});
		}
		builer.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				status = STANDBY;
			}
		});
		AlertDialog dialog = builer.create();
		if (isServiceMode) {
			dialog.getWindow().setType(android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		}
		dialog.show();
	}

	// 安装apk
	protected void installApk() {
		status = INSTALLING;
		Intent intent = new Intent();
		// 执行动作
		intent.setAction(Intent.ACTION_VIEW);
		// 执行的数据类型
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		if (isServiceMode) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		context.startActivity(intent);
		status = STANDBY;
	}

	private void installApkOnBackground() {
		status = INSTALLING;
		final String apkAbsolutePath = file.getAbsolutePath();
		final ProgressDialog pd = new ProgressDialog(context);
		pd.setMessage("正在安装更新");
		if (isServiceMode) {
			pd.getWindow().setType(android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		}
		if (isShowDialog) {
			pd.show();
		}
		new Thread() {
			@Override
			public void run() {
				String[] args = { "pm", "install", "-r", apkAbsolutePath };
				String result = "";
				ProcessBuilder processBuilder = new ProcessBuilder(args);
				Process process = null;
				InputStream errIs = null;
				InputStream inIs = null;
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int read = -1;
					process = processBuilder.start();
					errIs = process.getErrorStream();
					while ((read = errIs.read()) != -1) {
						baos.write(read);
					}
					inIs = process.getInputStream();
					while ((read = inIs.read()) != -1) {
						baos.write(read);
					}
					byte[] data = baos.toByteArray();
					result = new String(data);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (errIs != null) {
							errIs.close();
						}
						if (inIs != null) {
							inIs.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (process != null) {
						process.destroy();
					}
				}
				if (isShowDialog) {
					pd.dismiss();
				}
				Message msg = new Message();
				msg.what = INSTALL_APK_FAIL;
				if (result.length() >= 7) {
					Log.i("result=", result);
					if (result.substring(result.length() - 10, result.length()).toLowerCase(java.util.Locale.US).indexOf("success") != -1) {
						msg.what = INSTALL_APK_SUCCESS;
					}
				}
				handler.sendMessage(msg);
				status = STANDBY;
			}
		}.start();

	}

	public void install() {
		Message msg = new Message();
		msg.what = INSTALL_APK;
		handler.sendMessage(msg);
	}

	public void close() {
		handler.removeCallbacksAndMessages(null);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (listener != null) {
				listener.onChange(msg.what);
			}
			super.handleMessage(msg);
			switch (msg.what) {
			case NO_UPDATE_APK:
				// 版本相同无需升级
				break;
			case UPDATE_APK:
				// 对话框通知用户升级程序
				if (isShowDialog && !isAutoDownload && !isBackgroundDownload) {
					showUpdateDialog();
				} else {
					downLoadApk();
				}
				break;
			case GET_UNDATEINFO_ERROR:
				// 服务器超时
				Log.e(TAG, "获取服务器更新信息失败！");
				break;
			case INSTALL_APK:
				// 安装新的APK
				// isDownloadSuccess = false;

				if (isShowDialog) {
					showInstallDialog();
				} else {
					if (isInstallApkOnBackground) {
						installApkOnBackground();
					} else {
						installApk();
					}
				}
				break;
			case INSTALL_APK_SUCCESS:
				// 安装成功
				Log.i(TAG, "安装成功！");
				break;
			case INSTALL_APK_FAIL:
				// 安装失败
				Log.e(TAG, "安装失败！");
				break;
			case DOWNLOAD_SUCCESS:
				// 下载apk成功
				Log.i(TAG, "下载成功！");
				if (isAutoInstall) {
					install();
				}
				break;
			case DOWNLOAD_ERROR:
				// 下载apk失败
				Log.e(TAG, "下载失败！");
				break;
			}
		}
	};

	public void setOnStatusChangeListener(onStatusChangeListener listener) {
		this.listener = listener;
	}

	private onStatusChangeListener listener;

	public interface onStatusChangeListener {
		public void onChange(int status);
	}
}
