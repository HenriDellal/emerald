package ru.henridellal.emerald;

import android.content.Context;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Comparator;

public abstract class BaseData implements Comparable<BaseData> {
	//component is a package name
	protected String component;
	
	//name is app name
	protected String name;

	public String getId() {
		return component;
	}
	public static final Comparator<BaseData> NameComparator = 
		new Comparator<BaseData>() {

		public int compare(BaseData a, BaseData b) {
			return a.name.compareToIgnoreCase(b.name);
		}
	};
	
	@Override
	public boolean equals(Object a) {
		if (! (a instanceof BaseData))
			return false;
		if (component == null) {
			return a == null || ((BaseData)a).component == null;
		}
		return component.equals( ((BaseData)a).component );
	}
	
	public BaseData() {}
	public BaseData(String component, String name) {
		this.component = component;
		this.name = name;
	}
	public String getComponent() {
		return this.component;
	}
	public String getName() {
		return this.name;
	}
	public void read(BufferedReader reader, String firstLineOfData) {}
	public void write(BufferedWriter reader) throws IOException {
		throw new IOException();
	}
	protected String readLine(BufferedReader reader, String key) throws IOException {
		String line = reader.readLine();
		if (line == null || !line.startsWith(key))
			throw new IOException();
		return line;
	}
	
	@Override
	public int compareTo(BaseData arg0) {
		return arg0.name.compareToIgnoreCase(this.name);
	}

	abstract public Intent getLaunchIntent(Context context);
	
	@Override
	public int hashCode() {
		if (component == null)
			return "NULL null NULL".hashCode();
		else
			return (component).hashCode();
	}
}
