package ru.henridellal.emerald;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Comparator;

public class AppData implements Comparable<AppData> {
	//component is a package name
	private String component;
	
	//name is app name
	public String name;

	public String getComponent() {
		return this.component;
	}
	//Constants for parsing
	private static final String COMPONENT = "C";
	private static final String NAME = "N";
	
	//app names comparator
	public static final Comparator<AppData> NameComparator = 
		new Comparator<AppData>() {

		public int compare(AppData a, AppData b) {
			return a.name.compareToIgnoreCase(b.name);
		}
	};
	
	public AppData() {			
	}
	
	@Override
	public boolean equals(Object a) {
		if (! (a instanceof AppData))
			return false;
		if (component == null) {
			return a == null || ((AppData)a).component == null;
		}
		return component.equals( ((AppData)a).component );
	}
	
	@Override
	public int hashCode() {
		if (component == null)
			return "NULL null NULL".hashCode();
		else
			return ("AppData:"+component).hashCode();
	}
	
	public AppData(String component, String name) {
		this.component = component;
		this.name = name;
	}
	
	public void read(BufferedReader reader) throws IOException {
		String component = reader.readLine();
		if (component == null || !component.startsWith(COMPONENT))
			throw new IOException();
		this.component = component.substring(1).trim();
		String name = reader.readLine();
		if (name == null || !name.startsWith(NAME))
			throw new IOException();
		this.name = name.substring(1).trim();
	}
	//writes app data in given file writer
	public void write(BufferedWriter writer) throws IOException {
		writer.write(new StringBuilder(COMPONENT)
		.append(this.component)
		.append("\n")
		.append(NAME)
		.append(this.name)
		.append("\n").toString());
	}

	@Override
	public int compareTo(AppData arg0) {
		return arg0.name.compareToIgnoreCase(this.name);
	}
}
