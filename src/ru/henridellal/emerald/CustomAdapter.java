package ru.henridellal.emerald;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

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
	private ArrayList<AppData> categoryData, toDisplay;
	private ArrayList<String> sectionsList;
	private HashMap<String, Integer> indexData;
	private String[] sections;
	private int curMode, iconSize, textSize, textColor, appShortcut;
	boolean lock, fastScrollEnabled;
	private ImageView img;
	private TextView tv;
	private String searchInput;
	private Set<String> sectionsSet;
	private Comparator<AppData> comparator;
	
	public void setIconSize(int size) {iconSize = size;}
	public void setTextSize(int size) {textSize = size;}
	
	public void filter(CharSequence searchInput) {
		indexData.clear();
		this.searchInput = searchInput.toString();
		toDisplay = new ArrayList<AppData>();
		for (AppData a: categoryData) {
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
				for (AppData a: categoryData) {
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
		AppData a;

		a = toDisplay.get(position);
		boolean isEmptyView = (convertView == null);
		if (isEmptyView) {
			LayoutInflater inflater = (LayoutInflater)contextRef.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (options.getBoolean(Keys.TILE, true))
				view = inflater.inflate(R.layout.iconbutton, parent, false);
			else
				view = inflater.inflate(R.layout.oneline, parent, false);
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
				tv.setTypeface(Typeface.DEFAULT,
					Integer.parseInt(options.getString(Keys.FONT_STYLE, "0")));
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
	public void update(ArrayList<AppData> curCatData) {
		categoryData = curCatData;
		toDisplay = new ArrayList<AppData>(categoryData);
		setSections();
		notifyDataSetChanged();
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
		int theme = Themer.theme;
		textColor = (theme == Themer.LIGHT || theme == Themer.WALLPAPER_DARK || theme == Themer.DEFAULT_THEME) ? Color.BLACK : Color.WHITE;
		categoryData = new ArrayList<AppData>();
		toDisplay = new ArrayList<AppData>();
		onClickListener = new OnAppClickListener((Apps)context);
		if (lock) {
			onLongClickListener = new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View arg0) {
					final View v = arg0;
					AlertDialog.Builder builder = new AlertDialog.Builder(contextRef.get());
					//builder.setTitle("");
					builder.setMessage(contextRef.get().getResources().getString(R.string.type_password));
					final EditText inputBox = new EditText(contextRef.get());
					inputBox.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
					builder.setView(inputBox);
					builder.setPositiveButton(android.R.string.yes, 
						new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (inputBox.getText().toString().equals(options.getString(Keys.PASSWORD, ""))) {
								((Apps)contextRef.get()).itemContextMenu((AppData)v.getTag());
							} else {
								Toast.makeText(contextRef.get(), contextRef.get().getResources().getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
							}
						}
					});
					builder.setCancelable(true);
					builder.show();
					return false;
				}
			};
		} else {
			onLongClickListener = new OnAppLongClickListener((Apps)context);
		}
		comparator = new Comparator<AppData>() {
			@Override
			public int compare(AppData first, AppData second) {
				boolean firstStarts = first.name.toLowerCase().startsWith(searchInput);
				boolean secondStarts = second.name.toLowerCase().startsWith(searchInput);
				if (firstStarts && !secondStarts) {
					return -1;
				} else if (!firstStarts && secondStarts) {
					return 1;
				} else {
					return AppData.NameComparator.compare(first, second);
				}
			}
		};
	}
}
