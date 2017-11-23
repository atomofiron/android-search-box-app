package ru.atomofiron.regextool.CustomViews;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.widget.EditText;


import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.Models.Finder;
import ru.atomofiron.regextool.R;
import ru.atomofiron.regextool.Models.Result;

public class RegexText extends android.support.v7.widget.AppCompatEditText implements TextWatcher {
	private static final int DELAY_AFTER_TYPING_MS = 300;
	private static final String LAST_QUERY = "LAST_QUERY";
	private static final String TEST_TEXT = "TEST_TEXT";
	private final int SPAN_COLOR;

	/** Запрет реагировать на изменение текста. */
	private boolean locked = false;
	private int start = 0;
	private int count = 0;
	private char deleted;
	private EditText testField;
	private Handler handler = new Handler();

	private final Finder finder = new Finder();
	private SharedPreferences sp;

	public RegexText(Context context) {
		super(context);
	}

	public RegexText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RegexText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	{
		Context context = getContext();

		addTextChangedListener(this);
		sp = I.sp(context);

		SPAN_COLOR = context.getResources().getColor(R.color.spanGreen);
		setText(sp.getString(LAST_QUERY, ""));
		setSelection(getText().length());
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
		if (locked)
			return;
		locked = true;

		if ((deleted == '[' || deleted == '{' || deleted == '(') && s.length() > start)
			switch (s.charAt(start)) {
				case ']': case '}': case ')':
					s.replace(start, start+1, "");
			}
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
		locked = false;

		finder.setQuery(s.toString());
		updateMark();
		postTest();
	}

	private void insert(Editable s, String symbol, int pos) {
		s.replace(pos, pos, symbol);
		setSelection(start);
	}

	public void setRegex(boolean isRegex) {
		finder.setRegex(isRegex);

		updateMark();
		test();
	}

	public void setCaseSense(boolean caseSense) {
		finder.setCaseSense(caseSense);
		test();
	}

	public void setMultiline(boolean miltiline) {
		finder.setMultiline(miltiline);
		test();
	}

	private void updateMark() {
		if (finder.isRegex() && !finder.regexpIsValid())
			getBackground().setColorFilter(
					getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
		else
			getBackground().clearColorFilter();
	}

	public void setTestField(EditText editText) {
		testField = editText;
		testField.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			public void afterTextChanged(Editable s) {
				if (locked)
					return;

				if (s.length() == 0) {
					sp.edit().putString(TEST_TEXT, "").apply();
					return;
				}

				postTest();
			}
		});

		testField.setText(new SpannableString(sp.getString(TEST_TEXT, "")), BufferType.SPANNABLE);
	}

	private void postTest() {
		handler.removeMessages(0);
		handler.postDelayed(new Runnable() {
			public void run() {
				test();
			}
		}, DELAY_AFTER_TYPING_MS);
	}

	private void test() {
		Editable editable = testField.getText();
		Result result = finder.search(testField.getText().toString());

		for (BackgroundColorSpan span : editable.getSpans(0, editable.length(), BackgroundColorSpan.class))
			editable.removeSpan(span);

		while (result.hasNext()) {
			int[] region = result.next();
			editable.setSpan(new BackgroundColorSpan(SPAN_COLOR), region[0], region[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		sp.edit().putString(TEST_TEXT, editable.toString()).apply();
		sp.edit().putString(LAST_QUERY, getText().toString()).apply();
	}
}
