package ru.henridellal.emerald;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;

public class DatabaseConverter {
	public static void convert(Context context) {
		SQLiteDatabase db = DatabaseHelper.getDatabase(context);
		ArrayList<BaseData> appsData = new ArrayList<BaseData>();
		MyCache.read(context, "apps", appsData);
		CategoryManager cm = LauncherApp.getCategoryManager();
		ArrayList<String> components = new ArrayList<String>();
		
		for (File f : context.getFilesDir().listFiles()) {
			String filename = f.getName();
			if (filename.endsWith(".cat")) {
				String categoryName = filename.substring(0, filename.length()-4);
				categoryName = URLDecoder.decode(categoryName);
				DatabaseHelper.addCategory(context, categoryName);
		
				components = cm.getEntriesComponents(f);
				for (BaseData data: appsData) {
					if (components.contains(data.getComponent())) {
						((AppData)data).addCategory(categoryName);
					}
				}
				f.delete();
			}
		}
		
		for (BaseData data: appsData) {
			db.insert("apps", null, ((AppData)data).getContentValues());
		}
		
		try {
			new File(MyCache.genFilename(context, "apps")).delete();
		} catch (Exception e) {}
	}
}
