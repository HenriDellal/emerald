package ru.henridellal.emerald;

import java.lang.ref.SoftReference;
import android.view.View;

public class OnAppLongClickListener implements View.OnLongClickListener {
	private SoftReference<Apps> appsRef;
	public OnAppLongClickListener(Apps apps) {
		appsRef = new SoftReference<Apps>(apps);
	}
	
	@Override
	public boolean onLongClick(View arg0) {
		if (arg0.getTag() instanceof AppData) {
			appsRef.get().itemContextMenu((AppData)arg0.getTag());
		} else if (arg0.getTag() instanceof ShortcutData) {
			appsRef.get().itemContextMenu((ShortcutData)arg0.getTag());
		}
		return false;
	}
}
