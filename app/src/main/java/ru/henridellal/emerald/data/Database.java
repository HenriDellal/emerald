package ru.henridellal.emerald.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
	
	public static String NAME = "data.s3db";
	public static int FIELD_APP_COMPONENT = 0;
	public static int FIELD_APP_NAME = 1;
	public static int FIELD_APP_CATEGORIES = 2;
	public static int FIELD_APP_DATE = 3;
	public static int FIELD_SHORTCUT_NAME = 0;
	public static int FIELD_SHORTCUT_URI = 1;
	public static int FIELD_SHORTCUT_PACKAGE = 2;
	public static int FIELD_SHORTCUT_RESOURCE = 3;
	public static int FIELD_SHORTCUT_ICON = 4;
	public static int FIELD_SHORTCUT_CATEGORIES = 5;
	public static int FIELD_SHORTCUT_DATE = 6;

	private static int VERSION = 1;
	
	public Database(Context context) {
		super(context, NAME, null, VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE apps (component TEXT NOT NULL, name TEXT NOT NULL, categories TEXT, date INTEGER)");
		db.execSQL("CREATE TABLE shortcuts (name TEXT NOT NULL, uri TEXT NOT NULL, package TEXT, resource TEXT, icon TEXT, categories TEXT, date INTEGER)");
		db.execSQL("CREATE TABLE categories (name TEXT NOT NULL)");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
	
	}
	
}
