package ru.henridellal.emerald;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.Map;

public class ManagerContainer {
	private static IconPackManager ipm;
	private static CategoryManager cm;
	public static void setIconPackManager(Context context) {
		String iconPack = PreferenceManager.getDefaultSharedPreferences(context)
			.getString(Options.PREF_ICON_PACK, "default");
		ipm = new IconPackManager(context, iconPack);
	}
	public static IconPackManager getIconPackManager(Context context) {
		if (ipm == null) {
			setIconPackManager(context);
		}
		return getIconPackManager();
	}
	public static IconPackManager getIconPackManager() {
		return ipm;
	}
	public static void newCategoryManager(Context context, Map<String,AppData> map) {
		cm = new CategoryManager(context, map);
	}
	public static CategoryManager getCategoryManager() {
		return cm;
	}

}
