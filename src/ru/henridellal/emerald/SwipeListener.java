package ru.henridellal.emerald;

import android.view.MotionEvent;
import android.view.View;

public class SwipeListener implements View.OnTouchListener {
	float x, density;
	private Apps apps;
	public SwipeListener(Apps apps) {
		this.apps = apps;
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
					ManagerContainer.getCategoryManager().setCurCategory(ManagerContainer.getCategoryManager().getPrevCategory());
					apps.loadFilteredApps();
					apps.setSpinner();
					return true;
				} else if (x-e.getX() > 30.0 * density) {
					ManagerContainer.getCategoryManager().setCurCategory(ManagerContainer.getCategoryManager().getNextCategory());
					apps.loadFilteredApps();
					apps.setSpinner();
					//list.startAnimation(fadeIn);
					return true;
				} else v.performClick();
			default:
				return false;
		}
	}
}
