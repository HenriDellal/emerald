package ru.henridellal.emerald;

//import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.preference.DialogPreference;
import android.util.AttributeSet;

//required by permissions checker
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;

public class BackupPreference extends DialogPreference {
	private FileLoaderDialog dialog;
	public BackupPreference(Context c) {
		this(c, null);
	}
	public BackupPreference(Context c, AttributeSet attr) {
		super(c, attr);
	}
	@Override
	public void onClick() {
		// request runtime permissions (Marshmallow+)
        if (Build.VERSION.SDK_INT >= 23) {
        	if ((getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        		|| (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {
        		((Options)getContext()).requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        		return;
        	} else {
        		super.onClick();
        	}
        } else {
        	super.onClick();
        }
	}
	@Override
	protected View onCreateDialogView() {
		int mode = 0;
		if ("backup".equals(getKey())) {
				mode = 0;
		} else if ("restore".equals(getKey())) {
				mode = 1;
		} else if ("convert".equals(getKey())) {
				mode = 2;
		}
		dialog = new FileLoaderDialog(this, getContext(), mode);
		return(dialog);
	}
	
}
