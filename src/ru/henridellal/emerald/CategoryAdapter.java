package ru.henridellal.emerald;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

public class CategoryAdapter extends ArrayAdapter<String> {
	public CategoryAdapter(Context context, int resource, List<String> categoryNames) {
		super(context, resource, categoryNames);
	}
	public void update(ArrayList<String> content) {
		clear();
		addAll(content);
		notifyDataSetChanged();
	}
}
