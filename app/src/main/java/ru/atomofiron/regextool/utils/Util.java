package ru.atomofiron.regextool.utils;


import android.content.SharedPreferences;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.util.Collection;

import ru.atomofiron.regextool.R;

public class Util {

    public static final String PREF_LAST_VERSION = "PREF_LAST_VERSION";
    public static final String PREF_FIRST_START = "PREF_FIRST_START";
    public static final String PREF_STORAGE_PATH = "pref_storage_path";
    public static final String PREF_SPECIAL_CHARACTERS = "pref_special_characters";
    public static final String PREF_EXTRA_FORMATS = "pref_extra_formats";
    public static final String PREF_ORIENTATION = "pref_orientation";
    public static final String PREF_THEME = "pref_theme";
    public static final String PREF_MAX_SIZE = "max_size";
    public static final String PREF_MAX_DEPTH = "max_depth";
    public static final String PREF_EXCLUDE_DIRS = "exclude_dirs";
    public static final String PREF_USE_SU = "pref_use_su";
    public static final String PREF_CURRENT_DIR = "pref_current_dir";

    public static final String SEARCH_COUNT = "SEARCH_COUNT";
    public static final String SEARCH_LIST = "SEARCH_LIST";
    public static final String CASE_SENSE = "CASE_SENSE";
    public static final String QUERY = "QUERY";
    public static final String SEARCH_IN_FILES = "SEARCH_IN_FILES";
    public static final String SEARCH_REGEX = "SEARCH_REGEX";
    public static final String MULTILINE = "MULTILINE";
    public static final String SELECTED_LIST = "SELECTED_LIST";

    public static final String NOTIFICATION_CHANNEL_ID = "channel_id";
    public static final String DEFAULT_EXTRA_FORMATS = "md mkd markdown cm ad adoc";
    public static final String DEFAULT_SPECIAL_CHARACTERS = "\\ [ { ? + * ^ $";

    public static void log9(String message) {
        Log.e("atomofiron", message);
    }

    public static boolean isTextFile(String path, String[] extra) {
		path = getFormat(path);

		if (path.isEmpty())
			return false;

        switch (path) {
            case "txt":
            case "java":
            case "xml":
            case "html":
            case "htm":
            case "smali":
            case "log9":
            case "js":
            case "css":
            case "json":
                return true;
        }
        for (String s : extra)
            if (path.equals(s))
                return true;
        return false;
    }

    public static String getFormat(String path) {
		int index = path.lastIndexOf('/');
		if (index == -1) {
            if (path.lastIndexOf('.') == -1)
                return path;
        } else
            path = path.substring(index);

		index = path.lastIndexOf('.');
		return index == -1 ? "" : path.substring(index + 1).toLowerCase();
	}

    public static SharedPreferences sp(Context co) {
        return PreferenceManager.getDefaultSharedPreferences(co);
    }

    public static void sleep(int sec) { try { Thread.sleep(sec * 1000); } catch (Exception ignored) {} }


    public static void showHelp(Context co) {
        new AlertDialog.Builder(co)
                .setTitle(co.getString(R.string.tips))
                .setMessage(co.getString(R.string.tips_message))
                .setNegativeButton("Ok", null)
                .create().show();
    }

    public static boolean equivalent(Collection a, Collection b) {
        if (a.size() != b.size())
            return false;

        for (Object o : a)
            if (!b.contains(o))
                return false;

        return true;
    }

    public static String intToHumanReadable(final int bytes, String[] suffixes) {
    	int k = 0;
    	float f = (float) bytes;
    	while (f >= 970) {
    		f /= 1024;
    		k++;
		}
		return String.format("%1$.2f %2$s", f, suffixes[k])
				.replaceAll("[.,]00|(?<=[.,][0-9])0", "");
    }
}
