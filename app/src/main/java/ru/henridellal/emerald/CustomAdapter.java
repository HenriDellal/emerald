package ru.henridellal.emerald;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

public class CustomAdapter extends BaseAdapter implements SectionIndexer
{
	public static final int TEXT = 1;
	public static final int ICON = 2;
	
	private View.OnClickListener onClickListener;
	private View.OnLongClickListener onLongClickListener;
	private SoftReference<Context> contextRef;
	private SharedPreferences options;
	private ArrayList<BaseData> categoryData, toDisplay;
	private ArrayList<String> sectionsList;
	private HashMap<String, Integer> indexData;
	private String[] sections;
	private int curMode, iconSize, textSize, textColor, appShortcut, inflatedLayoutId, fontStyle;
	boolean lock, fastScrollEnabled;
	private ImageView img;
	private TextView tv;
	private String searchInput;
	private Set<String> sectionsSet;
	private Comparator<BaseData> comparator;
	
	public void setIconSize(int size) {iconSize = size;}
	public void setTextSize(int size) {textSize = size;}
	
	public void filter(CharSequence searchInput) {
		indexData.clear();
		this.searchInput = searchInput.toString();
		toDisplay = new ArrayList<BaseData>();
		for (BaseData a: categoryData) {
			if (a.name.toLowerCase().contains(this.searchInput.toLowerCase())) {
				toDisplay.add(a);
			}
		}
		setSections();
		Collections.sort(toDisplay, comparator);
		notifyDataSetChanged();
	}
	public void setSections() {
		indexData.clear();
		if (fastScrollEnabled) {
			if (this.searchInput.equals("")) {
				String ch;
				int appIndex = 0;
				for (BaseData a: categoryData) {
					ch = (a.name.length() > 1) ? a.name.substring(0,1).toUpperCase() : a.name;
					indexData.put(ch, appIndex);
					appIndex++;
				}
			}
			
			sectionsSet = indexData.keySet();
			sectionsList = new ArrayList<String>(sectionsSet);
			Collections.sort(sectionsList);
			sections = new String[sectionsList.size()];
			sectionsList.toArray(sections);
		}
	}
	
	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		BaseData a;

		a = toDisplay.get(position);
		boolean isEmptyView = (convertView == null);
		if (isEmptyView) {
			LayoutInflater inflater = (LayoutInflater)contextRef.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(inflatedLayoutId, parent, false);
		} else {
			view = convertView;
		}
		// app shortcut
		view.setTag(a);
		img = (ImageView) view.findViewById(R.id.icon);
		tv = (TextView) view.findViewById(R.id.text);
		
		if (appShortcut != ICON) {
			tv.setText(a.name);
			if (isEmptyView) {
				if (appShortcut == TEXT) {
					img.setVisibility(View.GONE);
				}
				tv.setTextSize(textSize);
				tv.setTextColor(textColor);
				tv.setTypeface(Typeface.DEFAULT, fontStyle);
			}
		} else {
			tv.setVisibility(View.GONE);
		}
		if (appShortcut >= ICON) {
			IconPackManager.setIcon(contextRef.get(), img, a);
			if (isEmptyView) {
				img.setVisibility(View.VISIBLE);
				ViewGroup.LayoutParams p = img.getLayoutParams();
				p.width = iconSize;
				p.height = iconSize;
				img.setLayoutParams(p);
			}
		}
		view.setOnClickListener(onClickListener);
		view.setOnLongClickListener(onLongClickListener);
		return view;
	}
	public String getAppName(int position) {
		return toDisplay.get(position).name;
	}
		
	public int getPositionForSection(int sectionIndex) {
		return indexData.get(sections[sectionIndex]);
	}
	public int getSectionForPosition(int position) {
		return 0;
	}
	public Object[] getSections() {
		return sections;
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
	public void update(ArrayList<BaseData> curCatData) {
		categoryData = curCatData;
		toDisplay = new ArrayList<BaseData>(categoryData);
		if (!CategoryManager.HISTORY.equals(LauncherApp.getCategoryManager().getCurCategory()))
			Collections.sort(toDisplay, comparator);
		setSections();
		notifyDataSetChanged();
	}
	
	private void setTextColor() {
		int theme = Themer.theme;
		boolean invertFontColor = options.getBoolean(Keys.INVERT_FONT_COLOR, false);
		textColor = (theme == Themer.LIGHT || theme == Themer.WALLPAPER_DARK || theme == Themer.DEFAULT_THEME) ?
			(invertFontColor ? Color.WHITE : Color.BLACK) :
			(invertFontColor ? Color.BLACK : Color.WHITE);
	}
	
	public CustomAdapter(Context context) {
		super();
		indexData = new HashMap<String, Integer>();
		//Log.v(APP_TAG, "custom adapter created");
		contextRef = new SoftReference<Context>(context);
		options = PreferenceManager.getDefaultSharedPreferences(contextRef.get());
		fastScrollEnabled = options.getBoolean(Keys.SCROLLBAR, false);
		searchInput = "";
		appShortcut = Integer.parseInt(options.getString(Keys.APP_SHORTCUT, "3"));
		lock = options.getString(Keys.PASSWORD, "").length() > 0;
		setTextColor();
		categoryData = new ArrayList<BaseData>();
		toDisplay = new ArrayList<BaseData>();
		onClickListener = new OnAppClickListener((Apps)context);
		if (options.getBoolean(Keys.TILE, true)) {
			inflatedLayoutId = R.layout.iconbutton;
		} else {
			inflatedLayoutId = R.layout.oneline;
		}
		fontStyle = Integer.parseInt(options.getString(Keys.FONT_STYLE, "0"));
		if (lock) {
			onLongClickListener = new OnAppUnlockLongClickListener(context);
		} else {
			onLongClickListener = new OnAppLongClickListener((Apps)context);
		}
		comparator = new Comparator<BaseData>() {
			@Override
			public int compare(BaseData first, BaseData second) {
				boolean firstStarts = first.name.toLowerCase().startsWith(searchInput);
				boolean secondStarts = second.name.toLowerCase().startsWith(searchInput);
				if (firstStarts && !secondStarts) {
					return -1;
				} else if (!firstStarts && secondStarts) {
					return 1;
				} else {
					return BaseData.NameComparator.compare(first, second);
				}
			}
		};
	}
}
