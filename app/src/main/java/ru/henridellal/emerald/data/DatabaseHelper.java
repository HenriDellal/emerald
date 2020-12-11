package ru.henridellal.emerald.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.henridellal.emerald.activity.Apps;
import ru.henridellal.emerald.ui.Dock;
import ru.henridellal.emerald.LauncherApp;
import ru.henridellal.emerald.R;
import ru.henridellal.emerald.preference.Keys;

public class DatabaseHelper {
	private static SQLiteDatabase database;
	public static Database dbOpenHelper;
	private static int mCounter;
	private static int[] defCategoriesRes = new int[] {
		R.string.category_all,
		R.string.category_unclassified,
		R.string.category_history,
		R.string.category_hidden
	};
	private static String[] defCategories = new String[] {
		CategoryManager.ALL,
		CategoryManager.UNCLASSIFIED,
		CategoryManager.HISTORY,
		CategoryManager.HIDDEN
	};

	public static synchronized SQLiteDatabase getDatabase(Context context) {
		mCounter++;
		if (mCounter == 1) {
			dbOpenHelper = new Database(context);
			database = dbOpenHelper.getWritableDatabase();
		}
		return database;
	}
	
	public static synchronized void close() {
		mCounter--;
		if (mCounter == 0)
		database.close();
	}
	
	public static Map<String, Category> getCategories(Context context) {
		SQLiteDatabase db = getDatabase(context);
		Map<String, Category> categories = new HashMap<String, Category>();
		Cursor cursor = db.rawQuery("SELECT * FROM categories", null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String name = cursor.getString(0);
			categories.put(name, new Category(name, new ArrayList<BaseData>(), 0));
			cursor.moveToNext();
		}
		cursor.close();
		close();
		for (int i = 0; i < 4; i++) {
			categories.put(defCategories[i], new Category(defCategories[i], new ArrayList<BaseData>(), defCategoriesRes[i]));
		}
		return categories;
	}
	
	public static boolean hasCategory(SQLiteDatabase db, String categoryName) {
		String query = String.format("SELECT * FROM categories WHERE name = '%s'", categoryName);
		Cursor cursor = db.rawQuery(query, null);
		boolean categoryExists = ((cursor.getCount() != 0) || !CategoryManager.isCustom(categoryName));
		cursor.close();
		return categoryExists;
	}
	
	public static boolean addCategory(Context context, String categoryName) {
		SQLiteDatabase db = getDatabase(context);
		
		if (hasCategory(db, categoryName)) {
			close();
			return false;
		}
		ContentValues values = new ContentValues();
		values.put("name", categoryName);
		db.insert("categories", null, values);
		close();
		return true;
	}

	public static void addToCategory(Context context, BaseData data, String categoryName) {
		SQLiteDatabase db = getDatabase(context);
		String table, column;
		if (data instanceof AppData) {
			table = "apps";
			column = "component";
		} else if (data instanceof ShortcutData) {
			table = "shortcuts";
			column = "uri";
		} else {
			close();
			return;
		}

		String query = String.format("SELECT %s, categories FROM %s WHERE %s = '%s'", column, table, column, data.getId());
		Cursor cursor = db.rawQuery(query, null);
		cursor.moveToFirst();
		String categoriesList = cursor.getString(1);
		ContentValues values = new ContentValues();
		values.put("categories", categoriesList.concat("@" + categoryName + "@"));
		db.update(table, values, column + " = ?", new String[]{cursor.getString(0)});
		cursor.close();
		close();
	}
	
	public static void removeAppFromCategory(Context context, String id, String categoryName) {
		String query = String.format("SELECT component, categories FROM apps WHERE component = '%s'", id);
		SQLiteDatabase db = getDatabase(context);
		Cursor cursor = db.rawQuery(query, null);
		if (!cursor.moveToFirst()) {
			cursor.close();
			close();
			return;
		}
		String categoryNameSQL = "@" + categoryName + "@";
		String oldCategoriesList = cursor.getString(1);
		int startIndex = oldCategoriesList.indexOf(categoryNameSQL);
		String newValue = new StringBuilder(oldCategoriesList).delete(startIndex, startIndex+categoryNameSQL.length()).toString();
		ContentValues values = new ContentValues();
		values.put("categories", newValue);
		db.update("apps", values, "component = ?", new String[]{cursor.getString(0)});
		cursor.close();
		close();
	}
	
	public static void removeShortcutFromCategory(Context context, String id, String categoryName) {
		String query = String.format("SELECT uri, categories FROM shortcuts WHERE uri = '%s'", id);
		SQLiteDatabase db = getDatabase(context);
		Cursor cursor = db.rawQuery(query, null);
		if (!cursor.moveToFirst()) {
			cursor.close();
			close();
			return;
		}
		String categoryNameSQL = "@" + categoryName + "@";
		String oldCategoriesList = cursor.getString(1);
		int startIndex = oldCategoriesList.indexOf(categoryNameSQL);
		String newValue = new StringBuilder(oldCategoriesList).delete(startIndex, startIndex+categoryNameSQL.length()).toString();
		ContentValues values = new ContentValues();
		values.put("categories", newValue);
		db.update("shortcuts", values, "uri = ?", new String[]{cursor.getString(0)});
		cursor.close();
		close();
	}
	
	public static void removeFromCategory(Context context, BaseData data, String categoryName) {
		String table, column;
		if (data instanceof AppData) {
			table = "apps";
			column = "component";
		} else if (data instanceof ShortcutData) {
			table = "shortcuts";
			column = "uri";
		} else {
			return;
		}

		SQLiteDatabase db = getDatabase(context);
		String categoryNameSQL = "@" + categoryName + "@";
		String query = String.format("SELECT %s, categories FROM %s WHERE %s = '%s'", column, table, column, data.getId());
		Cursor cursor = db.rawQuery(query, null);
		cursor.moveToFirst();
		String oldCategoriesList = cursor.getString(1);
		int startIndex = oldCategoriesList.indexOf(categoryNameSQL);
		String newValue = new StringBuilder(oldCategoriesList).delete(startIndex, startIndex+categoryNameSQL.length()).toString();
		ContentValues values = new ContentValues();
		values.put("categories", newValue);
		db.update(table, values, column + " = ?", new String[]{cursor.getString(0)});
		cursor.close();
		close();
	}
	
	public static boolean renameCategory(Context context, String oldCategoryName, String newCategoryName) {
		SQLiteDatabase db = getDatabase(context);
		if (hasCategory(db, newCategoryName)) {
			close();
			return false;
		}
		String oldCategoryNameSQL = "@" + oldCategoryName + "@";
		String newCategoryNameSQL = "@" + newCategoryName + "@";
		String query, oldCategoriesList, newValue;
		String[] tableNames = new String[] {"apps", "shortcuts"};
		int[] categoryFields = new int[] {Database.FIELD_APP_CATEGORIES, Database.FIELD_SHORTCUT_CATEGORIES};
		Cursor cursor;
		for (int i = 0; i <= 1; i++) {
			query = String.format("SELECT * FROM %s WHERE categories LIKE '%%@%s@%%'", tableNames[i], oldCategoryName);
			cursor = db.rawQuery(query, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				oldCategoriesList = cursor.getString(categoryFields[i]);
				newValue = oldCategoriesList.replace(oldCategoryNameSQL, newCategoryNameSQL);
				ContentValues values = new ContentValues();
				values.put("categories", newValue);
				db.update(tableNames[i], values, "categories = ?", new String[]{oldCategoriesList});
				cursor.moveToNext();
			}
			cursor.close();
		}
		ContentValues values = new ContentValues();
		values.put("name", newCategoryName);
		db.update("categories", values, "name = ?", new String[]{oldCategoryName});
		close();
		return true;
	}
	
	public static void deleteCategory(Context context, String categoryName) {
		SQLiteDatabase db = getDatabase(context);
		db.delete("categories", "name = ?", new String[]{categoryName});
		clearCategory(context, categoryName);
		close();
		CategoryManager cm = LauncherApp.getCategoryManager();
		if (categoryName.equals(cm.getCurCategory()))
			cm.setCurCategory(CategoryManager.ALL);
	}
	
	public static void clearCategory(Context context, String categoryName) {
		SQLiteDatabase db = getDatabase(context);
		String categoryNameSQL = "@" + categoryName + "@";
		String query, oldCategoriesList, newValue;
		String[] tableNames = new String[] {"apps", "shortcuts"};
		int[] categoryFields = new int[] {Database.FIELD_APP_CATEGORIES, Database.FIELD_SHORTCUT_CATEGORIES};
		Cursor cursor;
		for (int i = 0; i <= 1; i++) {
			query = String.format("SELECT * FROM %s WHERE categories LIKE '%%%s%%'", tableNames[i], categoryNameSQL);
			cursor = db.rawQuery(query, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				oldCategoriesList = cursor.getString(categoryFields[i]);
				int startIndex = oldCategoriesList.indexOf(categoryNameSQL);
				newValue = new StringBuilder(oldCategoriesList).delete(startIndex, startIndex+categoryNameSQL.length()).toString();
				ContentValues values = new ContentValues();
				values.put("categories", newValue);
				db.update(tableNames[i], values, "categories = ?", new String[]{oldCategoriesList});
				cursor.moveToNext();
			}
			cursor.close();
		}
		close();
	}
	
	public static void insertApp(Context context, String component, String name) {
		SQLiteDatabase db = getDatabase(context);
		if (!hasApp(context, component)) {
			ContentValues values = new ContentValues();
			values.put("component", component);
			values.put("name", name);
			values.put("categories", "");
			db.insert("apps", null, values);
		}
		close();
	}

	public static void insertShortcut(Context context, ContentValues values) {
        SQLiteDatabase db = getDatabase(context);
        if (!hasShortcut(context, values.getAsString("uri"))) {
        	db.insert("shortcuts", null, values);
        }
        close();
    }
    
    public static void removeApp(Context context, String component) {
    	SQLiteDatabase db = getDatabase(context);
    	db.delete("apps", "component LIKE ?", new String[]{component + "%"});
    	db.delete("shortcuts", "uri LIKE ?", new String[]{"%" + component + "%"});
    	close();
		Dock.remove(component);
    	Cache.getIconFile(context, component).delete();
    	Cache.getCustomIconFile(context, component).delete();
    }
    
    public static void removeShortcut(Context context, String uri) {
        SQLiteDatabase db = getDatabase(context);
        db.delete("shortcuts", "uri = ?", new String[]{uri});
        close();
        new File(context.getCacheDir(), uri.hashCode()+".png").delete();
    }

    public static boolean isDatabaseEmpty(Context context) {
    	SQLiteDatabase db = getDatabase(context);
    	Cursor cursor = db.rawQuery("SELECT * FROM apps", null);
		boolean result = cursor.getCount() == 0;
    	cursor.close();
    	close();
    	return result;
    }
    
    public static ArrayList<BaseData> getEntries(Context context, String categoryName) {
    	
    	String whereClause;
    	if (categoryName == null) {
    		whereClause = "";
    	} else if (categoryName.equals(CategoryManager.ALL)) {
        	whereClause = " WHERE categories NOT LIKE '%@Hidden@%'";
        } else if (categoryName.equals(CategoryManager.UNCLASSIFIED)) {
        	whereClause = " WHERE categories = '@History@' OR categories = '' OR categories = NULL";
        } else if (categoryName.equals(CategoryManager.HISTORY)) {
        	whereClause = " WHERE categories LIKE '%@History@%' ORDER BY date DESC";
        } else {
        	whereClause = " WHERE categories LIKE '%@" + categoryName + "@%'";
        }
        ArrayList<BaseData> records = new ArrayList<BaseData>();
        SQLiteDatabase db = getDatabase(context);

        Cursor cursor = db.rawQuery("SELECT * FROM shortcuts" + whereClause, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            BaseData entry = new ShortcutData(cursor);
            records.add(entry);
            cursor.moveToNext();
        }
        cursor.close();
		cursor = db.rawQuery("SELECT * FROM apps" + whereClause, null);
		cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            BaseData entry = new AppData(cursor);
            records.add(entry);
            cursor.moveToNext();
        }
        cursor.close();
        close();
        return records;
    }
    
    public static void reduceHistory(Context context) {
    	SQLiteDatabase db = getDatabase(context);
    	Cursor cursor;
    	String[] ids = new String[2];
    	String[] queries = new String[]{
    		"SELECT component, date FROM apps WHERE categories LIKE '%@History@%' ORDER BY date ASC",
    		"SELECT uri, date FROM shortcuts WHERE categories LIKE '%@History@%' ORDER BY date ASC"
    	};
    	long[] dates = new long[]{Long.MAX_VALUE, Long.MAX_VALUE};
    	int[] counts = new int[2];
    	for (int i = 0; i <= 1; i++) {
    		cursor = db.rawQuery(queries[i], null);
	    	counts[i] = cursor.getCount();
	    	cursor.moveToFirst();
	    	if (counts[i] > 0) {
	    		ids[i] = cursor.getString(0);
	    		dates[i] = cursor.getLong(1);
	    	}
    		cursor.close();
    	}
    	close();
    	// value is compared to history size - 1, because this function is executed before the addition
    	if (counts[0] + counts[1] > ((Apps)context).options.getInt(Keys.HISTORY_SIZE, 10) - 1) {
    		if (dates[0] <= dates[1]) { 
    			removeAppFromCategory(context, ids[0], CategoryManager.HISTORY);
    		} else {
    			removeShortcutFromCategory(context, ids[1], CategoryManager.HISTORY);
    		}
    	}
    }
    
    public static void addToHistory(Context context, BaseData data) {
    	if (!hasItem(context, data, CategoryManager.HISTORY)) {
    		reduceHistory(context);
    	}
    	String table, columnName;
    	if (data instanceof AppData) {
    		table = "apps";
    		columnName = "component";
    	} else {
    		table = "shortcuts";
    		columnName = "uri";
    	}
    	SQLiteDatabase db = getDatabase(context);
    	ContentValues values = new ContentValues();
    	values.put("date", System.currentTimeMillis());
    	db.update(table, values, columnName + " = ?", new String[] {data.getId()});
    	if (!hasItem(context, data, CategoryManager.HISTORY)) {
    		addToCategory(context, data, CategoryManager.HISTORY);
    	}
    	close();
    
    }
    
    public static boolean hasComponent(Context context, String component, String categoryName) {
    	SQLiteDatabase db = getDatabase(context);
    	Cursor cursor = db.rawQuery("SELECT component FROM apps WHERE component = '"+ component+ "' AND categories LIKE '%@" + categoryName+ "@%'", null);
    	boolean result = cursor.getCount() != 0;
    	cursor.close();
    	close();
    	return result;
    }
    
    public static boolean hasItem(Context context, AppData data, String categoryName) {
    	return hasComponent(context, data.getId(), categoryName);
    }
    
    public static boolean hasItem(Context context, BaseData data, String categoryName) {
    	SQLiteDatabase db = getDatabase(context);
		boolean result = false;
    	String categoryQuery = (null == categoryName) ? "'" :
    				"' AND categories LIKE '%@" + categoryName+ "@%'";
		Cursor cursor = null;
    	if (data instanceof AppData) {
    		cursor = db.rawQuery("SELECT component FROM apps WHERE component = '"+ data.getId()
    					+ categoryQuery, null);
    	} else if (data instanceof ShortcutData) {
    		cursor = db.rawQuery("SELECT uri FROM shortcuts WHERE uri = '"+ data.getId()
						+ categoryQuery, null);
		}
		if (null != cursor) {
			result = cursor.getCount() != 0;
    		cursor.close();
		}
		close();
		return result;
    }
    
    public static boolean hasApp(Context context, String component) {
		String query = String.format("SELECT component FROM apps WHERE component = '%s'", component);
		SQLiteDatabase db = getDatabase(context);
		Cursor cursor = db.rawQuery(query, null);
		boolean result = cursor.getCount() != 0;
		cursor.close();
		close();
		return result;
	}

    public static boolean hasShortcut(Context context, String uri) {
		String query = String.format("SELECT uri FROM shortcuts WHERE uri = '%s'", uri);
    	SQLiteDatabase db = getDatabase(context);
    	Cursor cursor = db.rawQuery(query, null);
    	boolean result = cursor.getCount() != 0;
    	cursor.close();
    	close();
    	return result;
    }

	public static boolean hasMenuShortcut(Context context) {
		return hasShortcut(context, Apps.ACTION_OPEN_MENU);
	}
}
