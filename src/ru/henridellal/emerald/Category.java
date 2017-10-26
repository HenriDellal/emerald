package ru.henridellal.emerald;

import java.util.ArrayList;

public class Category {
	private boolean isHidden, hasCustomName;
	private ArrayList<AppData> entries;
	private String name;
	//private int stringResource;
	public Category(String name, ArrayList<AppData> entries) {
		this(name, entries, true);
	}
	public Category(String name, ArrayList<AppData> entries, boolean hasCustomName) {
		this.entries = entries;
		this.name = name;
		this.hasCustomName = hasCustomName;
	}
	public boolean hasCustomName() {
		return hasCustomName;
	}
	public boolean isHidden() {
		return isHidden;
	}
	public void hide() {
		isHidden = true;
	}
	public void unhide() {
		isHidden = false;
	}
	public ArrayList<AppData> getData() {
		return entries;
	}
	public AppData get(int index) {
		return entries.get(index);
	}
	public void remove(int index) {
		entries.remove(index);
	}
	public void remove(AppData app) {
		entries.remove(app);
	}
	public void removeAll() {
		entries.removeAll(entries);
		//Collections.removeAll(entries);
	}
	/*public File getPath() {
		return new File(context.getFilesDir()+"/"+URLEncoder.encode(category)+".cat");
	}*/
	/*public String getRepresentName() {
		return hasCustomName ? name : getResources().getString(stringResource);
	}*/
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if (hasCustomName)
			this.name = name;
	}
	public void setCustom(boolean hasCustomName) {
		this.hasCustomName = hasCustomName;
	}
	public String getProps() {
		return "hidden="+isHidden+"\n" +"customName="+hasCustomName+"\n";
	}
}
