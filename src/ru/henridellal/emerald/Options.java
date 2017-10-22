package ru.henridellal.emerald;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.ListPreference;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

public class Options extends PreferenceActivity {
	// Constants for icon representation
	public static final int TEXT = 1;
	public static final int ICON = 2;
	
	// THEME CONSTANTS
	
	public static final int DEFAULT_THEME = 0;
	public static final int LIGHT = 1;
	public static final int DARK = 2;
	public static final int WALLPAPER_LIGHT = 3;
	public static final int WALLPAPER_DARK = 4;
	
	public final static String PREF_BAR_BACKGROUND = "bar_background";
	public final static String PREF_DOCK_BACKGROUND = "dock_background";
	public final static String PREF_APPS_WINDOW_BACKGROUND = "apps_background";
	
	public final static String PREF_ICON_PACK = "icon_pack";
	public final static String PREF_TRANSFORM_DRAWABLE = "transform_drawable";
	public final static String PREF_APP_SHORTCUT = "app_shortcut";
	public final static String PREF_PREV_APP_SHORTCUT = "prevApp_shortcut";
	public final static String PREF_TILE = "tile";
	
	public final static String PREF_ICON_SIZE = "icon_size";
	public final static String PREF_TEXT_SIZE = "text_size";
	public final static String PREF_FONT_STYLE = "font_style";
	public final static String PREF_COLUMN_WIDTH = "column_width";
	public final static String PREF_VERTICAL_SPACING = "vertical_spacing";
	
	public final static String PREF_ICON_SIZE_LANDSCAPE = "icon_size_land";
	public final static String PREF_TEXT_SIZE_LANDSCAPE = "text_size_land";
	public final static String PREF_COLUMN_WIDTH_LANDSCAPE = "column_width_land";
	public final static String PREF_VERTICAL_SPACING_LANDSCAPE = "vertical_spacing_land";
	
	public static final String PREF_DOCK_IN_LANDSCAPE = "show_dock_in_landscape";
	
	public static final String PREF_HOME = "home";
	public static final String PREF_CATEGORY = "category";
	public static final String PREF_DIRTY = "dirty";
	public static final String PREF_ORIENTATION = "orientation";
	public static final String PREF_SEARCH_PROVIDER = "search_provider";
	public static final String PREF_THEME = "theme";
	public static final String PREF_PREV_THEME = "prevTheme";
	public static final String PREF_HISTORY_SIZE = "history_size";
	public static final String PREF_PASSWORD = "password";
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.options);
		setIconPacksList(ManagerContainer.getIconPackManager(this).getIconPacks());
		/*int theme = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(Options.PREF_THEME, getResources().getString(R.string.defaultThemeValue)));
		switch (theme) {
			case DEFAULT_THEME:
			case LIGHT:
			case WALLPAPER_LIGHT:
				setTheme(R.style.AppTheme_Preferences_Light);
				break;
			default:
				setTheme(R.style.AppTheme_Preferences_Dark);
		}*/
		
			
	}
	@Override
	public void onResume() {
		super.onResume();
		//setRequestedOrientation(Integer.parseInt(PreferenceManager
		//	.getDefaultSharedPreferences(this)
		//	.getString(Options.PREF_ORIENTATION, "1")));
	/*	int theme = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
			.getString(Options.PREF_THEME, "0"));
		if (theme == Options.DEFAULT_THEME || theme == Options.LIGHT || theme == Options.WALLPAPER_LIGHT) {
			setTheme(R.style.AppTheme);
		} else {
			setTheme(R.style.AppTheme_Dark);
		}*/
	}
	
	public void setIconPacksList(Map<String, String> iconPacks) {
		ListPreference preference = (ListPreference)findPreference(PREF_ICON_PACK);
		CharSequence[] e = new CharSequence[iconPacks.size()+1];
		CharSequence[] v = new CharSequence[iconPacks.size()+1];
		e[0] = getResources().getString(R.string.defaultIconPack);
		v[0] = "default";
		Set<Map.Entry<String, String>> entryset = iconPacks.entrySet();
		short i=1;
		//for (int i = 1; i <= values.size(); i++) {
		for (Map.Entry<String, String> entry: entryset) {
			e[i] = entry.getKey();
			v[i] = entry.getValue();
			i++;
		}
		preference.setEntries(e);
		preference.setEntryValues(v);
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	public void backupPrefs(File backupFile) {
		FileOutputStream output = null;
		try {
			//File sdPath = Environment.getExternalStorageDirectory();
			//String backupPath = "/Android/data/ru.henridellal.emerald/files/preferences.txt";
			//File backupFile = new File(sdPath, backupPath);
			if (!backupFile.exists()) {
				backupFile.getParentFile().mkdirs();
				backupFile.createNewFile();
			}
			output = new FileOutputStream(backupFile);
			Set<? extends Map.Entry<? extends String, ? extends Object>> prefEntrySet = PreferenceManager.getDefaultSharedPreferences(this).getAll().entrySet();
			for (Map.Entry<? extends String, ? extends Object> entry: prefEntrySet) {
				String key = entry.getKey();
				if (key.equals("password")) {
					continue;
				}
				Object value = entry.getValue();
				String argType = null;
				if (value instanceof String) {
					argType = "STRING\n";
				} else if (value instanceof Integer) {
					argType = "INTEGER\n";
				} else if (value instanceof Boolean) {
					argType = "BOOLEAN\n";
				} else if (value instanceof Float) {
					argType = "FLOAT\n";
				} else {
					argType = "STRING\n";
				}
				byte[] typeBuffer = argType.getBytes();
				output.write(typeBuffer, 0, typeBuffer.length);
				StringBuilder line = new StringBuilder();
				line.append(key);
				line.append("=");
				line.append(entry.getValue().toString());
				line.append("\n");
				byte[] buffer = line.toString().getBytes();
				output.write(buffer, 0, buffer.length);
			}
			byte[] buffer = "END".getBytes();
			output.write(buffer, 0, buffer.length);
			Toast.makeText(this, getResources().getString(R.string.successfulBackup)+backupFile.getPath(), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(this, "Backup failed: " + e, Toast.LENGTH_LONG).show();
		} finally {
			try {
				//output.flush();
				output.close();
			} catch (Exception ex) {}
		}
	}
	
	public void restorePrefs(File backupFile) {
		BufferedReader input = null;
		SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		ArrayList<String> argTypes = new ArrayList<String>();
		argTypes.add("STRING");
		argTypes.add("INTEGER");
		argTypes.add("BOOLEAN");
		argTypes.add("FLOAT");
		try {
			input = new BufferedReader(new FileReader(backupFile));
			String key, value, line, prefType = null;
			while ((line = input.readLine()).indexOf("END") == -1) {
				if (argTypes.contains(line.trim())) {
					prefType = line.trim();
					continue;
				}
				int index = line.indexOf('=');
				key = line.substring(0, index).trim();
				if (!PreferenceManager.getDefaultSharedPreferences(this).contains(key)) {
					continue;
				}
				value = line.substring(index+1, line.length()).trim();
				if (prefType.indexOf("STRING") == 0) {
						prefsEditor.putString(key, value);
				} else if (prefType.indexOf("INTEGER") == 0) {
						prefsEditor.putInt(key, Integer.parseInt(value));
				} else if (prefType.indexOf("BOOLEAN") == 0) {
						prefsEditor.putBoolean(key, value.equals("true"));
				} else if (prefType.indexOf("FLOAT") == 0) {
						prefsEditor.putFloat(key, Float.parseFloat(value));
				}
			}
			prefsEditor.commit();
			Toast.makeText(this, getResources().getString(R.string.successfulRestore)+backupFile.getPath(), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(this, "Restore failed: "+ e, Toast.LENGTH_LONG).show();
		} finally {
			try {
				input.close();
				Intent i = getIntent();
				finish();
				startActivity(i);
			} catch (Exception ex) {}
		}
	}
}
