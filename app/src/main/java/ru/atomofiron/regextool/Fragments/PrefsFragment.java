package ru.atomofiron.regextool.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.R;
import ru.atomofiron.regextool.Utils.Cmd;

public class PrefsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

	private SharedPreferences sp;

	public PrefsFragment() {}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		setHasOptionsMenu(true);

		sp = I.sp(getActivity());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_preferences, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.tips)
			I.showHelp(getContext());

		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		PreferenceScreen prefScreen = getPreferenceScreen();
		for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
			Preference pref = prefScreen.getPreference(i);
			if (pref.getClass().equals(PreferenceCategory.class)) {
				PreferenceCategory prefCategory = (PreferenceCategory) pref;
				for (int j = 0; j < prefCategory.getPreferenceCount(); j++) {
					pref = prefCategory.getPreference(j);
					pref.setOnPreferenceChangeListener(this);
					update(pref, null);
				}
			}
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		boolean result = update(preference, newValue);

		if (preference.getKey().equals(I.PREF_THEME) || preference.getKey().equals(I.PREF_ORIENTATION))
			getActivity().recreate();
		else if (preference.getKey().equals(I.PREF_MAX_SIZE))
			update(preference, newValue);

		return result;
	}

	private boolean update(Preference pref, Object newValue) {
		String value = newValue == null ? null : newValue.toString();

		String key = pref.getKey();
		switch (key) {
			case I.PREF_STORAGE_PATH:
			case I.PREF_EXTRA_FORMATS:
				pref.setSummary(value == null ? sp.getString(key, "") : value);
				break;
			case I.PREF_SPECIAL_CHARACTERS:
				pref.setSummary(value == null ?
						sp.getString(I.PREF_SPECIAL_CHARACTERS, I.DEFAULT_SPECIAL_CHARACTERS) : value);
				break;
			case I.PREF_THEME:
				pref.setSummary(getResources().getStringArray(R.array.theme_var)
						[Integer.parseInt(value == null ? sp.getString(key, "0") : value)]);
				break;
			case I.PREF_ORIENTATION:
				int i = Integer.parseInt(value == null ? sp.getString(key, "2") : value);
				pref.setSummary(getResources().getStringArray(R.array.orientation_var)[i]);
				break;
			case I.PREF_USE_SU:
				if (value == null) {
					if (sp.getBoolean(key, false) && !Cmd.checkSu())
						((SwitchPreferenceCompat)pref).setChecked(false);
				} else if (value.equals("true"))
					return Cmd.checkSu();

				break;
			case I.PREF_MAX_SIZE:
				if (newValue == null)
					newValue = sp.getInt(key, 0);

				pref.setSummary(I.intToHumanReadable((int) newValue, getResources().getStringArray(R.array.size_suffix_arr)));
				break;
		}
		return true;
	}

}
