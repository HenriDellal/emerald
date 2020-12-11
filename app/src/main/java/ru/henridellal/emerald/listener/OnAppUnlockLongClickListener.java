package ru.henridellal.emerald.listener;

import java.lang.ref.SoftReference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ru.henridellal.emerald.activity.Apps;
import ru.henridellal.emerald.data.AppData;
import ru.henridellal.emerald.data.BaseData;
import ru.henridellal.emerald.R;
import ru.henridellal.emerald.data.ShortcutData;
import ru.henridellal.emerald.preference.Keys;

public class OnAppUnlockLongClickListener implements View.OnLongClickListener {
	private SoftReference<Context> contextRef;
	public OnAppUnlockLongClickListener(Context context) {
		contextRef = new SoftReference<Context>(context);
	}
	
	@Override
	public boolean onLongClick(final View arg0) {
		AlertDialog.Builder builder = new AlertDialog.Builder(contextRef.get());
		builder.setTitle(contextRef.get().getResources().getString(R.string.type_password));
		final EditText inputBox = new EditText(contextRef.get());
		inputBox.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
		builder.setView(inputBox);
		builder.setPositiveButton(android.R.string.yes, 
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (inputBox.getText().toString().equals(((Apps)contextRef.get()).options.getString(Keys.PASSWORD, ""))) {
						BaseData tag = (BaseData) arg0.getTag();
						if (tag instanceof AppData) {
							((Apps)contextRef.get()).itemContextMenu((AppData)tag);
						} else if (tag instanceof ShortcutData) {
							((Apps)contextRef.get()).itemContextMenu((ShortcutData)tag);
						}
					} else {
						Toast.makeText(contextRef.get(), contextRef.get().getResources().getString(R.string.wrong_password), Toast.LENGTH_LONG).show();
					}
				}
			});
		builder.setNegativeButton(android.R.string.cancel,
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});

		builder.setCancelable(true);
		builder.show();
		return false;
	}
}
