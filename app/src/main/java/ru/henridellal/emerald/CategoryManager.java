package ru.henridellal.emerald;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
//import android.util.Log;

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
			boolean finished = false;
			while (!finished) {
				if (direction == PREVIOUS) {
					result--;
					result = (result > -1) ? result : names.size()-2;
				} else {
					result++;
					result = (result < names.size()-1) ? result : 0;
				}
				if (!isHidden(names.get(result))) {
					finished = true;
				}
			}
			return names.get(result);
		} else {
			return null;
		}
	}
	public Category getCategory(String categoryName) {
		return categories.get(categoryName);
	}
	public boolean isHidden(String categoryName) {
		if (HISTORY.equals(categoryName)) {
			return options.getBoolean(Keys.HIDE_HISTORY, false);
		} else if (UNCLASSIFIED.equals(categoryName)) {
			return options.getBoolean(Keys.HIDE_UNCLASSIFIED, false);
		} else {
			return false;
		}
	}
	public ArrayList<BaseData> getCategoryData(String category) {
		return DatabaseHelper.getEntries(contextRef.get(), category);
	}
	public void convert() {
		BufferedReader reader = null;
		File file = new File(contextRef.get().getFilesDir() + "/categories.props");
		ArrayList<String> hiddenCategories = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(file));
			
			String d;
			String key = null;
			String value = null;
			int index = -1;
			while (null != (d = reader.readLine())) {
				d = d.trim();
				if (names.contains(d)) {
					String category = d;
					d = reader.readLine();
					index = d.indexOf('=');
					key = d.substring(0, index).trim();
					value = d.substring(index+1, d.length()).trim();
					if (key.equals("hidden")) {
						if ("true".equals(value)) {
							hiddenCategories.add(category);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		
		if (reader != null)
			try {
				reader.close();
				file.delete();
			} catch (IOException e) {
		}
		for (File f : contextRef.get().getFilesDir().listFiles()) {
			//get files names and look for .cat ones
			String n = f.getName();
			if (n.endsWith(".cat")) {
				String name = n.substring(0, n.length()-4);
				name = URLDecoder.decode(name);
				DatabaseHelper.addCategory(contextRef.get(), name);
			}
		}
		if (hiddenCategories.contains("History")) {
			options.edit().putBoolean(Keys.HIDE_HISTORY, true);
		} else if (hiddenCategories.contains("Unclassified")) {
			options.edit().putBoolean(Keys.HIDE_UNCLASSIFIED, true);
		}
	}
	
	/*Sets current category and saves its name in preferences*/
	public void setCurCategory(String category) {
		setCurCategory(category, true);
	}
	
	public void setCurCategory(String category, boolean push) {
		//Log.v("TinyLaunch", "setCur "+category+" "+push);
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
		} else {
			setCurCategory(home);
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
				//Log.v("TinyLaunch", "pop "+history.size()+" "+c);
				return c;
			}
		}
		return home;
//		return null;
	}
	
	//returns category file
	@SuppressWarnings("deprecation")
	public File catPath(String category) {
		return new File(contextRef.get().getFilesDir()+"/"+URLEncoder.encode(category)+".cat");
	}
	//return category names
	public ArrayList<String> getCategories() {
		return names;
	}
	//removes an app reference from category
	public void removeFromCategory(String cat, int i) {
		if (!isEditable(cat))
			return;
		ArrayList<? extends BaseData> data = categories.get(cat).getData();
		//update category file
		if (data != null) {
			data.remove(i);				
		}
	}
	public void removeFromCategory(String cat, BaseData a) {
		if (!isEditable(cat))
			return;
		ArrayList<BaseData> data = categories.get(cat).getData();
		if (data != null) {
			data.remove(a);				
		}
	}
	//adds app to category
	public void addToCategory(String cat, BaseData a) {
		if (!isEditable(cat))
			return;
		
		ArrayList<BaseData> data = categories.get(cat).getData();
		if (data != null) {
//			Log.d("TinyLaunch", "adding "+a.name);
			data.add(a);	
		}
	}
	
	public void addToHistory(BaseData a) {
		ArrayList<BaseData> data = categories.get(HISTORY).getData();
		if (data != null) {
//			Log.d("TinyLaunch", "adding "+a.name);
			data.add(0, a);	
		}
	}
	
	public void removeCategory(String catName) {
		if (!isCustom(catName))
			return;
		catPath(catName).delete();
		categories.remove(catName);
		names.remove(catName);
		if (home.equals(catName)) {
			setHome(ALL);
		}
		setCurCategory(home);
	}
	//makes the current category empty
	public void clearCategory(String cat) {
    	if (!isEditable(cat))
			return;
		int s = categories.get(cat).getData().size();
		for (int i = 0; i < s; i++) {
			removeFromCategory(cat, 0);
		}
	}
	/*public void cleanCategory(String category) {
		if (!isEditable(category))
			return;
		
		ArrayList<? extends BaseData> data = categories.get(category).getData();
			
		if (data == null) 
			return; // should not happen
			
		boolean dirty = false;
		
		for (int i = data.size() - 1 ; i >= 0 ; i--) {
			BaseData a = data.get(i);
			if (null == map.get(a.getId())) {
				data.remove(i);
				dirty = true;
			}
		}
		
		if (dirty) 
			putEntries(catPath(category), data);
	}*/
	//cleans categories files from deleted apps
	//updates category files
	/*public void cleanCategories() {
		for (String c: names) {
			cleanCategory(c);
		}
	}*/
	//adds new category
	/*public boolean addCategory(String c) {
//		Log.v("TinyLaunch", "adding "+c);
		if (names.contains(c)) {
//			Log.v("TinyLaunch", "already used "+c);
			return false;
		}
		try {
			catPath(c).createNewFile();
		} catch (IOException e) {
			return false;
		}
		categories.put(c, new Category(c, new ArrayList<BaseData>()));
		names.add(c);
		sortNames();
		return true;
	}*/
	//get entries of category from category file
	public ArrayList<String> getEntriesComponents(File f) {
		ArrayList<String> data = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			
			String d;
			
			while (null != (d = reader.readLine())) {
				d = d.trim();
				if (d.length()>0) {
					data.add(d);
				}
			}			
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		
		if (reader != null)
			try {
				reader.close();
			} catch (IOException e) {
			}
		
		return data;
	}
	
	//return list of apps for default categories (All, Unclassified, Hidden)
	public ArrayList<BaseData> filterApps(Map<String,? extends BaseData> map) {
		ArrayList<BaseData> data = new ArrayList<BaseData>();
		
		if (!isEditable(curCategory)) {
			data.addAll(map.values());
			if (UNCLASSIFIED.equals(curCategory)) {
				for (Category c : categories.values()) {
					if (!c.getName().equals(HISTORY)) {
						data.removeAll(c.getData());
					}
				}
			} else {
				ArrayList<BaseData> c = categories.get(HIDDEN).getData();
				if (c != null)
					data.removeAll(c);
			}
		}
		else {
			ArrayList<BaseData> c = categories.get(curCategory).getData();
			//Log.v("TinyLaunch", "filtering via "+curCategory+" "+c.size());
			if (c != null) {
				data.addAll(c);
			}
		}
		//if (customSorting) {}
		if (!curCategory.equals(HISTORY))
			Collections.sort(data, BaseData.NameComparator);
		
		return data;		
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
	//returns true if rename was successful
	
	public boolean renameCategory(String newName, String cat) {
		if (newName.equals(cat))
			return true;
		
		if (names.contains(newName)) {
//			Log.v("TinyLaunch", "already used "+c);
			return false;
		}
		
		if (!catPath(cat).renameTo(catPath(newName)))
			return false;

		Category c = categories.get(cat);
		categories.remove(cat);
		categories.put(newName, c);
		names.remove(cat);
		names.add(newName);
		c.setName(newName);
		sortNames();
		setCurCategory(ALL);
		if (cat.equals(home)) {
			setHome(newName);
		}
		return true;
	}

	public void clearHistory() {
		history = new ArrayList<String>();
	}
}
