package ru.henridellal.emerald;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

public class TutorialActivity extends Activity{

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tutorial);
		((Button)findViewById(R.id.close_tutorial)).setOnClickListener(
		new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PreferenceManager.getDefaultSharedPreferences(TutorialActivity.this)
					.edit()
					.putBoolean(Keys.SHOW_TUTORIAL, false)
					.commit();
				TutorialActivity.this.finish();
			}
		});
	}
}
