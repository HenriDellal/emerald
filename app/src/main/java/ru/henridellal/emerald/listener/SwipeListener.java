package ru.henridellal.emerald.listener;

import android.view.GestureDetector;
import android.view.MotionEvent;

import java.lang.ref.SoftReference;

import ru.henridellal.emerald.activity.Apps;
import ru.henridellal.emerald.data.CategoryManager;
import ru.henridellal.emerald.LauncherApp;

public class SwipeListener extends GestureDetector.SimpleOnGestureListener {
	private SoftReference<Apps> appsRef;
	private float density;

	public SwipeListener(Apps apps) {
		appsRef = new SoftReference<Apps>(apps);
		density = apps.getResources().getDisplayMetrics().density;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		float deltaX = e1.getX() - e2.getX();
		float deltaY = Math.abs(e1.getY() - e2.getY());
		if (Math.abs(deltaX) > 100.f*density && (deltaY < Math.abs(deltaX)*0.3f)) {
			CategoryManager cm = LauncherApp.getInstance().getCategoryManager();
			String category = (deltaX > 0.f) ? cm.getCategory(CategoryManager.NEXT) : cm.getCategory(CategoryManager.PREVIOUS);
			if (null != category) {
				cm.setCurCategory(category);
				appsRef.get().loadFilteredApps();
			}
			return true;
		} else {
			return false;
		}
	}
}
