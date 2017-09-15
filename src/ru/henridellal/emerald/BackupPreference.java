package ru.henridellal.emerald;

//import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class BackupPreference extends DialogPreference {
	Context context;
	FileLoaderDialog dialog;
	public BackupPreference(Context c) {
		this(c, null);
	}
	public BackupPreference(Context c, AttributeSet attr) {
		super(c, attr);
		context = c;
	}
	@Override
	protected View onCreateDialogView() {
			dialog = new FileLoaderDialog(getContext(), getKey().equals("backup")? 0:1);
		return(dialog);
	}
	
	/*@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
	}*/
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if (positiveResult) {
			((Options)getContext()).backupPrefs();
		}
	}
}
