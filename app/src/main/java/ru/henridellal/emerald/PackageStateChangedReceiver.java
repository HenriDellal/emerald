package ru.henridellal.emerald;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Process;
//import android.util.Log;

import java.io.File;
import java.util.List;

public class PackageStateChangedReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null)
			return;
		String action = intent.getAction(); 
		if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
			onPackageRemove(context, intent);
		} else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
			onPackageReplace(context, intent);
		} else {
			onPackageAdd(context, intent);
		}
	}
	
	private void onPackageReplace(Context context, Intent intent) {
		String component = intent.getStringExtra(Intent.EXTRA_CHANGED_COMPONENT_NAME);
		//String component = (intent.getStringArrayExtra(Intent.EXTRA_CHANGED_COMPONENT_NAME_LIST))[0];

		if (DatabaseHelper.hasApp(context, component)) {
			DatabaseHelper.removeApp(context, component);

			//onPackageRemove(context, intent);
		} else {
			onPackageAdd(context, intent);
		}
	}

	private void onPackageRemove(Context context, Intent intent) {
		if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
			return;
		}
		String packageName = intent.getData().getSchemeSpecificPart();
		DatabaseHelper.removeApp(context, packageName);
	}

	private void onPackageAdd(Context context, Intent intent) {
		PackageManager pm = context.getPackageManager();
		String packageName = intent.getData().getSchemeSpecificPart();
		Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
		if (launchIntent == null) {
			return;
		}
		String className = launchIntent.getComponent().getClassName();
		String component = packageName + "/" + className;
		String label;
		if (Build.VERSION.SDK_INT >= 21) {
			List<LauncherActivityInfo> list = ((LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE))
							.getActivityList(packageName, Process.myUserHandle());
			try {
				label = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
			} catch (Exception e) {
				return;
			}
			if (list.size() > 0) {
				File iconFile = MyCache.getIconFile(context, component);
				LauncherActivityInfo info = list.get(0);
				Apps.writeIconToFile(iconFile, info.getIcon(0), component);
				if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
					DatabaseHelper.insertApp(context, component, label);
				}
			}
		} else {
			List<ResolveInfo> list = pm.queryIntentActivities(launchIntent, 0);
			if (list.size() > 0) {
				File iconFile = MyCache.getIconFile(context, component);
				ResolveInfo info = list.get(0);
				label = info.activityInfo.loadLabel(pm).toString();
				ComponentName cn = new ComponentName(info.activityInfo.packageName, 
										info.activityInfo.name);
				try {
					Apps.writeIconToFile(iconFile, pm.getResourcesForActivity(cn)
													.getDrawable(pm.getPackageInfo(
													info.activityInfo.packageName, 
													0).applicationInfo.icon), component);
					if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
						DatabaseHelper.insertApp(context, component, label);
					}
				} catch (PackageManager.NameNotFoundException e) {}
			}
		}
	}
}
