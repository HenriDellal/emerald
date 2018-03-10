package ru.henridellal.emerald;

import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.SoftReference;

public class SwipeListener implements View.OnTouchListener {
	private float x, density;
	private SoftReference<Apps> appsRef;
	public SwipeListener(Apps apps) {
		appsRef = new SoftReference<Apps>(apps);
		density = apps.getResources().getDisplayMetrics().density;
	}
	@Override
	public boolean onTouch(View v, MotionEvent e) {
		int action = e.getAction() & 255;
		switch (action){
			case MotionEvent.ACTION_DOWN:
				x = e.getX();
				return true;
			case MotionEvent.ACTION_UP:
				if (e.getX()-x > 30.0 * density) {
					LauncherApp.getInstance().getCategoryManager().setCurCategory(LauncherApp.getInstance().getCategoryManager().getPrevCategory());
					appsRef.get().loadFilteredApps();
					return true;
				} else if (x-e.getX() > 30.0 * density) {
					LauncherApp.getInstance().getCategoryManager().setCurCategory(LauncherApp.getInstance().getCategoryManager().getNextCategory());
					appsRef.get().loadFilteredApps();
					return true;
				} else v.performClick();
			default:
				return false;
		}
	}
}
