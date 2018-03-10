package ru.henridellal.emerald;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.content.pm.LauncherApps;
import android.content.pm.LauncherActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Process;
import android.util.Log;

public class GetApps extends AsyncTask<Boolean, Integer, ArrayList<AppData>> {
	final PackageManager pm;
	final Apps	 context;
	public final static String CACHE_NAME = "apps"; 
	ProgressDialog progress;
	String component;

	GetApps(Apps c) {
		this.context = c;
		pm = context.getPackageManager();
	}

//	private boolean profilable(ApplicationInfo a) {
//		return true;
//	}
	private void writeIconTo(File iconFile, Drawable d) {
		try {
			Bitmap bmp;
			IconPackManager ipm = LauncherApp.getInstance().getIconPackManager();
			// get icon from icon pack
			if (((bmp = ipm.getBitmap(component)) == null) && (d instanceof BitmapDrawable)) {
				// edit drawable to match icon pack
				bmp = ipm.transformDrawable(d);
			}
			// save icon in cache
			FileOutputStream out = new FileOutputStream(iconFile);
			bmp.compress(CompressFormat.PNG, 100, out);
			out.close();
		} catch (Exception e) {
			iconFile.delete();
		}
	}
	@Override
	protected ArrayList<AppData> doInBackground(Boolean... slow) {
		//		Log.v("getting", "installed");
		
		ArrayList<AppData> apps = new ArrayList<AppData>();

		// use intent to get apps that can be launched
		Intent launchIntent = new Intent(Intent.ACTION_MAIN);
		launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		int appShortcut = Integer.parseInt(context.options.getString(Keys.APP_SHORTCUT, "3"));
		boolean icons = appShortcut >= CustomAdapter.ICON;
		Map<String,AppData> cache = new HashMap<String,AppData>();
		// delete icons from cache if they aren't used
		if (slow[0] || !icons) {
			MyCache.deleteIcons(context);
		}
		// copy apps information into local cache variable
		if (!slow[0]) {
			ArrayList<AppData> cacheData = new ArrayList<AppData>();
			MyCache.read(context, CACHE_NAME, cacheData);
			//			Log.v("TinyLaunch", "cache "+cacheData.size());
			for (AppData a : cacheData) {
				cache.put(a.getComponent(), a);
			}
		}
		// get list of app info from system (only those that can be launched)
		
		String name;
		boolean cacheValid;
		
		if (Build.VERSION.SDK_INT >= 21) {
			List<LauncherActivityInfo> list = ((LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE))
				.getActivityList(null, Process.myUserHandle());
			/*List<ResolveInfo> list = 
					pm.queryIntentActivities(launchIntent, 0);*/
	
			for (int i = 0 ; i < list.size() ; i++) {
				// call function to update the progress bar
				publishProgress(i, list.size());
				
				// get info of app located by index i in list
				LauncherActivityInfo info = list.get(i);
				// get package name
				ComponentName cn = info.getComponentName();
				component = cn.flattenToString();
				name = null;
				cacheValid = false;
				// if cache is not empty
				// get app data from cache
				if (!slow[0]) {
					AppData a = cache.get(component);
					if (a != null) {
						name = a.name;
						cacheValid = true;
					}
				}
				// if cache is not valid then set name from package manager
				if (!cacheValid) {
					name = info.getLabel().toString();
					if (name == null)
						name = component;
					if (name.equals("Emerald Launcher")) {
						continue;
					}
				}
				// add new appdata object to apps list
				apps.add(new AppData(component, name));
				// load icons
				if (icons) {
					// get icon file for app from cache
					File iconFile = MyCache.getIconFile(context, component);
					// if there is no icon for app in cache
					if (!cacheValid || !iconFile.exists()) {
						writeIconTo(iconFile, info.getIcon(0));
					}
				}
			}
			// when apps are retrieved
			// save apps list in cache
			MyCache.write(context, CACHE_NAME, apps);
			// clean icons of deleted apps
			MyCache.cleanIcons(context, apps);
			publishProgress(list.size(), list.size());
		} else {
			List<ResolveInfo> list = 
					pm.queryIntentActivities(launchIntent, 0);
	
			for (int i = 0 ; i < list.size() ; i++) {
				// call function to update the progress bar
				publishProgress(i, list.size());
				
				// get info of app located by index i in list
				ResolveInfo info = list.get(i);
				// get package name
				ComponentName cn = new ComponentName(info.activityInfo.packageName, 
						info.activityInfo.name);
				component = cn.flattenToString();
				name = null;
				cacheValid = false;
				// if cache is not empty
				// get app data from cache
				if (!slow[0]) {
					AppData a = cache.get(component);
					if (a != null) {
						name = a.name;
						cacheValid = true;
					}
				}
				// if cache is not valid then set name from package manager
				if (!cacheValid) {
					name = info.activityInfo.loadLabel(pm).toString();
					if (name == null)
						name = component;
					if (name.equals("Emerald Launcher")) {
						continue;
					}
				}
				// add new appdata object to apps list
				apps.add(new AppData(component, name));
				// load icons
				if (icons) {
					// get icon file for app from cache
					File iconFile = MyCache.getIconFile(context, component);
					// if there is no icon for app in cache
					if (!cacheValid || !iconFile.exists()) { // || ((Apps)context).iconPackChanged()) {
						try {
							writeIconTo(iconFile, pm.getResourcesForActivity(cn)
													.getDrawable(pm.getPackageInfo(
													info.activityInfo.packageName, 
													0).applicationInfo.icon));
						} catch (Exception e) {}
					}
				}
			}
			// when apps are retrieved
			// save apps list in cache
			MyCache.write(context, CACHE_NAME, apps);
			// clean icons of deleted apps
			MyCache.cleanIcons(context, apps);
	
			publishProgress(list.size(), list.size());
		}


		return apps;
	}

	@Override
	protected void onPreExecute() {
		//		listView.setVisibility(View.GONE);
		// sets window with progress bar
		progress = new ProgressDialog(context);
		progress.setCancelable(false);
		progress.setMessage("Getting applications...");
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setIndeterminate(true);
		progress.show();
	}

	protected void onProgressUpdate(Integer... p) {
		progress.setIndeterminate(false);
		progress.setMax(p[1]);
		progress.setProgress(p[0]);
	}

	@Override
	protected void onPostExecute(ArrayList<AppData> data) {
		// send apps list to activity
		context.loadList(data, true);
		context.options.edit().putString(Keys.PREV_APP_SHORTCUT, 
				context.options.getString(Keys.APP_SHORTCUT, "1")).commit();
		context.options.edit().putBoolean(Keys.DIRTY,false).commit();

		try {
			progress.dismiss();
		}
		catch (Exception e) {
		}
		context.loadFilteredApps();
		context.getDock().update();
	}
}
