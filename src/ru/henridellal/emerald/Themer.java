package ru.henridellal.emerald;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
	private static void setBarTheme(Activity activity, int theme) {
		Button menuButton = (Button)activity.findViewById(R.id.menuButton);
		Button searchButton = (Button)activity.findViewById(R.id.searchButton);
		Button webSearchButton = (Button)activity.findViewById(R.id.webSearchButton);
		EditText searchField = (EditText)activity.findViewById(R.id.textField);
		Resources resources = activity.getResources();
		switch (theme) {
			case DEFAULT_THEME:
			case LIGHT:
			case WALLPAPER_LIGHT:
				if (Build.VERSION.SDK_INT >= 16) {
					menuButton.setBackground(resources.getDrawable(R.drawable.menu_bg));
					searchButton.setBackground(resources.getDrawable(R.drawable.search_bg));
					webSearchButton.setBackground(resources.getDrawable(R.drawable.web_search_bg));
				} else {
					menuButton.setBackgroundDrawable(resources.getDrawable(R.drawable.menu_bg));
					searchButton.setBackgroundDrawable(resources.getDrawable(R.drawable.search_bg));
					webSearchButton.setBackgroundDrawable(resources.getDrawable(R.drawable.web_search_bg));
				}
				searchField.setTextColor(Color.WHITE);
				break;
			default:
				if (Build.VERSION.SDK_INT >= 16) {
					menuButton.setBackground(resources.getDrawable(R.drawable.menu_dark_bg));
					searchButton.setBackground(resources.getDrawable(R.drawable.search_dark_bg));
					webSearchButton.setBackground(resources.getDrawable(R.drawable.web_search_dark_bg));
				} else {
					menuButton.setBackgroundDrawable(resources.getDrawable(R.drawable.menu_dark_bg));
					searchButton.setBackgroundDrawable(resources.getDrawable(R.drawable.search_dark_bg));
					webSearchButton.setBackgroundDrawable(resources.getDrawable(R.drawable.web_search_dark_bg));
				}
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
		setBarTheme(activity, theme);
	}
	public static void setWindowDecorations(Activity activity, SharedPreferences options) {
		if (Build.VERSION.SDK_INT >= 21) {
			activity.getWindow().setStatusBarColor(options.getInt(Keys.STATUS_BAR_BACKGROUND, 0x22000000));
			activity.getWindow().setNavigationBarColor(options.getInt(Keys.NAV_BAR_BACKGROUND, 0x22000000));
		} /*else {
			activity.findViewById(R.id.dummy_top_view).setBackgroundColor(options.getInt(Keys.BAR_BACKGROUND, 0x22000000));
			Display display = ((WindowManager)activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			Point size = new Point();
			Point realSize = new Point();
			display.getSize(size);
			display.getRealSize(realSize);
			int navBarHeight = size.y-realSize.y;
			View dummyBottomView = activity.findViewById(R.id.dummy_bottom_view);
			ViewGroup.LayoutParams p = dummyBottomView.getLayoutParams();
			p.height = navBarHeight;
			dummyBottomView.setLayoutParams(p);
			if (navBarHeight > 0) {
				dummyBottomView.setVisibility(View.VISIBLE);
				dummyBottomView.setBackgroundColor(options.getInt(Keys.NAV_BAR_BACKGROUND, 0x22000000));
			}
		}*/
	}
}
