package ru.henridellal.emerald;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//import android.annotation.SuppressLint;
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
//import android.content.pm.ActivityInfo;
//import android.content.pm.PackageManager;
import android.content.res.Configuration;
//import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
//import android.os.Process;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
//import android.util.Log;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.view.GestureDetector;
//import android.view.GestureDetector.OnGestureListener;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
//import android.view.KeyCharacterMap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
//import android.widget.RelativeLayout;
//import android.widget.SectionIndexer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Apps extends Activity //implements OnGestureListener 
{
	CategoryManager categories;
	ArrayList<AppData> curCatData;
	GridView grid;
	Dock dock;
	Map<String,AppData> map;
	public SharedPreferences options;
	final static String PREF_APPS = "apps";
	final static String APP_TAG = "Emerald";
	private Spinner spin;
	private CustomAdapter adapter = null;
	public static final int GRID = 0;
	public static final int LIST = 1;
	GetApps scanner = null;
	private OnSharedPreferenceChangeListener prefListener;
	private boolean lock, returnToHome, searchIsOpened, homeButtonPressed;
	private int historySize, appShortcut;
	
	public void loadList(boolean cleanCategory) {
		ArrayList<AppData> data = new ArrayList<AppData>(); 
		MyCache.read(this, GetApps.CACHE_NAME, data);
		loadList(data, cleanCategory);
	}
	public Dock getDock() {
		return dock;
	}
	public boolean hasApp(AppData app) {
		return (map.get(app.getComponent()) != null);
	}
	/*returns map with pairs of package names 
	and AppData related to them*/
	private Map<String, AppData> makeMap(ArrayList<AppData> data) {
		Map<String, AppData> map = new HashMap<String, AppData>();

		for (AppData a : data)
			map.put(a.getComponent(), a);
		return map;
	}

	public void loadList(ArrayList<AppData> data, boolean cleanCategory) {
		loadList(makeMap(data), cleanCategory);
	}

	public void loadList(Map<String,AppData> map, boolean cleanCategory) {
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
		setSpinner();
	}
	public void setSpinner() {
		//Log.v(APP_TAG, "Updating spinner");
		final ArrayList<String> cats = new ArrayList<String>();
		cats.addAll(categories.getCategories());
		cats.remove(CategoryManager.HIDDEN);
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_item,
				cats);
		//		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		aa.setDropDownViewResource(R.layout.spinner_item);
		spin.setAdapter(aa);
		//Set chosen item of spinner
		String cur = categories.getCurCategory();
		int pos = -1;
		for (int i = 0 ; i < cats.size(); i++ )
			if ( cats.get(i).equals(cur)) {
				pos = i;
				break;
			}

		spin.setSelection(pos);

		spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int catNum, long arg3) {
				//Log.v(APP_TAG, "selected from spinner");
				String newCat = cats.get(catNum);
				if (!newCat.equals(categories.getCurCategory())) {
					categories.setCurCategory(newCat);
					loadFilteredApps();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
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
		//Log.v(APP_TAG, "loadFilteredApps : finished");
	}
	//handles history filling
	private void addInHistory(AppData a) {
    //removes app from history if it is already in it
    // to avoid duplicating
    	//Log.v(APP_TAG, "Add app in history");
		if (categories.getCategoryData(CategoryManager.HISTORY).indexOf(a) != -1) {
			categories.removeFromCategory(CategoryManager.HISTORY, a);
		}
    
		categories.addToHistory(a);
		//categories.addToCategory(Categories.HISTORY, a);
    //removes old entries if History has maximum size
		/*if (categories.getCategoryData(Categories.HISTORY).size() == 1) {
			categories.unhideCategory(Categories.HISTORY);
		}*/
		if (categories.getCategoryData(CategoryManager.HISTORY).size() > historySize) {
			categories.removeFromCategory(CategoryManager.HISTORY, categories.getCategoryData(CategoryManager.HISTORY).size()-1);
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
		}
	}

	private void initGrid() {
		//Log.v(APP_TAG, "Make a grid");
		grid.setAdapter(null);

		// -1 is a value of AUTO_FIT constant
		if (options.getBoolean(Keys.TILE, true)) {
			grid.setNumColumns(GridView.AUTO_FIT);
		}
		adapter = new CustomAdapter(this);

		grid.setAdapter(adapter);
		if (Themer.theme == Themer.LIGHT)
			grid.setBackgroundColor(Color.WHITE);
	}
	
	//launches popup window for editing apps
	private void itemEdit(final AppData item) {
		//Log.v(APP_TAG, "Open app edit window");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(item.name);
		builder.setCancelable(true);

		ArrayList<String> editableCategories =  categories.getEditableCategories();
		final int nCategories = editableCategories.size();
		final int position = grid.getFirstVisiblePosition();

		if (nCategories > 0) {
			final String[] editableCategoryNames = new String[nCategories];
			editableCategories.toArray(editableCategoryNames);
			final boolean[] checked = new boolean[nCategories];			

			for (int i = 0; i < nCategories ; i++) {
				checked[i] = categories.in(item, editableCategoryNames[i]);
			}

			final boolean[] oldChecked = checked.clone();

			builder.setMultiChoiceItems(editableCategoryNames, checked, 
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
							categories.addToCategory(editableCategoryNames[i], item);
						else if (!checked[i] && oldChecked[i])
							categories.removeFromCategory(editableCategoryNames[i], item);
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
				getResources().getString(R.string.add_to_dock)
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
				}
			}
		});
		if (!isFinishing())
			builder.create().show();
	}
//	@Override
//	public boolean dispatchKeyEvent(KeyEvent event) {
//		Log.v(APP_TAG, "key "+event);
//		if (event.getKeyCode() == KeyEvent.KEYCODE_HOME &&
//				event.getAction() == KeyEvent.ACTION_UP &&
//				event.getDownTime() < 500) {
//			categories.setCurCategory(Categories.ALL);
//			categories.clearHistory();
//			loadFilteredApps();
//			setSpinner();
//			return true;			
//		}
//		return super.dispatchKeyEvent(event);
//	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//Log.v(APP_TAG, "Configuration changed");
		super.onConfigurationChanged(newConfig);
		changePrefsOnRotate();
		loadFilteredApps();
	}
	
	void menu() {
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
		findViewById(R.id.tabs).setVisibility(View.GONE);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(grid, InputMethodManager.SHOW_IMPLICIT);
		}
		findViewById(R.id.searchBar).setVisibility(View.VISIBLE);
		final EditText text = (EditText)findViewById(R.id.textField);
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
		findViewById(R.id.tabs).setVisibility(View.VISIBLE);
		if (!dock.isEmpty()) {
			dock.unhide();
		}
		searchIsOpened=false;
	}
	public void onMyClick(View v) {
		switch(v.getId()) {
			case R.id.searchButton:
				openSearch();
				break;
			case R.id.menuButton:
				//startActivity(new Intent(this, Options.class));
				menu();
				break;
			case R.id.quit_hidden_apps:
				categories.setCurCategory(CategoryManager.ALL);
				v.setVisibility(View.GONE);
				findViewById(R.id.tabs).setVisibility(View.VISIBLE);
				loadFilteredApps();
				setSpinner();
				break;
		}
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
			}
			if (categories.getCurCategory().equals(CategoryManager.HIDDEN)) {
				findViewById(R.id.quit_hidden_apps).setVisibility(View.GONE);
				findViewById(R.id.tabs).setVisibility(View.VISIBLE);
				categories.setCurCategory(CategoryManager.ALL);
			} else {
				categories.prevCategory();
			}
			loadFilteredApps();
			setSpinner();
			return true;
		}
		return false;
	}
	
	@Override
	public void onBackPressed() {}
	
	@Override
	protected void onStart() {
		//Log.v(APP_TAG, "onStart ");
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		returnToHome = true;
		super.onStop();
	}
	
	@Override
	protected void onPause() {
		//Log.v(APP_TAG, "onPause");
		super.onPause();
		returnToHome = false;
		if (searchIsOpened) {
			closeSearch();
		}
	}
	@Override
	protected void onDestroy() {
		//Log.v(APP_TAG, "onDestroy");
		options.unregisterOnSharedPreferenceChangeListener(prefListener);
		grid.setOnScrollListener(null);
		prefListener = null;
		super.onDestroy();
	}
	
	public void setScrollbar() {
		if (options.getBoolean("scrollbar", false)) {
			//grid.setFastScrollEnabled(true);
			//grid.setFastScrollAlwaysVisible(true);
			//grid.setScrollBarStyle(AbsListView.SCROLLBARS_OUTSIDE_INSET);
			//grid.setSmoothScrollbarEnabled(true);
			AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
				String firstChar;
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					if (adapter != null) {
						if (adapter.getCount() > 0) {
							firstChar = adapter.getAppName(firstVisibleItem);
							if (firstChar.length() > 1) {
								firstChar = firstChar.substring(0,1);
							}
						} else {
							firstChar = " ";
						}
					} else {
						firstChar = " ";
					}
					((TextView)findViewById(R.id.hintText)).setText(firstChar);
				}
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					if (scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING) {
						findViewById(R.id.hint).setVisibility(View.VISIBLE);
					} else {
						findViewById(R.id.hint).setVisibility(View.GONE);
					}
				}
			};
			grid.setOnScrollListener(onScrollListener);
		} else {
			grid.setOnScrollListener(null);
			findViewById(R.id.hint).setVisibility(View.GONE);
		}
	}
	@Override
	protected void onNewIntent(Intent i) {
		//Log.v(APP_TAG, "onNewIntent");
		homeButtonPressed = true;
		if (categories == null) {
			loadList(false);
		}
		if (returnToHome) {
			categories.setCurCategory(categories.getHome());
		} else if (categories.getCurCategory().equals(CategoryManager.HIDDEN)) {
			findViewById(R.id.quit_hidden_apps).setVisibility(View.GONE);
			findViewById(R.id.tabs).setVisibility(View.VISIBLE);
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
		setSpinner();
		super.onNewIntent(i);
	}
	/*public void layoutInit() {
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.appsWindow);
		RelativeLayout.LayoutParams params = 
	}*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log.v(APP_TAG, "onCreate");
		super.onCreate(savedInstanceState);
		//layoutInit();
		options = PreferenceManager.getDefaultSharedPreferences(this);
		Themer.theme = Integer.parseInt(options.getString(Keys.THEME, getResources().getString(R.string.defaultThemeValue)));
		if (options.getBoolean(Keys.SHOW_TUTORIAL, true)) {
			startActivity(new Intent(this, TutorialActivity.class));
		}
		if (Build.VERSION.SDK_INT >= 11 && options.getBoolean(Keys.KEEP_IN_MEMORY, false)) {
			Notification noti = new Notification.Builder(this)
				.setContentTitle("Emerald")
				.setContentText(" ")
				.setSmallIcon(R.drawable.icon)
			//	.setLargeIcon(new Bitmap(Bitmap.ARGB_8888))
				.build();
			NotificationManager notiManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			notiManager.notify(0, noti);
		}
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setRequestedOrientation(Integer.parseInt(options.getString(Keys.ORIENTATION, "1")));
		setContentView(R.layout.apps);
		findViewById(R.id.appsWindow).setBackgroundColor(options.getInt(Keys.APPS_WINDOW_BACKGROUND, 0));
		findViewById(R.id.topbar).setBackgroundColor(options.getInt(Keys.BAR_BACKGROUND, 0x22000000));
		findViewById(R.id.dock_bar).setBackgroundColor(options.getInt(Keys.DOCK_BACKGROUND, 0x22000000));
		grid = (GridView)findViewById(R.id.appsGrid);
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
					setSpinner();
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
		initGrid();
		setScrollbar();
		if (Build.VERSION.SDK_INT >= 19) {
			Themer.setWindowDecorations(this, options);
		}
		Themer.applyTheme(this, options);
		spin = (Spinner)findViewById(R.id.category);
		spin.setOnTouchListener(new SwipeListener(this));
		dock = new Dock(this);
		changePrefsOnRotate();
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
		appShortcut = Integer.parseInt(options.getString(Keys.APP_SHORTCUT, "3"));
	    lock = options.getString(Keys.PASSWORD, "").length() > 0;
	    if (!homeButtonPressed) {
	    	loadList(false);
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

		if (needReload || options.getBoolean(Keys.DIRTY, false)) {
			//			Log.v(APP_TAG, "scan");
			if (scanner == null || scanner.getStatus() != Status.RUNNING) {
				scanner = new GetApps(this);
				scanner.execute(false);
			}
		}
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
}
