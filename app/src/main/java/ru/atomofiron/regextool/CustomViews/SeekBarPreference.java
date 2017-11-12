package ru.atomofiron.regextool.CustomViews;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends android.support.v7.preference.SeekBarPreference implements SeekBar.OnSeekBarChangeListener {

	private TextView valueView;

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SeekBarPreference(Context context) {
		super(context);
	}

	@Override
	public void onBindViewHolder(PreferenceViewHolder view) {
		super.onBindViewHolder(view);

		valueView = (TextView) view.findViewById(android.support.v7.preference.R.id.seekbar_value);
		((SeekBar) view.findViewById(android.support.v7.preference.R.id.seekbar)).setOnSeekBarChangeListener(this);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
		valueView.setText(String.valueOf(i));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		super.setValue(seekBar.getProgress());
	}
}
