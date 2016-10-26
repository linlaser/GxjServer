package com.ywangwang.gxjserver.update;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

public class UpdateInfoParser {
	/*
	 * 用pull解析器解析服务器返回的xml文件 (xml封装了版本号)
	 */
	public static UpdateInfo getUpdateInfo(InputStream is) throws Exception {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(is, "utf-8");// 设置解析的数据源
		int type = parser.getEventType();
		UpdateInfo info = new UpdateInfo();// 实体
		while (type != XmlPullParser.END_DOCUMENT) {
			switch (type) {
			case XmlPullParser.START_TAG:
				if ("fileName".equals(parser.getName())) {
					info.setFileName(parser.nextText()); // 获取fileName
				} else if ("appLabel".equals(parser.getName())) {
					info.setAppLabel(parser.nextText()); // 获取AppLabel
				} else if ("versionName".equals(parser.getName())) {
					info.setVersionName(parser.nextText()); // 获取versionName
				} else if ("versionCode".equals(parser.getName())) {
					info.setVersionCode(Integer.parseInt(parser.nextText())); // 获取versionCode
				} else if ("mustUpdate".equals(parser.getName())) {
					info.setMustUpdate(Integer.parseInt(parser.nextText())); // 获取mustUpdate
				} else if ("url".equals(parser.getName())) {
					info.setUrl(parser.nextText()); // 获取要升级的APK文件下载路径
				} else if ("description".equals(parser.getName())) {
					info.setDescription(parser.nextText()); // 获取该文件的信息
				} else if ("md5".equals(parser.getName())) {
					info.setMD5(parser.nextText()); // 获取该文件的MD5值
				}
				break;
			}
			type = parser.next();
		}
		return info;
	}

}
