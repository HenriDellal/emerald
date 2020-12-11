package ru.henridellal.emerald.data;

import android.content.Context;

import java.util.ArrayList;

public class Category {
	private ArrayList<BaseData> entries;
	private String name;
	private int stringResourceId;

	public Category(String name, ArrayList<BaseData> entries, int stringResourceId) {
		this.name = name;
		this.entries = entries;
		this.stringResourceId = stringResourceId;
	}

	public boolean hasCustomName() {
		return stringResourceId == 0;
	}

	public ArrayList<BaseData> getData() {
		return entries;
	}

	public BaseData get(int index) {
		return entries.get(index);
	}

	public String getRepresentName(Context c) {
		return hasCustomName() ? name : c.getResources().getString(stringResourceId);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
