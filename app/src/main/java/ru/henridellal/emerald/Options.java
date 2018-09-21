package ru.henridellal.emerald;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.ListPreference;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

public class Options extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.options);
		setIconPacksList(LauncherApp.getInstance().getIconPackManager().getIconPacks());
		if (Build.VERSION.SDK_INT < 11) {
			findPreference(Keys.KEEP_IN_MEMORY).setEnabled(false);
			findPreference(Keys.THEME).setEnabled(false);
		}
		PreferenceManager.getDefaultSharedPreferences(this)
			.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(Keys.ICON_PACK) || key.equals(Keys.TRANSFORM_DRAWABLE)) {
			MyCache.deleteIcons(this);
			LauncherApp.getInstance().getIconPackManager().setIconPack(sharedPreferences.getString(Keys.ICON_PACK, "default"));
			sharedPreferences.edit().putBoolean(Keys.ICON_PACK_CHANGED, true).commit();
			//Apps.this.loadAppsFromSystem(true);
		} else if (key.equals(Keys.APP_SHORTCUT)) {
			int appShortcut = Integer.parseInt(sharedPreferences.getString(Keys.APP_SHORTCUT, "3"));
			int prevAppShortcut = Integer.parseInt(sharedPreferences.getString(Keys.PREV_APP_SHORTCUT, "3"));
			if (appShortcut >= CustomAdapter.ICON && prevAppShortcut == CustomAdapter.TEXT) {
				sharedPreferences.edit()
					.putBoolean(Keys.ICON_PACK_CHANGED, true)
					.putString(Keys.PREV_APP_SHORTCUT, ((Integer)appShortcut).toString())
					.commit();
			} else if (appShortcut == CustomAdapter.TEXT && prevAppShortcut >= CustomAdapter.ICON) {
				MyCache.deleteIcons(this);
				sharedPreferences.edit()
					.putString(Keys.PREV_APP_SHORTCUT, ((Integer)appShortcut).toString())
					.commit();
			}
		} else if (!sharedPreferences.getBoolean(Keys.MESSAGE_SHOWN, false) && Arrays.asList(Keys.restart).contains(key)) {
			Toast.makeText(this, getResources().getString(R.string.restart_needed), Toast.LENGTH_LONG).show();
			sharedPreferences.edit().putBoolean(Keys.MESSAGE_SHOWN, true).commit();
		}
	}
	
	@Override
	public void onBackPressed() {
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Keys.MESSAGE_SHOWN, false)) {
			System.exit(0);
		} else {
			super.onBackPressed();
		}
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	public void setIconPacksList(Map<String, String> iconPacks) {
		ListPreference preference = (ListPreference)findPreference(Keys.ICON_PACK);
		CharSequence[] e = new CharSequence[iconPacks.size()+1];
		CharSequence[] v = new CharSequence[iconPacks.size()+1];
		e[0] = getResources().getString(R.string.defaultIconPack);
		v[0] = "default";
		Set<Map.Entry<String, String>> entryset = iconPacks.entrySet();
		short i=1;
		for (Map.Entry<String, String> entry: entryset) {
			e[i] = entry.getKey();
			v[i] = entry.getValue();
			i++;
		}
		preference.setEntries(e);
		preference.setEntryValues(v);
	}
	
	@Override
	public void onDestroy() {
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}
	
	public void backupPrefs(File file) {
		List<String> backupKeys = Arrays.asList(Keys.BACKUP);
		FileOutputStream output = null;
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			output = new FileOutputStream(file);
			Set<? extends Map.Entry<? extends String, ? extends Object>> prefEntrySet = PreferenceManager.getDefaultSharedPreferences(this).getAll().entrySet();
			for (Map.Entry<? extends String, ? extends Object> entry: prefEntrySet) {
				String key = entry.getKey();
				if (!backupKeys.contains(key)) {
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
			Toast.makeText(this, getResources().getString(R.string.successfulBackup)+file.getPath(), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(this, "Backup failed: " + e, Toast.LENGTH_LONG).show();
		} finally {
			try {
				//output.flush();
				output.close();
			} catch (Exception ex) {}
		}
	}
	
	public void restorePrefs(File file) {
		List<String> backupKeys = Arrays.asList(Keys.BACKUP);
		BufferedReader input = null;
		SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		ArrayList<String> argTypes = new ArrayList<String>();
		argTypes.add("STRING");
		argTypes.add("INTEGER");
		argTypes.add("BOOLEAN");
		argTypes.add("FLOAT");
		try {
			input = new BufferedReader(new FileReader(file));
			String key, value, line, prefType = null;
			while ((line = input.readLine()).indexOf("END") == -1) {
				if (argTypes.contains(line.trim())) {
					prefType = line.trim();
					continue;
				}
				int index = line.indexOf('=');
				key = line.substring(0, index).trim();
				if (!backupKeys.contains(key)) {
					continue;
				}
				value = line.substring(index+1, line.length()).trim();
				if (prefType.indexOf("STRING") == 0) {
						prefsEditor.putString(key, value);
				} else if (prefType.indexOf("INTEGER") == 0) {
						prefsEditor.putInt(key, Integer.parseInt(value));
				} else if (prefType.indexOf("BOOLEAN") == 0) {
						prefsEditor.putBoolean(key, "true".equals(value));
				} else if (prefType.indexOf("FLOAT") == 0) {
						prefsEditor.putFloat(key, Float.parseFloat(value));
				}
			}
			prefsEditor.commit();
			Toast.makeText(this, getResources().getString(R.string.successfulRestore)+file.getPath(), Toast.LENGTH_LONG).show();
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
