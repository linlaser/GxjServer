package com.ywangwang.gxjserver.update;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.ProgressDialog;
import android.os.Environment;
import android.util.Log;

public class DownLoadManager {

	public static File getFileFromServer(String downloadURL, ProgressDialog pd, String fileName) throws Exception {
		if (fileName == null || fileName == "") {
			fileName = "update.apk";
		}
		// 如果相等的话表示当前的sdcard挂载在手机上并且是可用的
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			URL url = new URL(downloadURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			// 获取到文件的大小
			if (pd != null) {
				pd.setMax(conn.getContentLength());
			}
			InputStream is = conn.getInputStream();

			// 获得存储卡的路径
			String savePath = Environment.getExternalStorageDirectory() + "/update";
			Log.d("path", savePath + "/" + fileName);

			File folder = new File(savePath);
			// 判断文件目录是否存在
			if (!folder.exists()) {
				folder.mkdir();
			}

			File file = new File(savePath, fileName);
			FileOutputStream fos = new FileOutputStream(file);
			BufferedInputStream bis = new BufferedInputStream(is);
			byte[] buffer = new byte[1024];
			int len;
			int total = 0;
			while ((len = bis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
				total += len;
				// 获取当前下载量
				if (pd != null) {
					pd.setProgress(total);
				}
			}
			fos.close();
			bis.close();
			is.close();
			return file;
		} else {
			return null;
		}
	}
}
