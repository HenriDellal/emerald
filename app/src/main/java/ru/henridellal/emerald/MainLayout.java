package ru.henridellal.emerald;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainLayout {

	public static RelativeLayout get(Context context, SharedPreferences options) {
		RelativeLayout mainLayout = new RelativeLayout(context);
		LayoutInflater layoutInflater = (LayoutInflater) 
			context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		addDockBar(mainLayout, layoutInflater, options);

		boolean kitkatNoImmersiveMode = (Build.VERSION.SDK_INT == 19 && !options.getBoolean(Keys.FULLSCREEN, false));
		FrameLayout mainBar = (FrameLayout) layoutInflater.inflate(R.layout.main_bar, mainLayout, false);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mainBar.getLayoutParams());
		
		GridView grid = (GridView) layoutInflater.inflate(R.layout.apps_grid, mainLayout, false);
		initAppsGrid(grid, options);
		
		if (options.getBoolean(Keys.BOTTOM_MAIN_BAR, true)) {
			layoutParams.addRule(RelativeLayout.ABOVE, R.id.dock_bar);
			mainBar.setLayoutParams(layoutParams);
			mainLayout.addView(mainBar);
			
			if (kitkatNoImmersiveMode) {
				mainLayout.addView(getFakeStatusBar(mainLayout, layoutInflater, options));
			}
			layoutParams = new RelativeLayout.LayoutParams(grid.getLayoutParams());
			if (kitkatNoImmersiveMode) {
				layoutParams.addRule(RelativeLayout.BELOW, R.id.kitkat_status_bar);
			} else {
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			}
			layoutParams.addRule(RelativeLayout.ABOVE, R.id.main_bar);
			grid.setLayoutParams(layoutParams);
			mainLayout.addView(grid);
		} else {
			if (!kitkatNoImmersiveMode) {
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			} else {
				mainLayout.addView(getFakeStatusBar(mainLayout, layoutInflater, options));
				layoutParams.addRule(RelativeLayout.BELOW, R.id.kitkat_status_bar);
			}
			
			mainBar.setLayoutParams(layoutParams);
			mainLayout.addView(mainBar);
			
			layoutParams = new RelativeLayout.LayoutParams(grid.getLayoutParams());
			layoutParams.addRule(RelativeLayout.ABOVE, R.id.dock_bar);
			layoutParams.addRule(RelativeLayout.BELOW, R.id.main_bar);
			grid.setLayoutParams(layoutParams);
			mainLayout.addView(grid);
		}
		if (options.getBoolean(Keys.HIDE_MAIN_BAR, false)) {
			mainBar.setVisibility(View.GONE);
		}
		mainBar.setBackgroundColor(options.getInt(Keys.BAR_BACKGROUND, 0x22000000));
		
		return mainLayout;
	}
	
	private static void initAppsGrid(GridView grid, SharedPreferences options) {
		grid.setBackgroundColor(options.getInt(Keys.APPS_WINDOW_BACKGROUND, 0));
		if (options.getBoolean(Keys.STACK_FROM_BOTTOM, false)) {
			grid.setStackFromBottom(true);
		}
		if (options.getBoolean(Keys.TILE, true)) {
			grid.setNumColumns(GridView.AUTO_FIT);
		}
		
		if (options.getBoolean(Keys.SCROLLBAR, false)) {
			setFastScroll(grid);
		}
	}
	
	private static void addDockBar(RelativeLayout mainLayout, LayoutInflater inflater, SharedPreferences options) {
		LinearLayout dockBar = (LinearLayout) inflater.inflate(R.layout.dock_bar, mainLayout, false);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dockBar.getLayoutParams());
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		dockBar.setLayoutParams(layoutParams);
		dockBar.setBackgroundColor(options.getInt(Keys.DOCK_BACKGROUND, 0x22000000));
		mainLayout.addView(dockBar);
	}
	
	private static View getFakeStatusBar(RelativeLayout mainLayout, LayoutInflater inflater, SharedPreferences options) {
		View fakeStatusBar = inflater.inflate(R.layout.kitkat_status_bar, mainLayout, false);
		fakeStatusBar.setBackgroundColor(options.getInt(Keys.STATUS_BAR_BACKGROUND, 0x22000000));
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(fakeStatusBar.getLayoutParams());
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		fakeStatusBar.setLayoutParams(layoutParams);
		return fakeStatusBar;
	}
	
	private static void setFastScroll(GridView grid) {
		grid.setFastScrollEnabled(true);
		grid.setFastScrollAlwaysVisible(true);
		grid.setScrollBarStyle(AbsListView.SCROLLBARS_INSIDE_INSET);
		grid.setSmoothScrollbarEnabled(true);
	}
}
