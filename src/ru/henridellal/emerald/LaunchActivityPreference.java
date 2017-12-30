package ru.henridellal.emerald;

//import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

import java.util.HashMap;
import java.util.Map;

public class LaunchActivityPreference extends Preference {
	public LaunchActivityPreference(Context c) {
		this(c, null);
	}
	public LaunchActivityPreference(Context c, AttributeSet attr) {
		super(c, attr);
	}
	@Override
	public void onClick() {
		super.onClick();
		Map<String, Class> activityMap = new HashMap<String, Class>();
		activityMap.put("about", AboutActivity.class);
		activityMap.put("category_manager", CategoryManagerActivity.class);
		activityMap.put("themer", ThemerActivity.class);
		if (activityMap.containsKey(getKey())) {
			Intent intent = new Intent(getContext(), activityMap.get(getKey()));
			getContext().startActivity(intent);
		}
	}
}
