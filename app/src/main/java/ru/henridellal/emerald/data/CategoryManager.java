package ru.henridellal.emerald.data;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ru.henridellal.emerald.preference.Keys;

public class CategoryManager {
	public static final String ALL = "All";
	public static final String UNCLASSIFIED = "Unclassified";
	public static final String HIDDEN = "Hidden";
	public static final String HISTORY = "History";
	
	public static final byte PREVIOUS = 0;
	public static final byte NEXT = 1;
	
	private SoftReference<Context> contextRef;
	private String home;
	private String curCategory;
	private ArrayList<String> names;
	private Map<String,Category> categories;
	private SharedPreferences options;
	private ArrayList<String> history;
	private static final int HISTORY_MAX = 10;
	
	public CategoryManager(Context context) {
		contextRef = new SoftReference<Context>(context);
		this.options = PreferenceManager.getDefaultSharedPreferences(context);
		history = new ArrayList<String>();
		loadCategoriesList();
		curCategory = options.getString(Keys.CATEGORY, ALL);
		home = options.getString(Keys.HOME, ALL);
	}

	public void loadCategoriesList() {
		categories = DatabaseHelper.getCategories(contextRef.get());
		names = new ArrayList<String>(categories.keySet());
		sortNames();
	}

	public String getHome() {
		return home;
	}

	public void setHome(String catName) {
		if (haveCategory(catName)) {
			home = catName;
			options.edit().putString(Keys.HOME, home).commit();
		}
	}
	/*Checks if Categories instance has a category named s*/
	public boolean haveCategory(String s) {
		return names.contains(s);
	}
	/*Functions for spinner. Return names of categories
	which are neighbouring to the current*/
	public String getCategory(byte direction) {
		if (!curCategory.equals(HIDDEN)) {
			int result = names.indexOf(curCategory);
			int startPosition = result;
			for (int i = 0; i < names.size(); i++) {
				if (direction == PREVIOUS) {
					result--;
					result = (result > -1) ? result : names.size()-2;
				} else {
					result++;
					result = (result < names.size()-1) ? result : 0;
				}
				if (startPosition == result) {
					return null;
				}
				if (!isHidden(names.get(result))) {
					break;
				}
			}
			return names.get(result);
		} else {
			return null;
		}
	}

	public Category getCategory(String categoryName) {
		if (categories.containsKey(categoryName)) {
			return categories.get(categoryName);
		} else {
			return categories.get(ALL);
		}
	}

	public boolean isHidden(String categoryName) {
		if (HISTORY.equals(categoryName)) {
			return options.getBoolean(Keys.HIDE_HISTORY, false);
		} else if (UNCLASSIFIED.equals(categoryName)) {
			return options.getBoolean(Keys.HIDE_UNCLASSIFIED, false);
		} else if (ALL.equals(categoryName)) {
			return options.getBoolean(Keys.HIDE_ALL, false);
		} else {
			return false;
		}
	}
	
	/*Sets current category and saves its name in preferences*/
	public void setCurCategory(String category) {
		setCurCategory(category, true);
	}
	
	public void setCurCategory(String category, boolean push) {
		if (push)
			pushCategory(curCategory);
		curCategory = category;
		options.edit().putString(Keys.CATEGORY, category).commit();
	}

	//sets previously chosen category as current
	public void prevCategory() {
		String c = popCategory();
		if (c != null) {
			setCurCategory(c, false);
		}
	}
	
	private void pushCategory(String c) {
		if (history.size() > 0 && history.get(history.size()-1).equals(c))
			return;
		if (history.size() >= HISTORY_MAX)
			history.remove(0);
		//Log.v("TinyLaunch", "push "+c);
		history.add(c);
	}

	//returns (pops) previously chosen category from history
	private String popCategory() {
		while (history.size() > 0) {
			String c = history.get(history.size()-1);
			history.remove(history.size()-1);
			if (names.contains(c)) {
				return c;
			}
		}
		return home;
	}

	//return category names
	public ArrayList<String> getCategories() {
		return names;
	}

	//sorts categories in specific order
	//All > custom categories > Unclassified > History > Hidden
	public void sortNames() {
		Collections.sort(names, new Comparator<String>(){
			//private Map<String, Integer> defaultRanks = new Map<String, Integer>();
			@Override
			public int compare(String lhs, String rhs) {
				//int lhsNumber, rhsNumber;
				if (lhs.equals(ALL)) {
					if (rhs.equals(ALL))
						return 0;
					else
						return -1;
				} else if (lhs.equals(HIDDEN)) {
					if (rhs.equals(HIDDEN)) 
						return 0;
					else
						return 1;
				} else if (lhs.equals(UNCLASSIFIED)) {
					if (rhs.equals(UNCLASSIFIED)) 
						return 0;
					else if (rhs.equals(HIDDEN) || rhs.equals(HISTORY))
						return -1;
					else
						return 1;
				} else if (lhs.equals(HISTORY)) {
					if (rhs.equals(HISTORY))
						return 0;
					else if (rhs.equals(HIDDEN))
						return -1;
					else
						return 1;
				} else if (rhs.equals(ALL)) {
					return 1;
				} else if (rhs.equals(HIDDEN) || rhs.equals(UNCLASSIFIED) || rhs.equals(HISTORY)) {
					return -1;
				}
				return lhs.compareToIgnoreCase(rhs);
			}});
	}
	
	public ArrayList<String> getEditableCategories() {
		ArrayList<String> customNames = new ArrayList<String>();
		for (String s : names) {
			if (isEditable(s)) {
				customNames.add(s);
			}
		}
		return customNames;
	}
	//can be renamed and deleted or not
	public static boolean isCustom(String c) {
		return ! c.equals(ALL) && ! c.equals(UNCLASSIFIED) && ! c.equals(HIDDEN) && ! c.equals(HISTORY);
	}
	//can be filled with apps by the user
	public static boolean isEditable(String c) {
		return ! c.equals(ALL) && ! c.equals(UNCLASSIFIED);
	}
	//checks if category has an app
	public boolean in(BaseData item, String cat) {
		return categories.get(cat).getData().contains(item);
	}

	public String getCurCategory() {
		return curCategory;
	}
}
