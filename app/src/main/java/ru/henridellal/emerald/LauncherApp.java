package ru.henridellal.emerald;

import android.app.Application;
import android.preference.PreferenceManager;

public class LauncherApp extends Application {
	private static LauncherApp singleton;
	public static LauncherApp getInstance() {
		return singleton;
	}
	
	private static IconPackManager ipm;
	private static CategoryManager cm;
	public static IconPackManager getIconPackManager() {
		return ipm;
	}
	public static CategoryManager getCategoryManager() {
		return cm;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		String iconPack = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
			.getString(Keys.ICON_PACK, "default");
		ipm = new IconPackManager(getApplicationContext(), iconPack);
		cm = new CategoryManager(getApplicationContext());
	}
}
