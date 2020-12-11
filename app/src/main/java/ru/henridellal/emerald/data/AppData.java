package ru.henridellal.emerald.data;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class AppData extends BaseData {
	
	//Constants for parsing
	public static final String COMPONENT = "C";
	public static final String NAME = "N";

	@Override
	public boolean equals(Object a) {
		if (! (a instanceof AppData))
			return false;
		if (component == null) {
			return a == null || ((AppData)a).component == null;
		}
		return component.equals( ((AppData)a).component );
	}

	public AppData() {
		super();
	}

	public AppData(Cursor cursor) {
		super();
		component = cursor.getString(0);
		name = cursor.getString(1);
	}

	public AppData(String component, String name) {
		super(component, name);
	}

	public void read(BufferedReader reader, String firstLineOfData) {
		try {
			this.component = firstLineOfData.substring(1).trim();
			this.name = readLine(reader, NAME).substring(1).trim();
		} catch (IOException ignored) {
		
		}
	}

	//writes app data in given file writer
	public void write(BufferedWriter writer) throws IOException {
		writer.write(new StringBuilder(COMPONENT)
			.append(this.component)
			.append("\n")
			.append(NAME)
			.append(this.name)
			.append("\n").toString());
	}

	public Intent getLaunchIntent(Context context) {
		//Log.v(APP_TAG, "User launched an app");
		if (!DatabaseHelper.hasItem(context, this, CategoryManager.HIDDEN))
			DatabaseHelper.addToHistory(context, this);
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_LAUNCHER);
		i.setComponent(ComponentName.unflattenFromString(getComponent()));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		return i;
	}
}
