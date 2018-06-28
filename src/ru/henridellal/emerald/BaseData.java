package ru.henridellal.emerald;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Comparator;

public class BaseData implements Comparable<BaseData> {
	//component is a package name
	protected String component;
	
	//name is app name
	protected String name;

	public static final Comparator<BaseData> NameComparator = 
		new Comparator<BaseData>() {

		public int compare(BaseData a, BaseData b) {
			return a.name.compareToIgnoreCase(b.name);
		}
	};

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
}
