package ru.henridellal.emerald.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import android.Manifest;

import com.commonsware.cwac.colormixer.ColorMixer;

import ru.henridellal.emerald.preference.Keys;
import ru.henridellal.emerald.R;

public class ThemerActivity extends Activity implements AdapterView.OnItemClickListener {
	private Drawable preview;
	private ListView list;
	private Point realSize;
	private SharedPreferences sharedPrefs;
	private ColorMixer colorMixer;
	private String key;
	private final String[] permissions = new String[] {
		Manifest.permission.READ_EXTERNAL_STORAGE
	};

	private final String[] keys = new String[] {
		Keys.APPS_WINDOW_BACKGROUND,
		Keys.STATUS_BAR_BACKGROUND,
		Keys.BAR_BACKGROUND,
		Keys.DOCK_BACKGROUND,
		Keys.NAV_BAR_BACKGROUND
	};

	@TargetApi(Build.VERSION_CODES.M)
	private boolean isPermissionGranted() {
		return checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT < 23) {
			preview = WallpaperManager.getInstance(this).getDrawable();
		} else {
			if (!isPermissionGranted()) {
				requestPermissions(permissions, 0);
			}
			
			if (isPermissionGranted()) {
				preview = WallpaperManager.getInstance(this).getDrawable();
			}
		}
		setContentView(R.layout.themer);
		Resources res = getResources();
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String[] options = new String[] {
				res.getString(R.string.appsBackground),
				res.getString(R.string.statusBarBackground),
				res.getString(R.string.barBackground),
				res.getString(R.string.dock_background),
				res.getString(R.string.navBarBackground)
		};
		colorMixer = (ColorMixer)(findViewById(R.id.color_mixer));
		list = (ListView)findViewById(R.id.ui_settings);
		list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options));
		list.setOnItemClickListener(this);
    	Button applyButton = (Button)findViewById(R.id.color_mixer_apply);
    	View.OnClickListener onClick = new View.OnClickListener() {
    		public void onClick(View v) {
    			findViewById(R.id.ui_settings).setVisibility(View.VISIBLE);
    			findViewById(R.id.color_mixer_holder).setVisibility(View.GONE);
    			findViewById(R.id.color_mixer_panel).setVisibility(View.GONE);
    			if (v.getId() == R.id.color_mixer_apply) {
    				sharedPrefs.edit().putInt(key, colorMixer.getColor()).commit();
    				setPreview();
    			}
    		}
    	};
    	applyButton.setOnClickListener(onClick);
    	Button cancelButton = (Button)findViewById(R.id.color_mixer_cancel);
		cancelButton.setOnClickListener(onClick);
		realSize = new Point();
		((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(realSize);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setPreview();
	}
	@Override
	public void onBackPressed() {
		if (findViewById(R.id.ui_settings).getVisibility() == View.GONE) {
			findViewById(R.id.ui_settings).setVisibility(View.VISIBLE);
    		findViewById(R.id.color_mixer_holder).setVisibility(View.GONE);
    		findViewById(R.id.color_mixer_panel).setVisibility(View.GONE);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onItemClick(AdapterView parent, View v, int position, long id) {
		int initialColor = 0;
		key = keys[position];
		initialColor = sharedPrefs.getInt(key, (position > 0) ? 0x22000000 : 0);
		colorMixer.setColor(initialColor);
		findViewById(R.id.color_mixer_panel).setVisibility(View.VISIBLE);
		findViewById(R.id.color_mixer_holder).setVisibility(View.VISIBLE);
		findViewById(R.id.ui_settings).setVisibility(View.GONE);
	}

	private void setPreview() {
		ImageView uiPreview = (ImageView)findViewById(R.id.ui_preview);
		int width = (int)(realSize.x*0.4f);
		int height = (int)(realSize.y*0.4f);
		float density = getResources().getDisplayMetrics().density;
		
		int statusBarHeight = (int) (24.f * density * 0.4f);
		int mainBarHeight = (int) (32.f * density * 0.4f);
		int navBarHeight = (int) (48.f * density * 0.4f);
		int dockHeight = (int) (56.f * density * 0.4f);
		
		Bitmap scaledBitmap;
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		if (null == preview) {
			scaledBitmap = bitmap;
		} else {
			scaledBitmap = Bitmap.createScaledBitmap(((BitmapDrawable)preview).getBitmap(), width, height, false);
		}
		canvas.drawBitmap(scaledBitmap, 0, 0, null);
		Paint paint = new Paint();
		// APP BACKGROUND
		int appBackgroundColor = sharedPrefs.getInt(Keys.APPS_WINDOW_BACKGROUND, 0x00000000);
		paint.setColor(appBackgroundColor);
		canvas.drawRect(0, 0, width, height, paint);
		
		// STATUS BAR
		int statusBarColor = (Build.VERSION.SDK_INT >= 21) ? sharedPrefs.getInt(Keys.STATUS_BAR_BACKGROUND, 0x22000000) : Color.BLACK;
		paint.setColor(statusBarColor);
		canvas.drawRect(0, 0, width, statusBarHeight, paint);
		
		// MAIN BAR
		int mainBarColor = sharedPrefs.getInt(Keys.BAR_BACKGROUND, 0x22000000);
		paint.setColor(mainBarColor);
		if (sharedPrefs.getBoolean(Keys.BOTTOM_MAIN_BAR, false)) {
			canvas.drawRect(0, height-navBarHeight-dockHeight-mainBarHeight, width, height-navBarHeight-dockHeight, paint);
		} else {
			canvas.drawRect(0, statusBarHeight, width, statusBarHeight+mainBarHeight, paint);
		}
		
		// NAVIGATION BAR
		int navBarColor = (Build.VERSION.SDK_INT >= 19) ? sharedPrefs.getInt(Keys.NAV_BAR_BACKGROUND, 0x22000000) : Color.BLACK;
		paint.setColor(navBarColor);
		canvas.drawRect(0, height-navBarHeight, width, height, paint);
		
		// DOCK
		int dockBarColor = sharedPrefs.getInt(Keys.DOCK_BACKGROUND, 0x22000000);
		paint.setColor(dockBarColor);
		canvas.drawRect(0, height-navBarHeight-dockHeight, width, height-navBarHeight, paint);
		
		uiPreview.setImageBitmap(bitmap);
	}
}
