package ru.henridellal.emerald;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper {
	private static SQLiteDatabase database;
	public static Database dbOpenHelper;
	private static int mCounter;
	
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
		categories.put(CategoryManager.ALL, new Category(CategoryManager.ALL, new ArrayList<BaseData>(), R.string.category_all));
		categories.put(CategoryManager.UNCLASSIFIED, new Category(CategoryManager.UNCLASSIFIED, new ArrayList<BaseData>(), R.string.category_unclassified));
		categories.put(CategoryManager.HISTORY, new Category(CategoryManager.HISTORY, new ArrayList<BaseData>(), R.string.category_history));
		categories.put(CategoryManager.HIDDEN, new Category(CategoryManager.HIDDEN, new ArrayList<BaseData>(), R.string.category_hidden));
		return categories;
	}
	
	public static boolean hasCategory(SQLiteDatabase db, String categoryName) {
		Cursor cursor = db.rawQuery("SELECT * FROM categories WHERE name = '" + categoryName +"'", null);
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
	
	public static void addAppToCategory(Context context, String component, String categoryName) {
		SQLiteDatabase db = getDatabase(context);
		String categoryNameSQL = "@" + categoryName + "@";
		String table, column;
		table = "apps";
		column = "component";
		Cursor cursor = db.rawQuery("SELECT "+ column + ", categories FROM " + table + " WHERE " + column + " = '" +
			component + "'", null);
		cursor.moveToFirst();
		String categoriesList = cursor.getString(1);
		ContentValues values = new ContentValues();
		values.put("categories", categoriesList.concat(categoryNameSQL));
		db.update(table, values, column + " = ?", new String[]{cursor.getString(0)});
		cursor.close();
		close();
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
		String categoryNameSQL = "@" + categoryName + "@";
		Cursor cursor = db.rawQuery("SELECT "+ column + ", categories FROM " + table + " WHERE " + column + " = '" +
			data.getId() + "'", null);
		cursor.moveToFirst();
		String categoriesList = cursor.getString(1);
		ContentValues values = new ContentValues();
		values.put("categories", categoriesList.concat(categoryNameSQL));
		db.update(table, values, column + " = ?", new String[]{cursor.getString(0)});
		cursor.close();
		close();
	}
	
	public static void removeAppFromCategory(Context context, String id, String categoryName) {
		SQLiteDatabase db = getDatabase(context);
		Cursor cursor = db.rawQuery("SELECT component, categories FROM apps WHERE component = '" +
			id + "'", null);
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
		SQLiteDatabase db = getDatabase(context);
		Cursor cursor = db.rawQuery("SELECT uri, categories FROM shortcuts WHERE uri = '" +
			id + "'", null);
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
		String categoryNameSQL = "@" + categoryName + "@";
		Cursor cursor = db.rawQuery("SELECT " + column + ", categories FROM " + table + " WHERE " + column + " = '" +
			data.getId() + "'", null);
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
		String oldCategoriesList, newValue;
		Cursor cursor = db.rawQuery("SELECT * FROM apps WHERE categories LIKE '%" +
			oldCategoryNameSQL + "%'", null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			oldCategoriesList = cursor.getString(2);
			newValue = oldCategoriesList.replace(oldCategoryNameSQL, newCategoryNameSQL);
			ContentValues values = new ContentValues();
			values.put("categories", newValue);
			db.update("apps", values, "categories = ?", new String[]{oldCategoriesList});
			cursor.moveToNext();
		}
		cursor.close();
		cursor = db.rawQuery("SELECT * FROM shortcuts WHERE categories LIKE '%" +
			oldCategoryNameSQL + "%'", null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			oldCategoriesList = cursor.getString(4);
			newValue = oldCategoriesList.replace(oldCategoryNameSQL, newCategoryNameSQL);
			ContentValues values = new ContentValues();
			values.put("categories", newValue);
			db.update("shortcuts", values, "categories = ?", new String[]{oldCategoriesList});
			cursor.moveToNext();
		}
		cursor.close();
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
	}
	
	public static void clearCategory(Context context, String categoryName) {
		SQLiteDatabase db = getDatabase(context);
		String categoryNameSQL = "@" + categoryName + "@";
		String oldCategoriesList, newValue;
		Cursor cursor = db.rawQuery("SELECT * FROM apps WHERE categories LIKE '%" +
			categoryNameSQL + "%'", null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			oldCategoriesList = cursor.getString(2);
			int startIndex = oldCategoriesList.indexOf(categoryNameSQL);
			newValue = new StringBuilder(oldCategoriesList).delete(startIndex, startIndex+categoryNameSQL.length()).toString();
			ContentValues values = new ContentValues();
			values.put("categories", newValue);
			db.update("apps", values, "categories = ?", new String[]{oldCategoriesList});
			cursor.moveToNext();
		}
		cursor.close();
		cursor = db.rawQuery("SELECT * FROM shortcuts WHERE categories LIKE '%" +
			categoryNameSQL + "%'", null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			oldCategoriesList = cursor.getString(2);
			int startIndex = oldCategoriesList.indexOf(categoryNameSQL);
			newValue = new StringBuilder(oldCategoriesList).delete(startIndex, startIndex+categoryNameSQL.length()).toString();
			ContentValues values = new ContentValues();
			values.put("categories", newValue);
			db.update("apps", values, "categories = ?", new String[]{oldCategoriesList});
			cursor.moveToNext();
		}
		cursor.close();
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
	
	public static void insertApp(Context context, AppData app) {
		SQLiteDatabase db = getDatabase(context);
		if (!hasApp(context, app.getComponent())) {
			db.insert("apps", null, app.getContentValues());
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
    	MyCache.getIconFile(context, component).delete();
    	MyCache.getCustomIconFile(context, component).delete();
    }
    
    public static void removeShortcut(Context context, String uri) {
        SQLiteDatabase db = getDatabase(context);
        db.delete("shortcuts", "uri = ?", new String[]{uri});
        close();
        new File(context.getCacheDir(), uri.hashCode()+".png").delete();
    }
    
    public static boolean isDatabaseEmpty(Context context) {
    	SQLiteDatabase db = getDatabase(context);
    	boolean result = db.rawQuery("SELECT * FROM apps", null).getCount() == 0;
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
    
    public static boolean hasUri(Context context, String uri, String categoryName) {
    	SQLiteDatabase db = getDatabase(context);
    	Cursor cursor = db.rawQuery("SELECT uri FROM shortcuts WHERE uri = '"+ uri+ "' AND categories LIKE '%@" + categoryName+ "@%'", null);
    	boolean result = cursor.getCount() != 0;
    	cursor.close();
    	close();
    	return result;
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
    	String categoryQuery = (null == categoryName) ? "'" :
    				"' AND categories LIKE '%@" + categoryName+ "@%'";
    	if (data instanceof AppData) {
    		Cursor cursor = db.rawQuery("SELECT component FROM apps WHERE component = '"+ data.getId()
    					+ categoryQuery, null);
    		boolean result = cursor.getCount() != 0;
    		cursor.close();
    		close();
    		return result;
    	} else if (data instanceof ShortcutData) {
    		Cursor cursor = db.rawQuery("SELECT uri FROM shortcuts WHERE uri = '"+ data.getId()
    					+ categoryQuery, null);
    		boolean result = cursor.getCount() != 0;
    		cursor.close();
    		close();
    		return result;
    	} else {
    		close();
    		return false;
    	}
    }
    
    public static boolean hasApp(Context context, String component) {
    	SQLiteDatabase db = getDatabase(context);
    	Cursor cursor = db.rawQuery("SELECT component FROM apps WHERE component = '"+ component+ "'", null);
    	boolean result = cursor.getCount() != 0;
    	cursor.close();
    	close();
    	return result;
    }
    
    public static boolean hasShortcut(Context context, String uri) {
    	SQLiteDatabase db = getDatabase(context);
    	Cursor cursor = db.rawQuery("SELECT uri FROM shortcuts WHERE uri = '"+ uri +"'", null);
    	boolean result = cursor.getCount() != 0;
    	cursor.close();
    	close();
    	return result;
    }
}
