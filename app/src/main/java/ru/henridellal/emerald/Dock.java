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
	private ArrayList<BaseData> apps;
	private ArrayList<ImageView> buttons;
	private SoftReference<Context> contextRef;
	private LinearLayout dockBar;
	private int defaultHeight;
	private boolean alwaysHide = false;
	private OnAppClickListener onAppClickListener;
	private OnAppLongClickListener onAppLongClickListener;
	
	public Dock(Context context) {
		onAppClickListener = new OnAppClickListener((Apps)context);
		onAppLongClickListener = new OnAppLongClickListener((Apps)context);
		contextRef = new SoftReference<Context>(context);
		dockBar = (LinearLayout) ((Apps)context).findViewById(R.id.dock_bar);
		defaultHeight = dockBar.getLayoutParams().height;
		apps = new ArrayList<BaseData>();
		buttons = new ArrayList<ImageView>();
		buttons.add((ImageView)((Apps)context).findViewById(R.id.button1));
		buttons.add((ImageView)((Apps)context).findViewById(R.id.button2));
		buttons.add((ImageView)((Apps)context).findViewById(R.id.button3));
		buttons.add((ImageView)((Apps)context).findViewById(R.id.button4));
		buttons.add((ImageView)((Apps)context).findViewById(R.id.button5));
	}
	public Object getApp(int index) {
		return ((apps.size() > index) ? apps.get(index) : null);
	}
	public boolean hasApp(BaseData app) {
		return apps.contains(app);
	}
	
	public void add(BaseData app) {
		if (DatabaseHelper.hasItem(contextRef.get(), app, null)) {
			apps.add(app);
			saveApps();
		}
	}
	/*public void add(int position, AppData app) {
		apps.add(position, app);
		putEntries(apps);
	}*/
	public void remove(BaseData app) {
		apps.remove(app);
		//dockContentHolder.removeView(dockContentHolder.findViewWithTag(app));
		saveApps();
	}
	// returns true if all button layouts are filled
	// must be removed after implementation of "unlimited" dock
	public boolean isFull() {
		return apps.size() == buttons.size();
	}
	public boolean isEmpty() {
		return apps.size() == 0;
	}
	public void initApps() {
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
	private void saveApps() {
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
	public void hide() {
		if (!alwaysHide) {
			ViewGroup.LayoutParams params = dockBar.getLayoutParams();
			params.height = 0;
			dockBar.setLayoutParams(params);
			dockBar.setVisibility(View.INVISIBLE);
		}
	}
	public void unhide() {
		if (!alwaysHide) {
			ViewGroup.LayoutParams params = dockBar.getLayoutParams();
			params.height = defaultHeight;
			dockBar.setLayoutParams(params);
			dockBar.setVisibility(View.VISIBLE);
		}
	}
	public void setAlwaysHide(boolean alwaysHide) {
		this.alwaysHide = alwaysHide;
	}
	public boolean isVisible() {
		return dockBar.getVisibility() == View.VISIBLE;
	}
	// updates the dock content after addition/deletion of icon
	public void update() {
		for (int i=0; i<buttons.size(); i++) {
			ImageView button = buttons.get(i);
			if (i<apps.size()) {
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
