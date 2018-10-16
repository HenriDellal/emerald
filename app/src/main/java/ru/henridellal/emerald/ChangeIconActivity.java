package ru.henridellal.emerald;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Build;
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
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

public class ChangeIconActivity extends Activity implements View.OnClickListener {
	public final static String COMPONENT_NAME = "ru.henridellal.emerald.component_name";
	public final static String SHORTCUT_NAME = "ru.henridellal.emerald.shortcut_name";
	private GridView iconGrid;
	private IconGridAdapter iconGridAdapter;
	private String component;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		final String shortcutName = intent.getStringExtra(SHORTCUT_NAME);
		setTitle(String.format(getResources().getString(R.string.change_icon_title), shortcutName));
		setContentView(R.layout.change_icon_activity);
		component = intent.getStringExtra(COMPONENT_NAME);
		EditText iconSearchBar = (EditText)findViewById(R.id.icon_search_bar);
		final File customIconFile = MyCache.getCustomIconFile(this, component);
		if (customIconFile.exists()) {
			initResetButton();
		}
		((Button)findViewById(R.id.choose_icon_from_memory)).setOnClickListener(this);
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
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.reset_icon:
				File customIconFile = MyCache.getCustomIconFile(this, component);
				customIconFile.delete();
				Toast.makeText(ChangeIconActivity.this, "The custom icon was deleted", Toast.LENGTH_LONG).show();
				((Button)findViewById(R.id.reset_icon)).setEnabled(false);
				break;
			case R.id.choose_icon_from_memory:
				if (Build.VERSION.SDK_INT >= 19) {
					Intent customIconIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
					customIconIntent.addCategory(Intent.CATEGORY_OPENABLE);
					customIconIntent.setType("image/png");
					startActivityForResult(customIconIntent, 0);
				} else {
					Intent intent = new Intent(this, FileActivity.class);
					startActivityForResult(intent, FileActivity.GET_IMAGE);
				}
				break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Uri fileUri;
		if (resultCode == RESULT_OK) {
			if (requestCode == FileActivity.GET_IMAGE) {
				fileUri = Uri.fromFile(new File(data.getStringExtra(FileActivity.RESULT_PATH)));
				saveCustomIcon(component, fileUri);
				initResetButton();
			} else if (null != data) {
				fileUri = data.getData();
				if (null != fileUri) {
					saveCustomIcon(component, fileUri);
					initResetButton();
				}
			}
		}
	}
	
	public void saveCustomIcon(String appComponent, Uri fileUri) {
		File customIconFile = MyCache.getCustomIconFile(this, appComponent);
		try {
			if (null != customIconFile) {
				FileDescriptor fd = getContentResolver().openAssetFileDescriptor(fileUri, "r").getFileDescriptor();
				FileInputStream fis = new FileInputStream(fd);
				FileOutputStream fos = new FileOutputStream(customIconFile);
				FileChannel src = fis.getChannel();
				FileChannel dst = fos.getChannel();
				dst.transferFrom(src, 0, src.size());
				fis.close();
				fos.close();
				dst.close();
				src.close();
			}
		} catch (Exception e) {}
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
	
	private void initResetButton() {
		((Button)findViewById(R.id.reset_icon)).setEnabled(true);
		((Button)findViewById(R.id.reset_icon)).setOnClickListener(this);
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
				v = inflater.inflate(R.layout.custom_icon, parent, false);
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
