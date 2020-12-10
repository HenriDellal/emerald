package ru.henridellal.emerald;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.ref.SoftReference;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Dock {
	private static int[] buttonIds = {
			R.id.button1, R.id.button2,
			R.id.button3, R.id.button4,
			R.id.button5
		};

	private static ArrayList<BaseData> apps;
	private static ArrayList<ImageView> buttons;
	private static SoftReference<Context> contextRef;
	private static LinearLayout dockBar;
	private static int defaultHeight;
	private static boolean alwaysHide = false;
	private static OnAppClickListener onAppClickListener;
	private static View.OnLongClickListener onAppLongClickListener;
	
	public static void init(Context context) {
		Apps mainActivity = (Apps) context;
		onAppClickListener = new OnAppClickListener(mainActivity);
		if (mainActivity.options.getString(Keys.PASSWORD, "").length() > 0) {
			onAppLongClickListener = new OnAppUnlockLongClickListener(context);
		} else {
			onAppLongClickListener = new OnAppLongClickListener(mainActivity);
		}
		contextRef = new SoftReference<Context>(context);
		dockBar = (LinearLayout) mainActivity.findViewById(R.id.dock_bar);
		defaultHeight = dockBar.getLayoutParams().height;
		apps = new ArrayList<BaseData>();
		addButtons(mainActivity);
	}

	private static void addButtons(Apps mainActivity) {
		buttons = new ArrayList<ImageView>();
		for (int i = 0; i < 5; i++)
			buttons.add((ImageView)mainActivity.findViewById(buttonIds[i]));
	}

	public static Object getApp(int index) {
		return ((apps.size() > index) ? apps.get(index) : null);
	}
	public static boolean hasApp(BaseData app) {
		return apps.contains(app);
	}
	
	public static void add(BaseData app) {
		if (DatabaseHelper.hasItem(contextRef.get(), app, null)) {
			apps.add(app);
			saveApps();
		}
	}

	public static void remove(BaseData app) {
		apps.remove(app);
		saveApps();
	}

	public static void remove(String component) {
		for (BaseData app: apps) {
			if (component.equals(app.getId())) {
				remove(app);
				return;
			}
		}
	}

	// TODO remove after implementation of "unlimited" dock
	public static boolean isFull() {
		return apps.size() == buttons.size();
	}

	public static boolean isEmpty() {
		return apps.size() == 0;
	}

	public static void initApps() {
		BufferedReader reader = null;
		boolean needSave = false;
		apps.clear();
		File f = new File(contextRef.get().getFilesDir(), "dock");
		try {
			if (!f.exists()) {
				f.createNewFile();
				update();
				return;
			}
			reader = new BufferedReader(new FileReader(f));
			String data;
			while (null != (data = reader.readLine())) {
				BaseData a;
				if (data.startsWith(AppData.COMPONENT)) {
					a = new AppData();
					a.read(reader, data);
					if (DatabaseHelper.hasItem(contextRef.get(), a, null)) {
						apps.add(a);
					}
				} else if (data.startsWith(ShortcutData.SHORTCUT_NAME)) {
					a = new ShortcutData();
					a.read(reader, data);
					apps.add(a);
				} else {
					data = data.trim();
					if (data.length()>0 ) {
						apps.add(new AppData(data, ""));
					}
						//Toast.makeText(contextRef.get(), data, Toast.LENGTH_LONG).show();
					needSave = true;
				}
			}
		} catch (FileNotFoundException e) {
			Toast.makeText(contextRef.get(), " "+e, Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(contextRef.get(), " "+e, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(contextRef.get(), " "+e, Toast.LENGTH_LONG).show();
		}
		
		if (reader != null)
			try {
				reader.close();
			} catch (IOException e) {
			}
		update();
		if (needSave) {
			saveApps();
		}
	}

	//writes app data into dock file
	private static void saveApps() {
		BufferedWriter writer = null;
		try {
			File file = new File(contextRef.get().getFilesDir(), "dock");
			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(file));

			for (BaseData a : apps) {
				a.write(writer);
			}
		} catch (IOException e) {
			Toast.makeText(contextRef.get(), " "+e, Toast.LENGTH_LONG).show();
		}

		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
			}		
		}
	}

	public static void hide() {
		if (!alwaysHide) {
			ViewGroup.LayoutParams params = dockBar.getLayoutParams();
			params.height = 0;
			dockBar.setLayoutParams(params);
			dockBar.setVisibility(View.INVISIBLE);
		}
	}

	public static void unhide() {
		if (!alwaysHide) {
			ViewGroup.LayoutParams params = dockBar.getLayoutParams();
			params.height = defaultHeight;
			dockBar.setLayoutParams(params);
			dockBar.setVisibility(View.VISIBLE);
		}
	}

	public static void setAlwaysHide(boolean value) {
		alwaysHide = value;
	}

	public static boolean isVisible() {
		return dockBar.getVisibility() == View.VISIBLE;
	}

	// updates the dock content after addition/deletion of icon
	public static void update() {
		for (int i = 0; i < buttons.size(); i++) {
			ImageView button = buttons.get(i);
			if (i < apps.size()) {
				button.setVisibility(View.VISIBLE);
				IconPackManager.setIcon(contextRef.get(), buttons.get(i), apps.get(i));
				button.setTag(apps.get(i));
				button.setOnClickListener(onAppClickListener);
				button.setOnLongClickListener(onAppLongClickListener);
			} else {
				button.setVisibility(i > 0 ? View.GONE : View.INVISIBLE);
				button.setImageResource(android.R.color.transparent);
				button.setOnClickListener(null);
				button.setOnLongClickListener(null);
			}
		}
		if (apps.size() == 0) {
			hide();
		} else if (apps.size() == 1) {
			unhide();
		}
	}
}
