package ru.henridellal.emerald;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class ShortcutData extends BaseData {
	
	public static String SHORTCUT_NAME = "S";
	public static String SHORTCUT_URI = "U";
	public static String SHORTCUT_COMPONENT = "P";
	
	
	private String uri, packageName, iconResource;
	@Override
	public String getId() {
		return uri;
	}
	public String getUri() {
		return uri;
	}
	public ShortcutData() {
		super();
	}
	public ShortcutData(String name, String uri, String packageName, String resourceName) {
		this(name, uri);
		this.component = packageName +"/"+resourceName;
	}
	public ShortcutData(String name, String uri) {
		super(null, name);
		this.uri = uri;
	}
	
	@Override
	public boolean equals(Object shortcut) {
		if (! (shortcut instanceof ShortcutData))
			return false;
		if (uri == null) {
			return shortcut == null || ((ShortcutData)shortcut).getUri() == null;
		}
		return uri.equals( ((ShortcutData)shortcut).getUri() );
	}
	
	public ShortcutData(Cursor cursor) {
		super();
		name = cursor.getString(Database.FIELD_SHORTCUT_NAME);
		uri = cursor.getString(Database.FIELD_SHORTCUT_URI);
		packageName = cursor.getString(Database.FIELD_SHORTCUT_PACKAGE);
		iconResource = cursor.getString(Database.FIELD_SHORTCUT_RESOURCE);
	}
	
	public void read(BufferedReader reader, String firstLineOfData){
		try {
			this.name = firstLineOfData.substring(1).trim();
			this.uri = readLine(reader, SHORTCUT_URI).substring(1).trim();
		} catch (IOException e) {
		
		}
	}
	
	public void write(BufferedWriter writer) throws IOException {
		writer.write(new StringBuilder(SHORTCUT_NAME)
		.append(this.name)
		.append("\n")
		.append(SHORTCUT_URI)
		.append(this.uri)
		.append("\n").toString());
	}

	@Override
	public Intent getLaunchIntent(Context context) {
		if (!DatabaseHelper.hasItem(context, this, CategoryManager.HIDDEN))
			DatabaseHelper.addToHistory(context, this);
		try {
			return Intent.parseUri(getUri(), 0);
		} catch (Exception e) {
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
			return null;
		}
	}

	@Override
	public int hashCode() {
		return getUri().hashCode();
	}
}
