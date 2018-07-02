package ru.henridellal.emerald;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
//import android.util.Log;
import android.view.GestureDetector;
import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class Apps extends Activity
{
	private GestureDetector gestureDetector;
	private CategoryManager categories;
	private ArrayList<BaseData> curCatData;
	private RelativeLayout mainLayout;
	private GridView grid;
	private Dock dock;
	private Map<String,BaseData> map;
	public SharedPreferences options;
	public static final String PREF_APPS = "apps";
	public static final String APP_TAG = "Emerald";
	private CustomAdapter adapter;
	public static final int GRID = 0;
	public static final int LIST = 1;
	private GetApps scanner;
	private OnSharedPreferenceChangeListener prefListener;
	private boolean lock, searchIsOpened, homeButtonPressed, modPressed;
	private int historySize;
	
	public void loadList(boolean cleanCategory) {
		ArrayList<BaseData> data = new ArrayList<BaseData>(); 
		MyCache.read(this, GetApps.CACHE_NAME, data);
		loadList(data, cleanCategory);
	}
	public Dock getDock() {
		return dock;
	}
	public boolean hasApp(BaseData app) {
		return (map.get(app.getComponent()) != null);
	}
	/*returns map with pairs of package names 
	and AppData related to them*/
	private Map<String, BaseData> makeMap(ArrayList<BaseData> data) {
		Map<String, BaseData> map = new HashMap<String, BaseData>();

		for (BaseData a : data)
			map.put(a.getComponent(), a);
		return map;
	}

	public void loadList(ArrayList<BaseData> data, boolean cleanCategory) {
		loadList(makeMap(data), cleanCategory);
	}

	public void loadList(Map<String, BaseData> map, boolean cleanCategory) {
		this.map = map;

		if (categories == null) {
			LauncherApp.getInstance().getCategoryManager().setInitialMap(map);
			categories = LauncherApp.getInstance().getCategoryManager();	
		}
		else {
			categories.setMap(map);
		}

		if (cleanCategory)
			categories.cleanCategories();

		loadFilteredApps();
	}
	
	public void changePrefsOnRotate() {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			//Log.v(APP_TAG, "loadFilteredApps : Portrait orientation");
			adapter.setIconSize((int)(options.getInt(Keys.ICON_SIZE, getResources().getInteger(R.integer.icon_size_default)) * getResources().getDisplayMetrics().density));
	    	adapter.setTextSize((int)(options.getInt(Keys.TEXT_SIZE, getResources().getInteger(R.integer.text_size_default)) * getResources().getDisplayMetrics().density));
	    	grid.setVerticalSpacing((int)(options.getInt(Keys.VERTICAL_SPACING, getResources().getInteger(R.integer.vertical_spacing_default)) * getResources().getDisplayMetrics().density));
	    	if (options.getBoolean(Keys.TILE, true)) {
	    		grid.setColumnWidth((int)(options.getInt(Keys.COLUMN_WIDTH, getResources().getInteger(R.integer.column_width_default)) * getResources().getDisplayMetrics().density));
			} else {
	    		grid.setNumColumns(1);
	    	}
	    	if (!(options.getBoolean(Keys.DOCK_IN_LANDSCAPE, true))) {
	    		dock.setAlwaysHide(false);
	    		dock.unhide();
	    	}
		} else {
			//Log.v(APP_TAG, "loadFilteredApps : orientation");
	    	adapter.setIconSize((int)(options.getInt(Keys.ICON_SIZE_LANDSCAPE, getResources().getInteger(R.integer.icon_size_land_default)) * getResources().getDisplayMetrics().density));
			adapter.setTextSize((int)(options.getInt(Keys.TEXT_SIZE_LANDSCAPE, getResources().getInteger(R.integer.text_size_land_default)) * getResources().getDisplayMetrics().density));
			grid.setVerticalSpacing((int)(options.getInt(Keys.VERTICAL_SPACING_LANDSCAPE, getResources().getInteger(R.integer.vertical_spacing_land_default)) * getResources().getDisplayMetrics().density));
	    	if (options.getBoolean(Keys.TILE, true)) {
	    		grid.setColumnWidth((int)(options.getInt(Keys.COLUMN_WIDTH_LANDSCAPE, getResources().getInteger(R.integer.column_width_land_default)) * getResources().getDisplayMetrics().density));
	    	} else {
	    		grid.setNumColumns(2);
	    		grid.setColumnWidth(-1);
	    	}
	    	if (!(options.getBoolean(Keys.DOCK_IN_LANDSCAPE, true))) {
	    		dock.hide();
	    		dock.setAlwaysHide(true);
	    	}
		}
	}
	public void loadFilteredApps() {
		//Log.v(APP_TAG, "Loading filtered apps");	
		//Log.v(APP_TAG, "filtering");
		curCatData = categories.filterApps(map);
		//Log.v(APP_TAG, "filtered");
		adapter.update(curCatData);
		if (!options.getBoolean(Keys.HIDE_MAIN_BAR, false)) {
			((Button)findViewById(R.id.category_button)).setText(categories.getCategory(categories.getCurCategory()).getRepresentName(this));
		}
		//Log.v(APP_TAG, "loadFilteredApps : finished");
	}
	//handles history filling
	private void addInHistory(BaseData a) {
    //removes app from history if it is already in it
    // to avoid duplicating
    	//Log.v(APP_TAG, "Add app in history");
    	if (!dock.hasApp(a)) {
			if (categories.getCategoryData(CategoryManager.HISTORY).indexOf(a) != -1) {
				categories.removeFromCategory(CategoryManager.HISTORY, a);
			}
			categories.addToHistory(a);
	    	//removes old entries if History has maximum size
			if (categories.getCategoryData(CategoryManager.HISTORY).size() > historySize) {
				categories.removeFromCategory(CategoryManager.HISTORY, categories.getCategoryData(CategoryManager.HISTORY).size()-1);
			}
    	}
	}
	//launches app and adds it to history
	public void launch(AppData a) {
		//Log.v(APP_TAG, "User launched an app");
		if (!categories.in(a, CategoryManager.HIDDEN))
			addInHistory(a);
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_LAUNCHER);
		i.setComponent(ComponentName.unflattenFromString(
				a.getComponent()));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		try {
			startActivity(i);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "Activity is not found", Toast.LENGTH_LONG).show();
		} finally {
			if (searchIsOpened) {
				closeSearch();
			}
		}
	}
	public void launch(ShortcutData shortcut) {
		//Log.v(APP_TAG, "User launched an app");
		if (!categories.in(shortcut, CategoryManager.HIDDEN))
			addInHistory(shortcut);
		try {
			startActivity(Intent.parseUri(shortcut.getUri(), 0));
		} catch (Exception e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	private void openCategoriesList() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		final ArrayList<String> cats = new ArrayList<String>(categories.getCategories());
		cats.remove(CategoryManager.HIDDEN);
		ArrayList<String> toRemove = new ArrayList<String>();
		for (String category: cats) {
			if (categories.getCategory(category).isHidden())
				toRemove.add(category);
		}
		cats.removeAll(toRemove);
		toRemove = null;
		final ArrayList<String> categoriesNames = new ArrayList<String>(cats.size());
		for (String category: cats) {
			categoriesNames.add(categories.getCategory(category).getRepresentName(this));
		}
		builder.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categoriesNames), 
			new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface di, int catNum) {
				String newCat = cats.get(catNum);
				if (!newCat.equals(categories.getCurCategory())) {
					categories.setCurCategory(newCat);
					loadFilteredApps();
				}
			}
		});
		if (!isFinishing())
			builder.create().show();
	}
	//launches popup window for editing apps
	private void itemEdit(final BaseData item) {
		//Log.v(APP_TAG, "Open app edit window");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(item.name);
		builder.setCancelable(true);

		ArrayList<String> editableCategories =  categories.getEditableCategories();
		final int nCategories = editableCategories.size();
		ArrayList<String> editableCategoriesNames = new ArrayList<String>(nCategories);
		final int position = grid.getFirstVisiblePosition();
		for (String category: editableCategories) {
			editableCategoriesNames.add(categories.getCategory(category).getRepresentName(this));
		}
		if (nCategories > 0) {
			final String[] editableCategoriesArray = new String[nCategories];
			editableCategories.toArray(editableCategoriesArray);
			final String[] editableCategoriesNamesArray = new String[nCategories];
			editableCategoriesNames.toArray(editableCategoriesNamesArray);
			
			final boolean[] checked = new boolean[nCategories];			

			for (int i = 0; i < nCategories ; i++) {
				checked[i] = categories.in(item, editableCategoriesArray[i]);
			}

			final boolean[] oldChecked = checked.clone();

			builder.setMultiChoiceItems(editableCategoriesNamesArray, checked, 
				new DialogInterface.OnMultiChoiceClickListener() {							
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				}
			});
			builder.setPositiveButton("OK", new OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					for (int i = 0 ; i < nCategories ; i++) {
						if (checked[i] && ! oldChecked[i])
							categories.addToCategory(editableCategoriesArray[i], item);
						else if (!checked[i] && oldChecked[i])
							categories.removeFromCategory(editableCategoriesArray[i], item);
					}
					loadFilteredApps();
					grid.setSelection(position);
				}});
		}
		if (!isFinishing())
			builder.create().show();
	}
	public void itemContextMenu(final AppData item) {
		//Log.v(APP_TAG, "Open app edit window");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(item.name);
		builder.setCancelable(true);
		
		String[] commands = new String[]{
			getResources().getString(R.string.aboutTitle),
			getResources().getString(R.string.findInMarket),
			getResources().getString(R.string.editAppCategories),
			getResources().getString(R.string.uninstall),
			(dock.hasApp(item)) ? 
				getResources().getString(R.string.remove_from_dock): 
				getResources().getString(R.string.add_to_dock),
			getResources().getString(R.string.change_icon)
		};
		builder.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, commands),
		new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface di, int which) {
				Uri uri;
				switch(which) {
					case 0:
						uri = Uri.parse("package:"+ComponentName.unflattenFromString(
							item.getComponent()).getPackageName());
						startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
						break;
					case 1:
						uri = Uri.parse("market://details?id="+ComponentName.unflattenFromString(
							item.getComponent()).getPackageName());
						startActivity(new Intent(Intent.ACTION_VIEW, uri));
						break;
					case 2:
						itemEdit(item);
						break;
					case 3:
						uri = Uri.parse("package:"+ComponentName.unflattenFromString(
							item.getComponent()).getPackageName());
						startActivity(new Intent(Intent.ACTION_DELETE, uri));
						break;
					case 4:
						if (dock.hasApp(item)) {
							dock.remove(item);
						} else {
							if (!dock.isFull()) {
								dock.add(item);
							} else {
								Toast.makeText(Apps.this, getResources().getString(R.string.dock_is_full), Toast.LENGTH_LONG).show();
							}
						}
						dock.update();
						break;
					case 5:
						Intent intent = new Intent(Apps.this, ChangeIconActivity.class);
						intent.putExtra(ChangeIconActivity.COMPONENT_NAME, item.getComponent());
						intent.putExtra(ChangeIconActivity.SHORTCUT_NAME, item.name);
						startActivity(intent);
						break;
				}
			}
		});
		if (!isFinishing())
			builder.create().show();
	}
	
	public void itemContextMenu(final ShortcutData item) {
		//Log.v(APP_TAG, "Open app edit window");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(item.getName());
		builder.setCancelable(true);
		
		String[] commands = new String[]{
			getResources().getString(R.string.editAppCategories),
			getResources().getString(R.string.uninstall),
			(dock.hasApp(item)) ? 
				getResources().getString(R.string.remove_from_dock): 
				getResources().getString(R.string.add_to_dock),
			getResources().getString(R.string.change_icon)
		};
		builder.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, commands),
		new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface di, int which) {
				Uri uri;
				switch(which) {
					case 0:
						itemEdit(item);
						break;
				/*	case 1:
						startActivity(new Intent(Intent.ACTION_DELETE, uri));
						break;*/
					case 2:
						if (dock.hasApp(item)) {
							dock.remove(item);
						} else {
							if (!dock.isFull()) {
								dock.add(item);
							} else {
								Toast.makeText(Apps.this, getResources().getString(R.string.dock_is_full), Toast.LENGTH_LONG).show();
							}
						}
						dock.update();
						break;
				/*	case 3:
						Intent intent = new Intent(Apps.this, ChangeIconActivity.class);
						intent.putExtra(ChangeIconActivity.COMPONENT_NAME, item.getComponent());
						intent.putExtra(ChangeIconActivity.SHORTCUT_NAME, item.getName());
						startActivity(intent);
						break;*/
				}
			}
		});
		if (!isFinishing())
			builder.create().show();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//Log.v(APP_TAG, "Configuration changed");
		super.onConfigurationChanged(newConfig);
		changePrefsOnRotate();
		loadFilteredApps();
	}
	
	private void menu() {
		//Log.v(APP_TAG, "Trying to open menu");
		if (lock) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			//builder.setTitle("");
			builder.setMessage(getResources().getString(R.string.type_password));
			final EditText inputBox = new EditText(this);
			inputBox.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
			builder.setView(inputBox);
			builder.setPositiveButton(android.R.string.yes, 
				new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (inputBox.getText().toString().equals(options.getString(Keys.PASSWORD, ""))) {
						openOptionsMenu();
					} else {
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
					}
			} });
			builder.setCancelable(true);
			if (!isFinishing()) {
				builder.show();
			}
		} else {
			openOptionsMenu();
		}
	}
	private boolean isDefaultLauncher() {
		final Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		final ResolveInfo resolveInfo =
				getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo != null &&
				getPackageName().equals(resolveInfo.activityInfo.packageName);
	}
	
	protected void onMenuButton(View v) {
		menu();
	}
	public void searchInWeb(String text) {
		String site = options.getString(Keys.SEARCH_PROVIDER, "https://duckduckgo.com/?q=");
		String url = site + text;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "Cannot handle "+ url + " request", Toast.LENGTH_LONG).show();
		}
	}
	public void openSearch() {
		//Log.v(APP_TAG, "Start searching");
		categories.setCurCategory(CategoryManager.ALL);
		loadFilteredApps();
		if (options.getBoolean(Keys.HIDE_MAIN_BAR, false)) {
			findViewById(R.id.main_bar).setVisibility(View.VISIBLE);
		}
		findViewById(R.id.tabs).setVisibility(View.GONE);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		final EditText text = (EditText)findViewById(R.id.textField);
		if (imm != null) {
			imm.showSoftInput(grid, InputMethodManager.SHOW_IMPLICIT);
		}
		findViewById(R.id.searchBar).setVisibility(View.VISIBLE);
		text.setVisibility(View.VISIBLE);
		findViewById(R.id.webSearchButton).setVisibility(View.VISIBLE);
		if (dock.isVisible()) {
			dock.hide();
		}
		text.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				adapter.filter(s);
			}
		});
		View.OnClickListener onClick = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchInWeb(text.getText().toString());
			}
		};
		findViewById(R.id.webSearchButton).setOnClickListener(onClick);
		searchIsOpened = true;
		text.requestFocusFromTouch();
	}
	public void closeSearch() {
		//Log.v(APP_TAG, "Quit search");
		//hideKeyboard();
		EditText text = (EditText)findViewById(R.id.textField);
		text.setText("");
		findViewById(R.id.searchBar).setVisibility(View.GONE);
		findViewById(R.id.webSearchButton).setVisibility(View.GONE);
		text.setVisibility(View.GONE);
		hideMainBarIfNeeded();
		if (!dock.isEmpty()) {
			dock.unhide();
		}
		searchIsOpened=false;
	}
	private void hideMainBarIfNeeded() {
		if (options.getBoolean(Keys.HIDE_MAIN_BAR, false)) {
			findViewById(R.id.main_bar).setVisibility(View.GONE);
		} else {
			findViewById(R.id.tabs).setVisibility(View.VISIBLE);
		}
	}
	public void onMyClick(View v) {
		switch(v.getId()) {
			case R.id.searchButton:
				openSearch();
				break;
			case R.id.category_button:
				openCategoriesList();
				break;
			case R.id.menuButton:
				menu();
				break;
			case R.id.quit_hidden_apps:
				categories.setCurCategory(CategoryManager.ALL);
				v.setVisibility(View.GONE);
				hideMainBarIfNeeded();
				loadFilteredApps();
				break;
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return super.dispatchTouchEvent(event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			menu();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			//Log.v(APP_TAG, "BACK pressed");
			if (searchIsOpened) {
				closeSearch();
			} else if (!isDefaultLauncher()) {
				finish();
				//moveTaskToBack(false);
				return true;
			} else {
				if (categories.getCurCategory().equals(CategoryManager.HIDDEN)) {
					findViewById(R.id.quit_hidden_apps).setVisibility(View.GONE);
					hideMainBarIfNeeded();
				}
				categories.prevCategory();
			}
			loadFilteredApps();
			return true;
		} else if (KeyEvent.isModifierKey(keyCode)) {
			modPressed = !modPressed;
			return true;
		} else if (modPressed && getCurrentFocus().getId() != R.id.textField) {
			if (keyCode >= KeyEvent.KEYCODE_1 && keyCode <= KeyEvent.KEYCODE_9) {
				Object app = dock.getApp(keyCode-KeyEvent.KEYCODE_1);
				if (app != null) {
					if (app instanceof AppData) {
						launch((AppData)app);
					} else if (app instanceof ShortcutData) {
						launch((ShortcutData)app);
					}
					return true;
				} else {
					return false;
				}
			} else if (keyCode == KeyEvent.KEYCODE_0 && !searchIsOpened) {
				openSearch();
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				categories.setCurCategory(categories.getCategory(CategoryManager.PREVIOUS));
				loadFilteredApps();
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				categories.setCurCategory(categories.getCategory(CategoryManager.NEXT));
				loadFilteredApps();
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				openCategoriesList();
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
			View view = getCurrentFocus();
			if (view != null) {
				Object viewTag = view.getTag();
				if (viewTag instanceof AppData) {
					launch((AppData)viewTag);
					return true;
				} else if (viewTag instanceof ShortcutData) {
					launch((ShortcutData)viewTag);
					return true;
				}
				return false;
			}
			return false;
		} else {
			return false;
		}
	}
	
	@Override
	public void onBackPressed() {}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onPause() {
		//Log.v(APP_TAG, "onPause");
		super.onPause();
		modPressed = false;
		if (searchIsOpened) {
			closeSearch();
		}
	}
	@Override
	protected void onDestroy() {
		//Log.v(APP_TAG, "onDestroy");
		options.unregisterOnSharedPreferenceChangeListener(prefListener);
		grid.setOnScrollListener(null);
		grid.setOnTouchListener(null);
		prefListener = null;
		super.onDestroy();
	}
	
	@Override
	protected void onNewIntent(Intent i) {
		//Log.v(APP_TAG, "onNewIntent");
		homeButtonPressed = true;
		if (categories == null) {
			loadList(false);
		}
		if (categories.getCurCategory().equals(CategoryManager.HIDDEN)) {
			findViewById(R.id.quit_hidden_apps).setVisibility(View.GONE);
			hideMainBarIfNeeded();
			categories.setCurCategory(categories.getHome());
		} else if (categories.getCurCategory().equals(categories.getHome())) {
			String newCategory = options.getString(Keys.HOME_BUTTON, "");
			if (newCategory.length() > 0) {
				categories.setCurCategory(newCategory);
			} else {
				categories.setCurCategory(categories.getHome());
			}
		} else {
			categories.setCurCategory(categories.getHome());
		}
		loadFilteredApps();
		super.onNewIntent(i);
	}
	
	private void layoutInit() {
		mainLayout = new RelativeLayout(this);
		LayoutInflater layoutInflater = (LayoutInflater) 
			this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		LinearLayout dockBar = (LinearLayout) layoutInflater.inflate(R.layout.dock_bar, mainLayout, false);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dockBar.getLayoutParams());
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		dockBar.setLayoutParams(layoutParams);
		dockBar.setBackgroundColor(options.getInt(Keys.DOCK_BACKGROUND, 0x22000000));
		mainLayout.addView(dockBar);
		
		FrameLayout mainBar = (FrameLayout) layoutInflater.inflate(R.layout.main_bar, mainLayout, false);
		grid = (GridView) layoutInflater.inflate(R.layout.apps_grid, mainLayout, false);
		boolean kitkatNoImmersiveMode = (Build.VERSION.SDK_INT == 19 && !options.getBoolean(Keys.FULLSCREEN, false));
		if (options.getBoolean(Keys.BOTTOM_MAIN_BAR, false)) {
			layoutParams = new RelativeLayout.LayoutParams(mainBar.getLayoutParams());
			layoutParams.addRule(RelativeLayout.ABOVE, R.id.dock_bar);
			mainBar.setLayoutParams(layoutParams);
			mainLayout.addView(mainBar);
			
			if (kitkatNoImmersiveMode) {
				View fakeStatusBar = layoutInflater.inflate(R.layout.kitkat_status_bar, mainLayout, false);
				fakeStatusBar.setBackgroundColor(options.getInt(Keys.STATUS_BAR_BACKGROUND, 0x22000000));
				layoutParams = new RelativeLayout.LayoutParams(fakeStatusBar.getLayoutParams());
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				fakeStatusBar.setLayoutParams(layoutParams);
				mainLayout.addView(fakeStatusBar);
			}
			layoutParams = new RelativeLayout.LayoutParams(grid.getLayoutParams());
			if (kitkatNoImmersiveMode) {
				layoutParams.addRule(RelativeLayout.BELOW, R.id.kitkat_status_bar);
			} else {
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			}
			layoutParams.addRule(RelativeLayout.ABOVE, R.id.main_bar);
			grid.setLayoutParams(layoutParams);
			mainLayout.addView(grid);
		} else {
			if (!kitkatNoImmersiveMode) {
				layoutParams = new RelativeLayout.LayoutParams(mainBar.getLayoutParams());
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			} else {
				View fakeStatusBar = layoutInflater.inflate(R.layout.kitkat_status_bar, mainLayout, false);
				fakeStatusBar.setBackgroundColor(options.getInt(Keys.STATUS_BAR_BACKGROUND, 0x22000000));
				layoutParams = new RelativeLayout.LayoutParams(fakeStatusBar.getLayoutParams());
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				mainLayout.addView(fakeStatusBar);
				
				layoutParams = new RelativeLayout.LayoutParams(mainBar.getLayoutParams());
				layoutParams.addRule(RelativeLayout.BELOW, R.id.kitkat_status_bar);
			}
			
			mainBar.setLayoutParams(layoutParams);
			mainLayout.addView(mainBar);
			
			layoutParams = new RelativeLayout.LayoutParams(grid.getLayoutParams());
			layoutParams.addRule(RelativeLayout.ABOVE, R.id.dock_bar);
			layoutParams.addRule(RelativeLayout.BELOW, R.id.main_bar);
			grid.setLayoutParams(layoutParams);
			mainLayout.addView(grid);
		}
		if (options.getBoolean(Keys.HIDE_MAIN_BAR, false)) {
			mainBar.setVisibility(View.GONE);
		}
		mainBar.setBackgroundColor(options.getInt(Keys.BAR_BACKGROUND, 0x22000000));
		grid.setBackgroundColor(options.getInt(Keys.APPS_WINDOW_BACKGROUND, 0));
		if (options.getBoolean(Keys.STACK_FROM_BOTTOM, false)) {
			grid.setStackFromBottom(true);
		}
		if (options.getBoolean(Keys.TILE, true)) {
			grid.setNumColumns(GridView.AUTO_FIT);
		}
		adapter = new CustomAdapter(this);
		grid.setAdapter(adapter);
		
		if (options.getBoolean(Keys.SCROLLBAR, false)) {
			grid.setFastScrollEnabled(true);
			grid.setFastScrollAlwaysVisible(true);
			grid.setScrollBarStyle(AbsListView.SCROLLBARS_INSIDE_INSET);
			grid.setSmoothScrollbarEnabled(true);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log.v(APP_TAG, "onCreate");
		options = PreferenceManager.getDefaultSharedPreferences(this);
		if (options.getBoolean(Keys.FULLSCREEN, false)) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		super.onCreate(savedInstanceState);
		Themer.theme = Integer.parseInt(options.getString(Keys.THEME, getResources().getString(R.string.defaultThemeValue)));
		layoutInit();
		if (options.getBoolean(Keys.SHOW_TUTORIAL, true)) {
			startActivity(new Intent(this, TutorialActivity.class));
		}
		if (Build.VERSION.SDK_INT >= 11 && options.getBoolean(Keys.KEEP_IN_MEMORY, false)) {
			Notification noti = new Notification.Builder(this)
				.setContentTitle(getResources().getString(R.string.app_name))
				.setContentText(" ")
				.setSmallIcon(R.mipmap.icon)
			//	.setLargeIcon(new Bitmap(Bitmap.ARGB_8888))
				.build();
			NotificationManager notiManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			notiManager.notify(0, noti);
		}
		setRequestedOrientation(Integer.parseInt(options.getString(Keys.ORIENTATION, "2")));
		setContentView(mainLayout);
		options.edit().putBoolean(Keys.MESSAGE_SHOWN, false).commit();
		prefListener = new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				if (key.equals(Keys.ICON_PACK) || key.equals(Keys.TRANSFORM_DRAWABLE)) {
					MyCache.deleteIcons(Apps.this);
					LauncherApp.getInstance().getIconPackManager().setIconPack(sharedPreferences.getString(Keys.ICON_PACK, "default"));
					if (scanner != null && scanner.getStatus() == AsyncTask.Status.RUNNING)
						return;
					scanner = new GetApps(Apps.this);
					scanner.execute(true);
					loadFilteredApps();
				} else if (key.equals(Keys.DIRTY) && sharedPreferences.getBoolean(Keys.DIRTY, false)) {
					if (scanner == null || scanner.getStatus() != AsyncTask.Status.RUNNING) {
						scanner = new GetApps(Apps.this);
						scanner.execute(false);
					}
				} else if (!sharedPreferences.getBoolean(Keys.MESSAGE_SHOWN, false) && Arrays.asList(Keys.restart).contains(key)) {
					Toast.makeText(Apps.this, getResources().getString(R.string.restart_needed), Toast.LENGTH_LONG).show();
					sharedPreferences.edit().putBoolean(Keys.MESSAGE_SHOWN, true).commit();
				}
			}
		};
		options.registerOnSharedPreferenceChangeListener(prefListener);
		if (Build.VERSION.SDK_INT >= 11) {
			if (Build.VERSION.SDK_INT >= 21) {
				Themer.setWindowDecorations(this, options);
			}
			Themer.applyTheme(this, options);
		}
		dock = new Dock(this);
		changePrefsOnRotate();
		gestureDetector = new GestureDetector(this, new SwipeListener(this));
		grid.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View view, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Log.v(APP_TAG, "Menu item is selected");
		switch(item.getItemId()) {
		//    	case R.id.scan:
		//    		(new GetApps(this, false)).execute();
		//    		return true;
		case R.id.full_scan:
			if (scanner != null && scanner.getStatus() == AsyncTask.Status.RUNNING)
				return true;
			scanner = new GetApps(this);
			scanner.execute(true);
			return true;
		case R.id.change_wallpaper:
			startActivity(new Intent(Intent.ACTION_SET_WALLPAPER));
			return true;
		case R.id.options:
			startActivity(new Intent(this, Options.class));
			return true;
		case R.id.access_hidden:
			categories.setCurCategory(CategoryManager.HIDDEN);
			findViewById(R.id.searchBar).setVisibility(View.GONE);
			findViewById(R.id.tabs).setVisibility(View.GONE);
			if (options.getBoolean(Keys.HIDE_MAIN_BAR, false)) {
				findViewById(R.id.main_bar).setVisibility(View.VISIBLE);
			}
			findViewById(R.id.quit_hidden_apps).setVisibility(View.VISIBLE);
			if (searchIsOpened) {
				closeSearch();
			}
			loadFilteredApps();
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		//Log.v(APP_TAG, "onResume");
		
		int appShortcut = Integer.parseInt(options.getString(Keys.APP_SHORTCUT, "3"));
	    lock = options.getString(Keys.PASSWORD, "").length() > 0;
	    if (!homeButtonPressed) {
	    	try {
	    		loadList(false);
	    	} catch (Exception e) {
	    		Toast.makeText(Apps.this, e.toString(), Toast.LENGTH_LONG).show();
	    	}
	    } else {
	    	homeButtonPressed = false;
	    }
		boolean needReload = false;
		
		if (map.size() == 0) {
			needReload = true;
		} else {
			if ((Integer.parseInt(options.getString(Keys.PREV_APP_SHORTCUT, "3")) == CustomAdapter.TEXT) != (appShortcut == CustomAdapter.TEXT)) {
				if (appShortcut >= CustomAdapter.ICON) {
					needReload = true;
				} else {
					MyCache.deleteIcons(this);
					options.edit().putString(Keys.PREV_APP_SHORTCUT, ((Integer)appShortcut).toString()).commit();
				}
			}
		}

		if ((needReload || options.getBoolean(Keys.DIRTY, false))
			&& (scanner == null || scanner.getStatus() != Status.RUNNING)) {
			scanner = new GetApps(this);
			scanner.execute(false);
		}
		//removes the oldest result from history
		historySize = options.getInt(Keys.HISTORY_SIZE, 10);
		boolean historySizeChanged = false;
		while (categories.getCategoryData(CategoryManager.HISTORY).size() > historySize) {
			historySizeChanged = true;
			categories.removeFromCategory(CategoryManager.HISTORY, categories.getCategoryData(CategoryManager.HISTORY).size()-1);
		}
		if (historySizeChanged && categories.getCurCategory().equals(CategoryManager.HISTORY)) {
			loadFilteredApps();
		}
		dock.initApps(map);
	}
	@Override
	protected void onPostResume() {
		super.onPostResume();
		if (options.getBoolean(Keys.SHOW_KEYBOARD_ON_START, false)) {
			openSearch();
		}
	}
}
