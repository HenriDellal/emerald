package ru.henridellal.emerald.background;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.os.Process;

import java.io.File;
import java.util.List;

import ru.henridellal.emerald.R;
import ru.henridellal.emerald.activity.Apps;
import ru.henridellal.emerald.data.DatabaseHelper;
import ru.henridellal.emerald.data.Cache;
import ru.henridellal.emerald.preference.Keys;

@TargetApi(21)
public class AppsTaskLollipop implements Runnable {
	protected int i;
	protected List<LauncherActivityInfo> list;
	private Apps activity;
	private boolean icons, iconPackChanged;

	public AppsTaskLollipop(Apps activity, boolean icons, boolean iconPackChanged) {
		this.activity = activity;
		this.icons = icons;
		this.iconPackChanged = iconPackChanged;
	}

	@Override
	public void run() {
		list = ((LauncherApps)(activity).getSystemService(Context.LAUNCHER_APPS_SERVICE))
				.getActivityList(null, Process.myUserHandle());

		for (i = 0; i < list.size(); i++) {
			Apps.handler.post(new Runnable() {
				@Override
				public void run() {
					Apps.progress.setIndeterminate(false);
					Apps.progress.setMax(list.size());
					Apps.progress.setProgress(i);
				}
			});

			LauncherActivityInfo info = list.get(i);
			ComponentName cn = info.getComponentName();
			String component = cn.flattenToString();
			if (!iconPackChanged && DatabaseHelper.hasApp(activity, component)) {
				continue;
			}
			String name = info.getLabel().toString();
			if (name == null) {
				name = component;
			} else if (name.equals(activity.getResources().getString(R.string.app_name))) {
				continue;
			}
			// load icons
			if (icons) {
				// get icon file for app from cache
				File iconFile = Cache.getIconFile(activity, component);
				// if there is no icon for app in cache
				if (!iconFile.exists()) {
					Apps.writeIconToFile(iconFile, info.getIcon(0), component);
				}
			}
			DatabaseHelper.insertApp(activity, component, name);
		}
		activity.options.edit().putString(Keys.PREV_APP_SHORTCUT,
				activity.options.getString(Keys.APP_SHORTCUT, "1")).commit();
		Apps.loadAppsHandler.sendEmptyMessage(0);
	}
}