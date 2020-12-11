package ru.henridellal.emerald.data;

import java.io.File;

import android.content.Context;
import android.net.Uri;

import ru.henridellal.emerald.data.BaseData;

public class Cache {

	public static File getCustomIconFile(Context c, String component) {
		return new File(c.getFilesDir(),
				Uri.encode(component)+".png");
	}

	public static File getCustomIconFile(Context c, BaseData data) {
		return new File(c.getFilesDir(),
				getIconFileName(data, ".png"));
	}

	public static String getShortcutIconFileName(String uri) {
		return ((Integer)uri.hashCode()).toString() + ".png";
	}

	public static String getIconFileName(String id) {
		return Uri.encode(id)+".icon.png";
	}

	public static String getIconFileName(BaseData data) {
		return getIconFileName(data, ".icon.png");
	}

	public static String getIconFileName(BaseData data, String postfix) {
		String component = data.getComponent();
		if (component != null) {
			return Uri.encode(component)+postfix;
		} else {
			return ((Integer)data.hashCode()).toString() + postfix;
		}
	}

	public static File getShortcutIconFile(Context c, String uri) {
		return new File(c.getFilesDir(), getShortcutIconFileName(uri));
	}

	public static File getIconFile(Context c, String component) {
		return new File(c.getFilesDir(), getIconFileName(component));
	}

	public static File getIconFile(Context c, BaseData data) {
		return new File(c.getFilesDir(), getIconFileName(data));
	}

	public static void deleteIcons(Context c) {
		File[] dirs = c.getFilesDir().listFiles();

		for (File f : dirs) {
			String name = f.getName();
			if (name.endsWith(".icon.png")) {
				f.delete();
			}
		}
	}
}
