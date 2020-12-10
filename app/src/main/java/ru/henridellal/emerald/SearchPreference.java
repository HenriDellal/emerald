package ru.henridellal.emerald;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.EditText;

public class SearchPreference extends DialogPreference {
	private String searchValue;
	private TypedArray searchProviders;

	public SearchPreference(Context c) {
		super(c);
		searchProviders = getContext().getResources().obtainTypedArray(R.array.search_values);
	}

	public SearchPreference(Context c, AttributeSet attr) {
		super(c, attr);
		searchProviders = getContext().getResources().obtainTypedArray(R.array.search_values);
	}

	private void showCustomSearchDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		final EditText inputField = new EditText(getContext());
		inputField.setText(searchValue);
		builder.setTitle(getContext().getResources().getString(R.string.searchProvider));
		builder.setView(inputField);
		builder.setPositiveButton(android.R.string.yes, 
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					searchValue = inputField.getText().toString();
				}
			}
		);
		builder.create().show();
	}

	private int getValueIndex() {
		for (int i = 1; i < searchProviders.length(); i++) {
			if (searchProviders.getString(i).equals(searchValue)) {
				return i;
			}
		}
		return 0;
	}

	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		builder.setTitle(getContext().getResources().getString(R.string.searchProvider));
		builder.setSingleChoiceItems(R.array.search_entries, getValueIndex(),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0) {
						showCustomSearchDialog();
					} else {
						searchValue = searchProviders.getString(which);
					}
				}
			});
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			persistString(searchValue);
		}
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		searchValue = (restoreValue ? getPersistedString(searchValue) : defaultValue.toString());
	}
}
