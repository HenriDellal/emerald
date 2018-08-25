package ru.henridellal.emerald;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
//import android.util.Log;

public class DirtinessReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null)
			return;
		SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(context);
		if (options == null)
			return;
		options.edit().putBoolean(Keys.DIRTY, true).commit();
		//Log.v("TinyLaunch", "marked as dirty");
	}
}
