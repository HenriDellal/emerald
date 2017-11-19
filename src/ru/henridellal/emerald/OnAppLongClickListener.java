package ru.henridellal.emerald;

import android.view.View;

public class OnAppLongClickListener implements View.OnLongClickListener {
	private Apps apps;
	public OnAppLongClickListener(Apps apps) {
		this.apps = apps;
	}
	
	@Override
	public boolean onLongClick(View arg0) {
		apps.itemContextMenu((AppData)arg0.getTag());
		return false;
	}
}
