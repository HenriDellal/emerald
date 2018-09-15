package ru.henridellal.emerald;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
//import android.util.Log;

public class PackageUninstallReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null)
			return;
		if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
			return;
		}
		String packageName = intent.getData().getSchemeSpecificPart();
		DatabaseHelper.removeApp(context, packageName);
	}
}
