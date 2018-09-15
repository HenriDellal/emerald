package ru.henridellal.emerald;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;

public class MoveCustomIconsTask extends AsyncTask<Void, Void, Void> {
	private Context context;

	public MoveCustomIconsTask(Context context) {
		super();
		this.context = context;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
	@Override
	protected Void doInBackground(Void... values) {
		String fileName;
		for (File sourceFile : context.getCacheDir().listFiles()) {
			fileName = sourceFile.getName();
			if (fileName.endsWith(".custom.png")) {
				fileName = new StringBuilder(fileName).delete(fileName.length()-11, fileName.length()-4).toString();
				File destFile = new File(context.getFilesDir(), fileName);
				FileUtils.move(context, sourceFile, destFile);
			}
		}
		return null;
	}
	@Override
	protected void onPostExecute(Void value) {
		super.onPostExecute(value);
	}
}
