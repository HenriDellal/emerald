package ru.henridellal.emerald;

import android.content.Context;

import java.util.ArrayList;

public class Category {
	private boolean isHidden;
	private ArrayList<AppData> entries;
	private String name;
	private int stringResourceId;
	public Category(String name, ArrayList<AppData> entries) {
		this(name, entries, 0);
	}
	public Category(String name, ArrayList<AppData> entries, int stringResourceId) {
		this.entries = entries;
		this.name = name;
		this.stringResourceId = stringResourceId;
	}
	public boolean hasCustomName() {
		return stringResourceId == 0;
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
	}
	public String getRepresentName(Context context) {
		return hasCustomName() ? name : context.getResources().getString(stringResourceId);
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public String getProps() {
		return "hidden="+isHidden+"\n";
	}
}
