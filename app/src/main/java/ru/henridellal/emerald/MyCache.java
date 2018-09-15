package ru.henridellal.emerald;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class MyCache {
	static public final int MODE_READ = 0;
	static public final int MODE_WRITE = 1;
	
	/*public static boolean write(Context c, String fname, 
			ArrayList<BaseData> data) {
		String path = genFilename(c, fname);
//		Log.v("TinyLaunch", "cache write "+path+" "+data.size()+" items");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					path+".temp"));
			for (BaseData a: data) {
				a.write(writer);
			}
			writer.close();
			if (!new File(path+".temp").renameTo(new File(path))) {
				Log.e("TinyLaunch", "error renaming");
				throw new IOException();
			}
//			Log.v("TinyLaunch", "wrote cache to "+path);
		} catch (IOException e) {
//			Log.e("TinyLaunch", ""+e);
			new File(path+".tmp").delete();
			return false;
		}
		return true;		
	}*/
	
	public static void read(Context c, String fname, ArrayList<BaseData> data) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(genFilename(c,fname)));
			for(;;) {
				String firstLineOfData = reader.readLine();
				BaseData a;
				if (firstLineOfData.startsWith(AppData.COMPONENT)) {
					a = new AppData();
				} else if (firstLineOfData.startsWith(ShortcutData.SHORTCUT_NAME)) {
					a = new ShortcutData();
				} else {
					a = new BaseData();
				}
				a.read(reader, firstLineOfData);
				data.add(a);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (NullPointerException e) {}
	}
	static public String genFilename(Context c, String name) {
		File dir = c.getCacheDir();
		return dir.getPath() + "/" + name + ".MyCache"; 
	}
	public static File getOldCustomIconFile(Context c, BaseData data) {
		return new File(c.getCacheDir(),
				Uri.encode(data.getComponent())+".custom.png");
	}
	public static File getCustomIconFile(Context c, String component) {
		return new File(c.getFilesDir(),
				Uri.encode(component)+".png");
	}
	public static File getCustomIconFile(Context c, BaseData data) {
		return new File(c.getFilesDir(),
				getIconFileName(data, ".png"));
	}
	public static String getShortcutIconFileName(String uri) {
		return ((Integer)uri.hashCode()).toString() + ".icon.png";
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
		return new File(c.getCacheDir(), getShortcutIconFileName(uri));
	}
	public static File getIconFile(Context c, String component) {
		return new File(c.getCacheDir(), getIconFileName(component));
	}
	public static File getIconFile(Context c, BaseData data) {
		return new File(c.getCacheDir(), getIconFileName(data));
	}
	
	public static void deleteIcon(Context c, AppData app) {
		if (app == null)
			return;
		getIconFile(c, app).delete();
	}
	/* removes icons of deleted apps */
	public static void cleanIcons(Context c, ArrayList<BaseData> data) {
		File[] dirs = c.getCacheDir().listFiles();
		for (File f : dirs) {
			boolean deleteFile = true;
			for (BaseData a : data) {
				if ((getIconFileName(a)).equals(f.getName())) {
					deleteFile = false;	
					break;
				}
			}
			if (deleteFile && f.getName().contains(".icon.png")) {
				f.delete();
			}
		}
	}
	/* removes all icons in cache */
	public static void deleteIcons(Context c) {
		File[] dirs = c.getCacheDir().listFiles();
		
		for (File f : dirs) {
			String name = f.getName();
			if (name.endsWith(".icon.png")) {
				f.delete();
			}
		}
	}
}
