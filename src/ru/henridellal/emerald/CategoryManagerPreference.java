package ru.henridellal.emerald;

//import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

public class CategoryManagerPreference extends Preference {
	Context context;
	public CategoryManagerPreference(Context c) {
		this(c, null);
	}
	public CategoryManagerPreference(Context c, AttributeSet attr) {
		super(c, attr);
		context = c;
	}
	@Override
	public void onClick() {
		super.onClick();
		Intent intent = new Intent(context, CategoryManagerActivity.class);
		context.startActivity(intent);
	}
}
