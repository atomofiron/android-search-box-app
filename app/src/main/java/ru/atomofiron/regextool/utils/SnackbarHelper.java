package ru.atomofiron.regextool.utils;

import android.content.res.Resources;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarHelper {
	private final View view;
	private final Resources resources;

	public SnackbarHelper(View view) {
		this.view = view;
		resources = view.getResources();
	}

	public void show(int stringId) {
		show(resources.getString(stringId), Snackbar.LENGTH_SHORT);
	}

	public void showLong(int stringId) {
		show(resources.getString(stringId), Snackbar.LENGTH_LONG);
	}

	public void show(String message) {
		show(message, Snackbar.LENGTH_SHORT);
	}

	public void showLong(String message) {
		show(message, Snackbar.LENGTH_LONG);
	}

	private void show(String message, int length) {
		Snackbar.make(view, message, length).show();
	}

	public void show(int messageStringId, int actionStringId, View.OnClickListener listener) {
		show(resources.getString(messageStringId), actionStringId, listener);
	}

	public void show(String message, int actionStringId, View.OnClickListener listener) {
		Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
		snackbar.setAction(actionStringId, listener);
		snackbar.show();
	}
}
