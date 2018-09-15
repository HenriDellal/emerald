package ru.henridellal.emerald;

import android.app.Activity;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;

public class Themer {
	public static int theme;
	
	public static final int DEFAULT_THEME = 0;
	public static final int LIGHT = 1;
	public static final int DARK = 2;
	public static final int WALLPAPER_LIGHT = 3;
	public static final int WALLPAPER_DARK = 4;
	
	@SuppressWarnings("deprecation")
	private static void setBarTheme(Activity activity, SharedPreferences options, int theme) {
		Button menuButton = (Button)activity.findViewById(R.id.menuButton);
		Button searchButton = (Button)activity.findViewById(R.id.searchButton);
		Button webSearchButton = (Button)activity.findViewById(R.id.webSearchButton);
		Button categoryButton = (Button)activity.findViewById(R.id.category_button);
		EditText searchField = (EditText)activity.findViewById(R.id.textField);
		Resources resources = activity.getResources();
		switch (theme) {
			case DEFAULT_THEME:
			case LIGHT:
			case WALLPAPER_LIGHT:
				if (Build.VERSION.SDK_INT >= 16) {
					if (!options.getBoolean(Keys.HIDE_MAIN_BAR, false)) {
						menuButton.setBackground(resources.getDrawable(R.drawable.menu_bg));
						searchButton.setBackground(resources.getDrawable(R.drawable.search_bg));
					}
					webSearchButton.setBackground(resources.getDrawable(R.drawable.web_search_bg));
				} else {
					if (!options.getBoolean(Keys.HIDE_MAIN_BAR, false)) {
						menuButton.setBackgroundDrawable(resources.getDrawable(R.drawable.menu_bg));
						searchButton.setBackgroundDrawable(resources.getDrawable(R.drawable.search_bg));
					}
					webSearchButton.setBackgroundDrawable(resources.getDrawable(R.drawable.web_search_bg));
				}
				categoryButton.setTextColor(Color.WHITE);
				searchField.setTextColor(Color.WHITE);
				break;
			default:
				if (Build.VERSION.SDK_INT >= 16) {
					if (!options.getBoolean(Keys.HIDE_MAIN_BAR, false)) {
						menuButton.setBackground(resources.getDrawable(R.drawable.menu_dark_bg));
						searchButton.setBackground(resources.getDrawable(R.drawable.search_dark_bg));
					}
					webSearchButton.setBackground(resources.getDrawable(R.drawable.web_search_dark_bg));
				} else {
					if (!options.getBoolean(Keys.HIDE_MAIN_BAR, false)) {
						menuButton.setBackgroundDrawable(resources.getDrawable(R.drawable.menu_dark_bg));
						searchButton.setBackgroundDrawable(resources.getDrawable(R.drawable.search_dark_bg));
					}
					webSearchButton.setBackgroundDrawable(resources.getDrawable(R.drawable.web_search_dark_bg));
				}
				categoryButton.setTextColor(Color.BLACK);
				searchField.setTextColor(Color.BLACK);
		}
	}
	public static void applyTheme(Activity activity, SharedPreferences options) {
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
		setBarTheme(activity, options, theme);
	}
	public static void setWindowDecorations(Activity activity, SharedPreferences options) {
		if (!options.getBoolean(Keys.FULLSCREEN, false)) {
			activity.getWindow().setStatusBarColor(options.getInt(Keys.STATUS_BAR_BACKGROUND, 0x22000000));
		}
		activity.getWindow().setNavigationBarColor(options.getInt(Keys.NAV_BAR_BACKGROUND, 0x22000000));
	}
}
