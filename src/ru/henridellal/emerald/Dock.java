package ru.henridellal.emerald;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.ref.SoftReference;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class Dock {
	private ArrayList<BaseData> apps;
	private ArrayList<ImageView> buttons;
	private SoftReference<Context> contextRef;
	private LinearLayout dockBar;
	private int defaultHeight;
	private boolean alwaysHide = false;
	
	public Dock(Context context) {
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
		//update();
	}
	/*
		dockContentHolder.removeView(findViewWithTag(app));
	*/
	public Object getApp(int index) {
		return ((apps.size() > index) ? apps.get(index) : null);
	}
	public boolean hasApp(BaseData app) {
		return apps.contains(app);
	}
	public void add(BaseData app) {
		apps.add(app);
		/*try {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.dock_icon_button, (ViewGroup)dockContentHolder, false);
		ImageView img = (ImageView)v.findViewById(R.id.dock_icon);
		img.setTag(app);
		IconPackManager.setIcon(context, img, app);
		ViewGroup.LayoutParams params = img.getLayoutParams();
		//params.weight = 1.0f;
		params.width = 48;
		img.setLayoutParams(params);
		img.setVisibility(View.VISIBLE);
		//params.gravity = Gravity.CENTER_VERTICAL;
		//item.addView(img);
		//dockContentHolder.addView(img);
		Toast.makeText(context, " "+(v.getWidth())+(params.width), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(context, " "+e, Toast.LENGTH_LONG).show();
		}*/
		saveApps();
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
	//receives data from dock file if there is one
	public void initApps(Map<String, ? extends BaseData> map) {
		apps = new ArrayList<BaseData>();
		BufferedReader reader = null;
		File f = new File(contextRef.get().getFilesDir(), "dock");
		try {
			if (!f.exists()) {
				f.createNewFile();
				update();
				return;
			}
			reader = new BufferedReader(new FileReader(f));
			
			String d;
			
			while (null != (d = reader.readLine())) {
				d = d.trim();
				if (d.length()>0 ) {
					BaseData a = map.get(d);
					if (a != null)
						apps.add(a);
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
				writer.write(a.getComponent() + "\n");
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
			if (i<apps.size() && ((Apps)contextRef.get()).hasApp(apps.get(i))) {
				button.setVisibility(View.VISIBLE);
				IconPackManager.setIcon(contextRef.get(), buttons.get(i), apps.get(i));
				button.setTag(apps.get(i));
				button.setOnClickListener(new OnAppClickListener((Apps)contextRef.get()));
			} else {
				button.setVisibility(i > 0 ? View.GONE : View.INVISIBLE);
				button.setImageResource(android.R.color.transparent);
				button.setOnClickListener(null);
				//button.setOnLongClickListener(null);
			}
		}
		if (apps.size() == 0) {
			hide();
		} else if (apps.size() == 1) {
			unhide();
		}
	}
}
