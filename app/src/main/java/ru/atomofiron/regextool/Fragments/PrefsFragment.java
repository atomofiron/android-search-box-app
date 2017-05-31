package ru.atomofiron.regextool.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import java.util.Map;

import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.R;

public class PrefsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

	public PrefsFragment() {}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		Map<String, String> values = (Map<String, String>) I.SP(getActivity()).getAll();

		for (String key : new String[] { I.PREF_STORAGE_PATH, I.PREF_EXTRA_FORMATS, I.PREF_ORIENTATION }) {
			Preference pref = findPreference(key);
			pref.setOnPreferenceChangeListener(this);
			pref.setSummary(values.get(key));
		}
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		preference.setSummary(newValue.toString());
		return true;
	}
}
