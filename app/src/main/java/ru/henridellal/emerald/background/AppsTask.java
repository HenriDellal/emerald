package ru.henridellal.emerald.background;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.io.File;
import java.util.List;

import ru.henridellal.emerald.R;
import ru.henridellal.emerald.activity.Apps;
import ru.henridellal.emerald.data.DatabaseHelper;
import ru.henridellal.emerald.data.Cache;
import ru.henridellal.emerald.preference.Keys;

public class AppsTask implements Runnable {
	private Apps activity;
	private boolean icons, iconPackChanged;
	protected int i;
	protected List<ResolveInfo> list;

	public AppsTask(Apps activity, boolean icons, boolean iconPackChanged) {
		this.activity = activity;
		this.icons = icons;
		this.iconPackChanged = iconPackChanged;
	}

	@Override
	public void run() {
		// use intent to get apps that can be launched
		Intent launchIntent = new Intent(Intent.ACTION_MAIN);
		launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		PackageManager pm = activity.getPackageManager();
		list = pm.queryIntentActivities(launchIntent, 0);
		for (i = 0; i < list.size(); i++) {
			Apps.handler.post(new Runnable() {
				@Override
				public void run() {
					Apps.progress.setIndeterminate(false);
					Apps.progress.setMax(list.size());
					Apps.progress.setProgress(i);
				}
			});
			ResolveInfo info = list.get(i);
			ComponentName cn = new ComponentName(info.activityInfo.packageName,
					info.activityInfo.name);
			String component = cn.flattenToString();
			String name = info.activityInfo.loadLabel(pm).toString();
			if (!iconPackChanged && DatabaseHelper.hasApp(activity, component)) {
				continue;
			}
			if (name == null) {
				name = component;
			} else if (name.equals(activity.getResources().getString(R.string.app_name))) {
				continue;
			}
			if (icons) {
				File iconFile = Cache.getIconFile(activity, component);
				if (!iconFile.exists()) {
					try {
						Apps.writeIconToFile(iconFile, pm.getResourcesForActivity(cn)
										.getDrawable(pm.getPackageInfo(
												info.activityInfo.packageName,
												0).applicationInfo.icon),
								component);
					} catch (Exception e) {}
				}
			}
			DatabaseHelper.insertApp(activity, component, name);
		}
		activity.options.edit().putString(Keys.PREV_APP_SHORTCUT,
				activity.options.getString(Keys.APP_SHORTCUT, "1")).commit();
		Apps.loadAppsHandler.sendEmptyMessage(0);
	}
}
