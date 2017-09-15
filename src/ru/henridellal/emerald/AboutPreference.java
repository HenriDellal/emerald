package ru.henridellal.emerald;

//import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

public class AboutPreference extends Preference {
	Context context;
	public AboutPreference(Context c) {
		this(c, null);
	}
	public AboutPreference(Context c, AttributeSet attr) {
		super(c, attr);
		context = c;
	}
	@Override
	public void onClick() {
		super.onClick();
		Intent intent = new Intent(context, AboutActivity.class);
		context.startActivity(intent);
	}
}
