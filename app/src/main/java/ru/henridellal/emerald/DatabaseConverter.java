package ru.henridellal.emerald;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;

public class DatabaseConverter {
	public static void convert(Context context) {
		SQLiteDatabase db = DatabaseHelper.getDatabase(context);
		ArrayList<BaseData> appsData = new ArrayList<BaseData>();
		MyCache.read(context, "apps", appsData);
		CategoryManager cm = LauncherApp.getCategoryManager();
		ArrayList<String> components = new ArrayList<String>();
		for (String category: cm.getEditableCategories()) {
			components = cm.getEntriesComponents(cm.catPath(category));
			for (BaseData data: appsData) {
				if (components.contains(data.getComponent())) {
					((AppData)data).addCategory(category);
				}
			}
			cm.catPath(category).delete();
		}
		for (BaseData data: appsData) {
			db.insert("apps", null, ((AppData)data).getContentValues());
		}
		try {
			new File(MyCache.genFilename(context, "apps")).delete();
		} catch (Exception e) {}
	}
}
