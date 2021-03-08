package ru.henridellal.emerald.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.InputType;
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
		tv = new EditText(getContext());
		return(tv);
	}
	
	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		tv.setInputType(InputType.TYPE_CLASS_NUMBER);
		tv.setText(lastValue.toString());
		tv.requestFocusFromTouch();
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		String value = tv.getText().toString();
		if (positiveResult && callChangeListener(value)) {
			try {
				lastValue = Integer.valueOf(value);
				persistInt(lastValue);
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(getContext(), "This value is not an integer", Toast.LENGTH_LONG).show();
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
