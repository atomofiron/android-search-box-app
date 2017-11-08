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
import android.view.View;
import android.view.ViewGroup;

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

		sp = I.SP(getActivity());
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
			} else if (pref.getKey().equals(I.PREF_HELP))
				pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						I.showHelp(getContext());
						return false;
					}
				});
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
			case I.PREF_THEME:
				pref.setSummary(getResources().getStringArray(R.array.theme_var)
						[Integer.parseInt(value == null ? sp.getString(key, "0") : value)]);
				break;
			case I.PREF_ORIENTATION:
				int i = Integer.parseInt(value == null ? sp.getString(key, "2") : value);
				pref.setSummary(getResources().getStringArray(R.array.orientation_var)[i]);
				break;
			case I.PREF_USE_ROOT:
				if (value == null) {
					if (sp.getBoolean(key, false) && Cmd.easyExec("su") != 0)
						((SwitchPreferenceCompat)pref).setChecked(false);
					break;
				} else if (value.equals("true"))
					return Cmd.easyExec("su") == 0;
		}
		return true;
	}

}
