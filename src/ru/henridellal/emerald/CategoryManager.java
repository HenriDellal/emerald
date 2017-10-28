package ru.henridellal.emerald;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class CategoryManager {
	Context context;
	public static final String ALL = "All";
	public static final String UNCLASSIFIED = "Unclassified";
	public static final String HIDDEN = "Hidden";
	public static final String HISTORY = "History";
	private String home;
	private String curCategory;
	private ArrayList<String> names;
	private Map<String,Category> categories;
	private Map<String,AppData> map;
	private SharedPreferences options;
	private ArrayList<String> history;
	private static final int HISTORY_MAX = 10;
	
	public CategoryManager(Context context, Map<String,AppData> map) {
		this.context = context;
		this.map = map;
		this.options = PreferenceManager.getDefaultSharedPreferences(context);
		history = new ArrayList<String>();
		names = new ArrayList<String>();
		names.add(ALL);
		names.add(UNCLASSIFIED);
		names.add(HISTORY);
		names.add(HIDDEN);

		categories = new HashMap<String,Category>();
		loadCategories();		
		sortNames();
		setHome(options.getString(Options.PREF_HOME, ALL));
		curCategory = options.getString(Options.PREF_CATEGORY, ALL);
	}
	public String getHome() {
		return home;
	}
	public void setHome(String catName) {
		if (haveCategory(catName)) {
			home = catName;
			options.edit().putString(Options.PREF_HOME, home).commit();
		}
	}
	/*Checks if Categories instance has a category named s*/
	public boolean haveCategory(String s) {
		return names.contains(s);
	}
	/*Functions for spinner. Return names of categories
	which are neighbouring to the current*/
	public String getPrevCategory() {
		int result;
		boolean finished = false;
		if (!curCategory.equals(HIDDEN)) {
			result = names.indexOf(curCategory);
			while (!finished) {
				result--;
				result = (result > -1) ? result : names.size()-2;
				if (!categories.get(names.get(result)).isHidden()) {
					finished = true;
				}
			}
		}
		else {
			result = 0;
		}
		return names.get(result);
	}
	public String getNextCategory() {
		int result;
		boolean finished = false;
		if (!curCategory.equals(HIDDEN)) {
			result = names.indexOf(curCategory);
			while (!finished) {
				result++;
				result = (result < names.size()-1) ? result : 0;
				if (!categories.get(names.get(result)).isHidden()) {
					finished = true;
				}
			}
		}
		else {
			result = 0;
		}
		return names.get(result);
	}
	public Category getCategory(String categoryName) {
		return categories.get(categoryName);
	}
	
	public ArrayList<AppData> getCategoryData(String category) {
		return categories.get(category).getData();
	}
	/*looks for files which represent categories
	and load them*/
	@SuppressWarnings("deprecation")
	public void loadCategories() {
		for (File f : context.getFilesDir().listFiles()) {
			//get files names and look for .cat ones
			String n = f.getName();
			if (n.endsWith(".cat")) {
				String name = n.substring(0, n.length()-4);
				name = URLDecoder.decode(name);
				//puts entries from cache to categories
				if (isEditable(name)) {
					if (isCustom(name)) {
						names.add(name);
						categories.put(name, new Category(name, getEntries(f)));
					} else {
						categories.put(name, new Category(name, getEntries(f), false));
					}
				}
				//sets category names
			}
		}
		//if category has just created, it adds an empty category
		for (String n : names) {
			if (null == categories.get(n))
				categories.put(n, new Category(n, new ArrayList<AppData>()));
		}
		readCategoriesProps();
	}
	
	public void setMap(Map<String,AppData> map) {
		this.map = map;
	}
	
	/*public static void setHome(String cat) {
		HOME = cat;
	}*/
	
	/*Sets current category and saves its name in preferences*/
	public void setCurCategory(String category) {
		setCurCategory(category, true);
	}
	
	public void setCurCategory(String category, boolean push) {
		//Log.v("TinyLaunch", "setCur "+category+" "+push);
		if (push)
			pushCategory(curCategory);
		curCategory = category;
		options.edit().putString(Options.PREF_CATEGORY, category).commit();
	}
	//sets previously chosen category as current
	public void prevCategory() {
		String c = popCategory();
		if (c != null)
			setCurCategory(c, false);
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
		return ALL;
//		return null;
	}
	
	//returns category file
	@SuppressWarnings("deprecation")
	private File catPath(String category) {
		return new File(context.getFilesDir()+"/"+URLEncoder.encode(category)+".cat");
	}
	//return category names
	public ArrayList<String> getCategories() {
		return names;
	}
	//removes an app reference from category
	public void removeFromCategory(String cat, int i) {
		if (!isEditable(cat))
			return;
		ArrayList<AppData> data = categories.get(cat).getData();
		//update category file
		if (data != null) {
			data.remove(i);				
			putEntries(catPath(cat), data);
		}
	}
	public void removeFromCategory(String cat, AppData a) {
		if (!isEditable(cat))
			return;
		ArrayList<AppData> data = categories.get(cat).getData();
		if (data != null) {
			data.remove(a);				
			putEntries(catPath(cat), data);
		}
	}
	//adds app to category
	public void addToCategory(String cat, AppData a) {
		if (!isEditable(cat))
			return;
		
		ArrayList<AppData> data = categories.get(cat).getData();
		if (data != null) {
//			Log.d("TinyLaunch", "adding "+a.name);
			data.add(a);	
			putEntries(catPath(cat), data);
		}
	}
	
	public void addToHistory(AppData a) {
		ArrayList<AppData> data = categories.get(HISTORY).getData();
		if (data != null) {
//			Log.d("TinyLaunch", "adding "+a.name);
			data.add(0, a);	
			putEntries(catPath(HISTORY), data);
		}
	}
	
	public void removeCategory(String catName) {
		if (!isCustom(catName))
			return;
		catPath(catName).delete();
		categories.remove(catName);
		names.remove(catName);
		writeCategoriesProps();
		if (home.equals(catName)) {
			setHome(ALL);
		}
		setCurCategory(ALL);
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
	public void cleanCategory(String category) {
		if (!isEditable(category))
			return;
		
		ArrayList<AppData> data = categories.get(category).getData();
			
		if (data == null) 
			return; // should not happen
			
		boolean dirty = false;
		
		for (int i = data.size() - 1 ; i >= 0 ; i--) {
			AppData a = data.get(i);
			if (null == map.get(a.getComponent())) {
				data.remove(i);
				dirty = true;
			}
		}
		
		if (dirty) 
			putEntries(catPath(category), data);
	}
	//cleans categories files from deleted apps
	//updates category files
	public void cleanCategories() {
		for (String c: names) {
			cleanCategory(c);
		}
	}
	//adds new category
	public boolean addCategory(String c) {
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
		categories.put(c, new Category(c, new ArrayList<AppData>()));
		names.add(c);
		sortNames();
		writeCategoriesProps();
		return true;
	}
	//get entries of category from category file
	private ArrayList<AppData> getEntries(File f) {
		ArrayList<AppData> data = new ArrayList<AppData>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			
			String d;
			
			while (null != (d = reader.readLine())) {
				d = d.trim();
				if (d.length()>0) {
					AppData a = map.get(d);
					if (a != null)
						data.add(a);
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
	//writes app data into category file
	private void putEntries(File file, ArrayList<AppData> data) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));

			for (AppData a : data) 
				writer.write(a.getComponent() + "\n");
				
		} catch (IOException e) {
		}

		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
			}		
		}
	}
	private void readCategoriesProps() {
		BufferedReader reader = null;
		File file = new File(context.getFilesDir() + "/categories.props");
		try {
			reader = new BufferedReader(new FileReader(file));
			
			String d;
			String key = null;
			String value = null;
			String category = null;
			int index = -1;
			while (null != (d = reader.readLine())) {
				d = d.trim();
				if (names.contains(d)) {
					category = d;
					Category c = categories.get(category);
					d = reader.readLine();
					index = d.indexOf('=');
					key = d.substring(0, index).trim();
					value = d.substring(index+1, d.length()).trim();
					if (key.equals("hidden")) {
						if (value.equals("true")) {
							c.hide();
						} else {
							c.unhide();
						}
					}
					d = reader.readLine();
					index = d.indexOf('=');
					key = d.substring(0, index).trim();
					value = d.substring(index+1, d.length()).trim();
					if (key.equals("customName")) {
						if (value.equals("true")) {
							c.setCustom(true);
						} else {
							c.setCustom(false);
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
			} catch (IOException e) {
			}
	}
	private void writeCategoriesProps() {
		BufferedWriter writer = null;
		File file = new File(context.getFilesDir() + "/categories.props");
		try {
			writer = new BufferedWriter(new FileWriter(file));

			for (Category c : categories.values()) {
				writer.write(c.getName()+"\n");
				writer.write(c.getProps());
			}
				
		} catch (IOException e) {
		}

		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
			}		
		}
	}
	
	//return list of apps for default categories (All, Unclassified, Hidden)
	public ArrayList<AppData> filterApps(Map<String,AppData> map) {
		ArrayList<AppData> data = new ArrayList<AppData>();
		
		if (!isEditable(curCategory)) {
			data.addAll(map.values());
			if (curCategory == UNCLASSIFIED) {
				for (Category c : categories.values()) {
					if (!c.getName().equals(HISTORY)) {
						data.removeAll(c.getData());
					}
				}
			} else {
				ArrayList<AppData> c = categories.get(HIDDEN).getData();
				if (c != null)
					data.removeAll(c);
			}
		}
		else {
			ArrayList<AppData> c = categories.get(curCategory).getData();
			//Log.v("TinyLaunch", "filtering via "+curCategory+" "+c.size());
			if (c != null) {
				data.addAll(c);
			}
		}
		//if (customSorting) {}
		if (!curCategory.equals(HISTORY))
			Collections.sort(data, AppData.NameComparator);
		
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
	public boolean isCustom(String c) {
		return ! c.equals(ALL) && ! c.equals(UNCLASSIFIED) && ! c.equals(HIDDEN) && ! c.equals(HISTORY);
	}
	//can be filled with apps by the user
	public boolean isEditable(String c) {
		return ! c.equals(ALL) && ! c.equals(UNCLASSIFIED);
	}
	//checks if category has an app
	public boolean in(AppData item, String cat) {
		return categories.get(cat).getData().contains(item);
	}

	public String getCurCategory() {
		return curCategory;
	}
	
	public void hide(String catName) {
		categories.get(catName).hide();
		if (home.equals(catName)) {
			setHome(ALL);
		}
		writeCategoriesProps();
	}
	public void unhide(String catName) {
		categories.get(catName).unhide();
		if (catName.equals(getCurCategory())) {
			setCurCategory(ALL);
		}
		writeCategoriesProps();
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
		sortNames();
		writeCategoriesProps();
		setCurCategory(ALL);
		return true;
	}

	public void clearHistory() {
		history = new ArrayList<String>();
	}
}
