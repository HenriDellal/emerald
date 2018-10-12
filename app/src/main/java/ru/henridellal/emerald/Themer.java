package ru.henridellal.emerald;

import android.app.Activity;
import android.content.SharedPreferences;

public class Themer {

	public static int theme;

	public static final int DEFAULT_THEME = 0;
	public static final int LIGHT = 1;
	public static final int DARK = 2;
	public static final int WALLPAPER_LIGHT = 3;
	public static final int WALLPAPER_DARK = 4;
	
	public static void applyTheme(Activity activity, SharedPreferences options) {
		theme = Integer.parseInt(options.getString(Keys.THEME, activity.getResources().getString(R.string.defaultThemeValue)));
		switch (theme) {
			case LIGHT:
				activity.setTheme(R.style.AppTheme_Light);
				break;
			case DARK:
				activity.setTheme(R.style.AppTheme_Dark);
				break;
			case WALLPAPER_LIGHT:
				activity.setTheme(R.style.AppTheme_Light_Wallpaper);
				break;
			case WALLPAPER_DARK:
				activity.setTheme(R.style.AppTheme_Dark_Wallpaper);
				break;
		}
	}
	public static void setWindowDecorations(Activity activity, SharedPreferences options) {
		if (!options.getBoolean(Keys.FULLSCREEN, false)) {
			activity.getWindow().setStatusBarColor(options.getInt(Keys.STATUS_BAR_BACKGROUND, 0x22000000));
		}
		activity.getWindow().setNavigationBarColor(options.getInt(Keys.NAV_BAR_BACKGROUND, 0x22000000));
	}
}
