package ru.atomofiron.regextool.CustomViews;

import android.content.Context;
import android.graphics.PorterDuff;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import java.util.regex.Pattern;

import ru.atomofiron.regextool.R;

public class RegexText extends android.support.v7.widget.AppCompatEditText implements TextWatcher {

	public boolean isRegex = false;
	private boolean need = true;
	private int start = 0;
	private int count = 0;
	private char deleted;

	public RegexText(Context context) {
		super(context);
		addTextChangedListener(this);
	}

	public RegexText(Context context, AttributeSet attrs) {
		super(context, attrs);
		addTextChangedListener(this);
	}

	public RegexText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		addTextChangedListener(this);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		deleted = count == 1 ? s.charAt(start) : ' ';
	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		this.start = start;
		this.count = count;
	}
	@Override
	public void afterTextChanged(Editable s) {
		if (!need)
			return;
		need = false;
		if (deleted == '[' || deleted == '{' || deleted == '(')
			s.replace(start, start+1, "");
		else if (count == 1) {
			int pos = start+1;
			char c = s.charAt(start);
			if (c == '[')
				insert(s, "]", pos);
			else if (c == '{')
				insert(s, "}", pos);
			else if (c == '(')
				insert(s, ")", pos);
		}
		need = true;

		checkPatternValid(isRegex);
	}

	private void insert(Editable s, String symbol, int pos) {
		s.replace(pos, pos, symbol);
		setSelection(start);
	}

	public void checkPatternValid(boolean isRegex) {
		this.isRegex = isRegex;
		if (isRegex)
			try {
				Pattern.compile(getText().toString());
				getBackground().clearColorFilter();
			} catch (Exception ignored) {
				getBackground().setColorFilter(
						getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
			}
		else
			getBackground().clearColorFilter();
	}
}
