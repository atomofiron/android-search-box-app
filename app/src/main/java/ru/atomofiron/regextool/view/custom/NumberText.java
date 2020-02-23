package ru.atomofiron.regextool.view.custom;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.AppCompatEditText;

public class NumberText extends AppCompatEditText implements View.OnClickListener, TextWatcher {

	private InputMethodManager inputMethodManager = null;
	private OnInputListener onInputListener = null;

	public NumberText(Context context) {
		super(context);
	}

	public NumberText(Context context, AttributeSet attrs) {
		super(context, attrs, android.R.attr.editTextStyle);
	}

	public NumberText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	{
		inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		addTextChangedListener(this);
		setFilters(new InputFilter[] { new InputFilter.LengthFilter(9) });
		setImeOptions(EditorInfo.IME_ACTION_DONE);
		setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
		setKeyListener(DigitsKeyListener.getInstance("0123456789"));
		setGravity(Gravity.CENTER_HORIZONTAL);
		setOnClickListener(this);
		setLongClickable(false);
		setHint("_____");
		setHintTextColor(0);
	}

	public void setOnInputListener(OnInputListener onInputListener) {
		this.onInputListener = onInputListener;
	}

	@Override
	public boolean isSuggestionsEnabled() {
		return false;
	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
	}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
	}

	@Override
	public void afterTextChanged(Editable editable) {
		String value = editable.toString();

		if (value.length() > 1 && value.startsWith("0")) {
			int start = getSelectionStart();

			setText(value.substring(1));

			setSelection(start == 0 ? 0 : start - 1);
		} else if (value.length() == 0) {
			setText("0");
			setSelection(1);
		} else if (onInputListener != null) {
			onInputListener.onInput(Integer.parseInt(getText().toString()));
		}
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && hasFocus()) {
			setFocusable(false);

			return true; // перехватываем событие, чтобы оно не обработалось при уже закрытой клавиатуре
		}

		return false;
	}

	@Override
	public void onEditorAction(int actionCode) {
		super.onEditorAction(actionCode);

		if (actionCode == EditorInfo.IME_ACTION_DONE)
			setFocusable(false);
	}

	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);

		if (focused) {
			inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
			setSelection(length());
		} else {
			inputMethodManager.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
			setFocusable(false);
		}
	}

	@Override
	public void onClick(View view) {
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
	}

	public interface OnInputListener {
		void onInput(int value);
	}
}
