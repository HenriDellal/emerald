package ru.henridellal.emerald;

//import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class RestorePreference extends Preference {
	Context context;
	public RestorePreference(Context c) {
		this(c, null);
	}
	public RestorePreference(Context c, AttributeSet attr) {
		super(c, attr);
		context = c;
	}
	@Override
	public void onClick() {
		super.onClick();
		((Options)context).restorePrefs();
	}
}
