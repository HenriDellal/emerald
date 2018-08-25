package ru.henridellal.emerald;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

public class ChangeIconActivity extends Activity{
	public final static String COMPONENT_NAME = "ru.henridellal.emerald.component_name";
	public final static String SHORTCUT_NAME = "ru.henridellal.emerald.shortcut_name";
	private GridView iconGrid;
	private IconGridAdapter iconGridAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		final String shortcutName = intent.getStringExtra(SHORTCUT_NAME);
		setTitle(String.format(getResources().getString(R.string.change_icon_title), shortcutName));
		setContentView(R.layout.change_icon_activity);
		final String component = intent.getStringExtra(COMPONENT_NAME);
		EditText iconSearchBar = (EditText)findViewById(R.id.icon_search_bar);
		final File customIconFile = MyCache.getCustomIconFile(this, component);
		if (customIconFile.exists()) {
			((Button)findViewById(R.id.reset_icon)).setEnabled(true);
			((Button)findViewById(R.id.reset_icon)).setOnClickListener(
				new View.OnClickListener () {
					public void onClick(View v) {
						try {
							customIconFile.delete();
							Toast.makeText(ChangeIconActivity.this, "The custom icon was deleted", Toast.LENGTH_LONG).show();
							((Button)findViewById(R.id.reset_icon)).setEnabled(false);
						} catch (Exception e) {
							
						}
					}
				}
			);
		}
		iconGrid = (GridView)findViewById(R.id.icon_grid);
		iconGridAdapter = new IconGridAdapter(this, shortcutName);
		iconGridAdapter.setComponent(component);
		iconGrid.setAdapter(iconGridAdapter);
		iconSearchBar.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				iconGridAdapter.filter(s);
			}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	public void saveCustomIcon(String appComponent, String iconComponent) {
		File customIconFile = MyCache.getCustomIconFile(this, appComponent);
		Bitmap customBitmap = LauncherApp.getInstance().getIconPackManager().getBitmap(iconComponent);
		if (customIconFile != null) {
			try {
				// save icon in cache
				FileOutputStream out = new FileOutputStream(customIconFile);
				customBitmap.compress(CompressFormat.PNG, 100, out);
				out.close();
			} catch (Exception e) {
				customIconFile.delete();
			}
		}
	}
	public class IconGridAdapter extends BaseAdapter {
		private ArrayList<String> iconsArray, toDisplay;
		private ImageView icon;
		private TextView text;
		private String component;
		private View.OnClickListener onClickListener;
		public void filter(CharSequence searchInput) {
			toDisplay.clear();
			for (String iconComponent: iconsArray) {
				if (iconComponent.toLowerCase().contains(searchInput.toString().toLowerCase())) {
					toDisplay.add(iconComponent);
				}
			}
			notifyDataSetChanged();
		}
		
		public void setComponent(String component) {
			this.component = component;
		}
		
		public IconGridAdapter(Context context, final String shortcutName) {
			super();
			Map<String, String> iconsList = LauncherApp.getInstance().getIconPackManager().getIcons();
			Set<String> iconsSet = iconsList.keySet();
			iconsArray = new ArrayList<String>(iconsSet);
			Collections.sort(iconsArray);
			toDisplay = new ArrayList<String>(iconsArray);
			onClickListener = new View.OnClickListener() {
				public void onClick(View view) {
					final Object tag = view.getTag();
					AlertDialog.Builder builder = new AlertDialog.Builder(ChangeIconActivity.this);
					builder.setMessage(String.format(getResources().getString(R.string.change_icon_question), shortcutName));
					builder.setPositiveButton(android.R.string.yes, 
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								saveCustomIcon(component, tag.toString());
								finish();
							}
						}).setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								
							}
						}).setCancelable(true);
						builder.create().show();
				}
			};
		}
		@Override
		public boolean isEnabled(int position) {
			return true;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v;
			final int i = position;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.iconbutton, parent, false);
			} else {
				v = convertView;
			}
			icon = (ImageView)v.findViewById(R.id.icon);
			icon.setImageBitmap(LauncherApp.getInstance().getIconPackManager().getBitmap(toDisplay.get(i)));
			text = (TextView)v.findViewById(R.id.text);
			text.setText(toDisplay.get(i));
			v.setTag(toDisplay.get(i));
			v.setOnClickListener(onClickListener);
			return v;
		}
		@Override
		public int getCount() {
			return toDisplay.size();
		}
	
		@Override
		public Object getItem(int position) {
			return null;
		}
		
		@Override
		public long getItemId(int position) {
			return 0;
		}
	}
}
