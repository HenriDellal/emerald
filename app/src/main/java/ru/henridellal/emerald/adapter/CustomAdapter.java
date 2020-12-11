package ru.henridellal.emerald.adapter;

import android.content.Context;
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

import ru.henridellal.emerald.activity.Apps;
import ru.henridellal.emerald.data.BaseData;
import ru.henridellal.emerald.data.CategoryManager;
import ru.henridellal.emerald.util.IconPackManager;
import ru.henridellal.emerald.preference.Keys;
import ru.henridellal.emerald.LauncherApp;
import ru.henridellal.emerald.listener.OnAppClickListener;
import ru.henridellal.emerald.listener.OnAppLongClickListener;
import ru.henridellal.emerald.listener.OnAppUnlockLongClickListener;
import ru.henridellal.emerald.R;
import ru.henridellal.emerald.ui.Themer;

public class CustomAdapter extends BaseAdapter implements SectionIndexer
{
	public static final int TEXT = 1;
	public static final int ICON = 2;
	
	private View.OnClickListener onClickListener;
	private View.OnLongClickListener onLongClickListener;
	private ViewGroup.LayoutParams imageViewLayoutParams;
	private SoftReference<Context> contextRef;
	private SharedPreferences options;
	private ArrayList<BaseData> categoryData, toDisplay;
	private HashMap<String, Integer> indexData;
	private String[] sections;
	private int iconSize, textSize, textColor, appShortcut, inflatedLayoutId, fontStyle;
	boolean lock, fastScrollEnabled;
	private String searchInput;
	private Comparator<BaseData> comparator;
	
	public void setIconSize(int size) {
		iconSize = size;
		imageViewLayoutParams = null;
	}

	public void setTextSize(int size) {textSize = size;}
	
	public void filter(CharSequence searchInput) {
		this.searchInput = searchInput.toString();
		toDisplay = new ArrayList<BaseData>();
		for (BaseData a: categoryData) {
			if (a.getName().toLowerCase().contains(this.searchInput.toLowerCase())) {
				toDisplay.add(a);
			}
		}
		Collections.sort(toDisplay, comparator);
		setSections();
		notifyDataSetChanged();
	}

	public void setSections() {
		indexData.clear();
		if (fastScrollEnabled) {
			if (!searchInput.equals("") || CategoryManager.HISTORY.equals(LauncherApp.getCategoryManager().getCurCategory())) {
				sections = new String[0];
				return;
			} else {
				String ch;
				int sectionIndex = 0;
				for (int i = 0; i < toDisplay.size(); i++) {
					BaseData a = toDisplay.get(i);
					ch = (a.getName().length() > 1) ? a.getName().substring(0,1).toUpperCase() : a.getName();
					if (!indexData.containsKey(ch)) {
						indexData.put(ch, sectionIndex);
						sectionIndex++;
					}
				}
				Set<String> sectionsSet = indexData.keySet();
				ArrayList<String> sectionsList = new ArrayList<String>(sectionsSet);
				Collections.sort(sectionsList);
				sections = new String[sectionsList.size()];
				sectionsList.toArray(sections);
			}
		}
	}
	
	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		ImageView img;
		TextView tv;
		BaseData a = toDisplay.get(position);
		boolean isEmptyView = (convertView == null);
		if (isEmptyView) {
			LayoutInflater inflater = (LayoutInflater)contextRef.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(inflatedLayoutId, parent, false);
			img = (ImageView) view.findViewById(R.id.icon);
			tv = (TextView) view.findViewById(R.id.text);
			switch (appShortcut) {
				case TEXT:
					img.setVisibility(View.GONE);
					setTextViewParams(tv);
					break;
				case ICON:
					setImageViewLayoutParams(img);
					tv.setVisibility(View.GONE);
					break;
				default:
					setImageViewLayoutParams(img);
					setTextViewParams(tv);
			}
			view.setOnClickListener(onClickListener);
			view.setOnLongClickListener(onLongClickListener);
		} else {
			view = convertView;
			img = (ImageView) view.findViewById(R.id.icon);
			tv = (TextView) view.findViewById(R.id.text);	
		}
		// app shortcut
		view.setTag(a);
		if (appShortcut != ICON) {
			tv.setText(a.getName());
			tv.setTextSize(textSize);
		}
		if (appShortcut >= ICON) {
			IconPackManager.setIcon(contextRef.get(), img, a);
		}
		return view;
	}

	public void setTextViewParams(TextView tv) {
		tv.setTextColor(textColor);
		tv.setTypeface(Typeface.DEFAULT, fontStyle);
	}

	public void setImageViewLayoutParams(ImageView img) {
		if (null == imageViewLayoutParams) {
			imageViewLayoutParams = img.getLayoutParams();
			imageViewLayoutParams.width = iconSize;
			imageViewLayoutParams.height = iconSize;
		}
		img.setLayoutParams(imageViewLayoutParams);
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
				boolean firstStarts = first.getName().toLowerCase().startsWith(searchInput);
				boolean secondStarts = second.getName().toLowerCase().startsWith(searchInput);
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
