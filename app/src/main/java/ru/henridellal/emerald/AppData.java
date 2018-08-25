package ru.henridellal.emerald;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class AppData extends BaseData {
	
	//Constants for parsing
	public static final String COMPONENT = "C";
	public static final String NAME = "N";
	
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
			return (component).hashCode();
	}
	
	public AppData() {
		super();
	}
	
	public AppData(String component, String name) {
		super(component, name);
	}
	
	public void read(BufferedReader reader, String firstLineOfData){
		try {
			this.component = firstLineOfData.substring(1).trim();
			this.name = readLine(reader, NAME).substring(1).trim();
		} catch (IOException e) {
		
		}
		/*String component = reader.readLine();
		if (component == null || !component.startsWith(COMPONENT))
			throw new IOException();
		this.component = component.substring(1).trim();
		String name = reader.readLine();
		if (name == null || !name.startsWith(NAME))
			throw new IOException();
		this.name = name.substring(1).trim();*/
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

}
