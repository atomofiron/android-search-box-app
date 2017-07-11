package ru.atomofiron.regextool.CustomViews;

import android.content.Context;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.atomofiron.regextool.R;

public class RegexText extends android.support.v7.widget.AppCompatEditText implements TextWatcher {
	private static final int DELAY_AFTER_TYPING_MS = 300;

	private boolean isRegex = false;
	/** Запрет реагировать на изменение текста. */
	private boolean locked = false;
	private int start = 0;
	private int count = 0;
	private char deleted;
	private EditText testField;
	private Handler handler = new Handler();

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

		checkPatternValid();
		postTest();
	}

	private void insert(Editable s, String symbol, int pos) {
		s.replace(pos, pos, symbol);
		setSelection(start);
	}

	public void checkPatternValid(boolean isRegex) {
		this.isRegex = isRegex;

		checkPatternValid();
		test();
	}

	private void checkPatternValid() {
		if (isRegex && !isPatternValid())
			getBackground().setColorFilter(
					getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
		else
			getBackground().clearColorFilter();
	}

	private boolean isPatternValid() {
		try {
			Pattern.compile(getText().toString());
		} catch (Exception ignored) {
			return false;
		}
		return true;
	}

	public void setTestField(EditText editText) {
		testField = editText;
		testField.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			public void afterTextChanged(Editable s) {
				if (locked || s.length() == 0)
					return;

				postTest();
			}
		});
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
		locked = true;

		String query = getText().toString();
		String text = testField.getText().toString();
		int selectionStart = testField.getSelectionStart();

		if (isRegex && !isPatternValid() || query.isEmpty() || text.isEmpty()) {
			testField.setText(text); // getText().clearSpans() приводит к некорректному поведению
			testField.setSelection(selectionStart);

			locked = false;
			return;
		}

		int offset = 0;
		Pattern pattern = isRegex ? Pattern.compile(query) : null;
		Spannable spanRange = new SpannableString(text);

		for (String s : text.split("\n")) {
			if (isRegex) {
				int pos = 0;
				Matcher matcher = pattern.matcher(s);
				while (matcher.find(pos)) {
					spanRange.setSpan(
							new BackgroundColorSpan(Color.argb(128, 0, 128, 0)),
							offset + matcher.start(),
							offset + matcher.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
					);
					pos = matcher.end();
				}
			} else {
				int pos = 0;
				int index;
				while ((index = s.indexOf(query, pos)) != -1) {
					spanRange.setSpan(
							new BackgroundColorSpan(Color.argb(128, 0, 128, 0)),
							offset + index,
							offset + index + query.length(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
					);
					pos = index + query.length();
				}
			}
			offset += s.length() + 1;
		}
		testField.setText(spanRange);
		testField.setSelection(selectionStart);

		locked = false;
	}
}
