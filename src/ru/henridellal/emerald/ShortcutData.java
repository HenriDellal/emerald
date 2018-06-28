package ru.henridellal.emerald;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class ShortcutData extends BaseData {
	
	public static String SHORTCUT_PACKAGE = "P";
	public static String SHORTCUT_NAME = "S";
	public static String SHORTCUT_INTENT = "I";
	
	protected String intent;
	public String getIntent() {
		return intent;
	}
	
	public ShortcutData() {
		super();
	}
	public ShortcutData(String component, String name, String intent) {
		super(component, name);
		this.intent = intent;
	}
	
	@Override
	public boolean equals(Object a) {
		if (! (a instanceof ShortcutData))
			return false;
		if (intent == null) {
			return a == null || ((ShortcutData)a).intent == null;
		}
		return intent.equals( ((ShortcutData)a).intent );
	}
	
	public void read(BufferedReader reader, String firstLineOfData){
		try {
			this.component = firstLineOfData.substring(1).trim();
			this.name = readLine(reader, SHORTCUT_NAME).substring(1).trim();
			this.intent = readLine(reader, SHORTCUT_INTENT).substring(1).trim();
		} catch (IOException e) {
		
		}
	}
	
	public void write(BufferedWriter writer) throws IOException {
		writer.write(new StringBuilder(SHORTCUT_PACKAGE)
		.append(this.component)
		.append("\n")
		.append(SHORTCUT_NAME)
		.append(this.name)
		.append("\n")
		.append(SHORTCUT_INTENT)
		.append(this.intent)
		.append("\n").toString());
	}
}
