package ru.henridellal.emerald;

import android.view.View;

public class OnAppClickListener implements View.OnClickListener {
	private Apps apps;
	public OnAppClickListener(Apps apps) {
		this.apps = apps;
	}
	
	@Override
	public void onClick(View arg0) {
		if (arg0.getTag() instanceof AppData) {
			apps.launch((AppData)arg0.getTag());
		}
	}
}
