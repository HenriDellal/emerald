package ru.henridellal.emerald;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class BackupPreference extends DialogPreference {
	private FileLoaderDialog dialog;
	public BackupPreference(Context c) {
		this(c, null);
	}
	public BackupPreference(Context c, AttributeSet attr) {
		super(c, attr);
	}

	@Override
	protected View onCreateDialogView() {
		int mode = 0;
		if ("backup".equals(getKey())) {
			mode = 0;
		} else if ("restore".equals(getKey())) {
			mode = 1;
		}
		if (Build.VERSION.SDK_INT >= 19) {
			Intent intent;
			if (mode == 1) {
				intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			} else {
				intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			}

			// Filter to only show results that can be "opened", such as
			// a file (as opposed to a list of contacts or timezones).
			intent.addCategory(Intent.CATEGORY_OPENABLE);

			// Create a file with the requested MIME type.
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TITLE, "emerald-launcher-preferences.txt");
			((Options) getContext()).startActivityForResult(intent, mode);
			return null;
		} else {
			dialog = new FileLoaderDialog(this, getContext(), mode);
			return(dialog);
		}
	}

	@Override
	protected void showDialog(Bundle b) {
		super.showDialog(b);
		if (Build.VERSION.SDK_INT >= 19) {
			// the SAF dialog is shown already
			this.getDialog().hide();
		}
	}
}
