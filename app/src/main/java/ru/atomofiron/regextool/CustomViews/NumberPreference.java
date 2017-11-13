package ru.atomofiron.regextool.CustomViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class NumberPreference extends Preference implements NumberText.OnInputListener {

	private NumberText editText = null;
	private int defaultValue; // это поле инициализируется в конструкторе суперкласса!

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

	@Override
	protected Integer onGetDefaultValue(TypedArray a, int index) {
		defaultValue = a.getInt(index, 0);
		return defaultValue;
	}

	@Override
	public void onBindViewHolder(PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);

		if (editText == null) {
			editText = new NumberText(getContext());
			editText.setFocusable(false);
			editText.setOnInputListener(this);
			editText.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
			));
			ViewGroup viewGroup = holder.itemView.findViewById(android.R.id.widget_frame);
			viewGroup.addView(editText);
			viewGroup.setVisibility(View.VISIBLE);

			editText.setText(
					String.valueOf(
							PreferenceManager.getDefaultSharedPreferences(getContext())
									.getInt(getKey(), defaultValue)
					)
			);
		}
	}

	@Override
	public void onClick() {
		editText.onClick(editText);
	}

	@Override
	public void onInput(int value) {
		persistInt(value);
	}
}
