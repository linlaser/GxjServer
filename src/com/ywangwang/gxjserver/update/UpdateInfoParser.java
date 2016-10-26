package com.ywangwang.gxjserver.update;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

public class UpdateInfoParser {
	/*
	 * ��pull�������������������ص�xml�ļ� (xml��װ�˰汾��)
	 */
	public static UpdateInfo getUpdateInfo(InputStream is) throws Exception {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(is, "utf-8");// ���ý���������Դ
		int type = parser.getEventType();
		UpdateInfo info = new UpdateInfo();// ʵ��
		while (type != XmlPullParser.END_DOCUMENT) {
			switch (type) {
			case XmlPullParser.START_TAG:
				if ("fileName".equals(parser.getName())) {
					info.setFileName(parser.nextText()); // ��ȡfileName
				} else if ("appLabel".equals(parser.getName())) {
					info.setAppLabel(parser.nextText()); // ��ȡAppLabel
				} else if ("versionName".equals(parser.getName())) {
					info.setVersionName(parser.nextText()); // ��ȡversionName
				} else if ("versionCode".equals(parser.getName())) {
					info.setVersionCode(Integer.parseInt(parser.nextText())); // ��ȡversionCode
				} else if ("mustUpdate".equals(parser.getName())) {
					info.setMustUpdate(Integer.parseInt(parser.nextText())); // ��ȡmustUpdate
				} else if ("url".equals(parser.getName())) {
					info.setUrl(parser.nextText()); // ��ȡҪ������APK�ļ�����·��
				} else if ("description".equals(parser.getName())) {
					info.setDescription(parser.nextText()); // ��ȡ���ļ�����Ϣ
				} else if ("md5".equals(parser.getName())) {
					info.setMD5(parser.nextText()); // ��ȡ���ļ���MD5ֵ
				}
				break;
			}
			type = parser.next();
		}
		return info;
	}

}
