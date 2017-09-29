package ru.henridellal.emerald;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class EditIntPreference extends DialogPreference {
	private Integer lastValue = (Integer)0;
	private EditText tv;
	public EditIntPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected View onCreateDialogView() {
		tv=new EditText(getContext());
		return(tv);
	}
	
	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		tv.setText(lastValue.toString());
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			if (callChangeListener(tv.getText())) {
				try {
					lastValue = Integer.parseInt(tv.getText().toString());
					persistInt(lastValue);
				} catch (Exception e) {
					Toast.makeText(getContext(), "This value is not an integer", Toast.LENGTH_LONG).show();
				}
			}
		}
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return(a.getInt(index, 0));
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		lastValue = (restoreValue ? getPersistedInt(lastValue) : (Integer)defaultValue);
	}
}
