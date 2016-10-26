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
		// �����ȵĻ���ʾ��ǰ��sdcard�������ֻ��ϲ����ǿ��õ�
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			URL url = new URL(downloadURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			// ��ȡ���ļ��Ĵ�С
			if (pd != null) {
				pd.setMax(conn.getContentLength());
			}
			InputStream is = conn.getInputStream();

			// ��ô洢����·��
			String savePath = Environment.getExternalStorageDirectory() + "/update";
			Log.d("path", savePath + "/" + fileName);

			File folder = new File(savePath);
			// �ж��ļ�Ŀ¼�Ƿ����
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
				// ��ȡ��ǰ������
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
