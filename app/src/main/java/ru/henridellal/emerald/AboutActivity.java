package ru.henridellal.emerald;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity{

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		String versionName = getResources().getString(R.string.version_name);
		((TextView) findViewById(R.id.about)).setText(getString(R.string.about, versionName));
	}
}
