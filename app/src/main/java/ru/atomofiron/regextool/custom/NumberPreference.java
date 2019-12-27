package ru.atomofiron.regextool.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.*;
import android.util.AttributeSet;
import ru.atomofiron.regextool.R;

public class NumberPreference extends Preference implements NumberText.OnInputListener {

	private NumberText editText = null;
	private int initialValue = 0;

	public NumberPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public NumberPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public NumberPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NumberPreference(Context context) {
		super(context);
	}

	{
		setWidgetLayoutResource(R.layout.edittext_number);
	}

	@Override
	protected Integer onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		initialValue = restoreValue ? getPersistedInt(initialValue) : (Integer) defaultValue;
	}

	@Override
	public void onBindViewHolder(PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);

		if (editText == null) {
			editText = (NumberText) holder.findViewById(R.id.number);
			editText.setFocusable(false);
			editText.setOnInputListener(this);
			editText.setText(String.valueOf(initialValue));
		}
	}

	@Override
	public void onClick() {
		editText.onClick(editText);
	}

	@Override
	public void onInput(int value) {
		callChangeListener(value);
		persistInt(value);
	}
}
