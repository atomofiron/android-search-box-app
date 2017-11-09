package ru.atomofiron.regextool;


import android.content.SharedPreferences;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Collection;

public class I {

    public static final String toMainActivity = "ru.atomofiron.regextool.toMainActivity";
    public static final String closeService = "ru.atomofiron.regextool.closeService";

    public static final String PREF_FIRST_START = "PREF_FIRST_START";
    public static final String PREF_HELP = "pref_help";
    public static final String PREF_STORAGE_PATH = "pref_storage_path";
    public static final String PREF_EXTRA_FORMATS = "pref_extra_formats";
    public static final String PREF_ORIENTATION = "pref_orientation";
    public static final String PREF_THEME = "pref_theme";
    public static final String PREF_USE_ROOT = "pref_use_root";
    public static final String PREF_HISTORY = "PREF_HISTORY";
    public static final String PREF_HISTORY_PINNED = "PREF_HISTORY_PINNED";

    public static final int SEARCH_NOTHING = 0;
    public static final int SEARCH_ERROR = -1;
    public static final int SEARCH_OK = 1;
    public static final int REQUEST_FOR_INIT = 1;
    public static final int REQUEST_FOR_SEARCH = 2;
    public static final String SEARCH_CODE = "SEARCH_CODE";
    public static final String SEARCH_LIST = "SEARCH_LIST";
    public static final String RESULT_LIST = "RESULT_LIST";
    public static final String RESULT = "RESULT";
    public static final String CASE_SENSE = "CASE_SENSE";
    public static final String QUERY = "QUERY";
    public static final String SEARCH_IN_FILES = "SEARCH_IN_FILES";
    public static final String SEARCH_REGEX = "SEARCH_REGEX";
    public static final String MULTILINE = "MULTILINE";
    public static final String MAX_SIZE = "MAX_SIZE";
    public static final String SELECTED_LIST = "SELECTED_LIST";
    public static final String RES_PERM = "android.permission.READ_EXTERNAL_STORAGE";

    public static void log(String message) {
        Log.e("atomofiron", message);
    }

    public static void toast(Context context, String message, int time) {
        Toast.makeText(context, message, time).show();
    }

    public static void toast(Context context, int stringId) {
        Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show();
    }
    public static void toast(Context context, int stringId, boolean LONG) {
        Toast.makeText(context, stringId, LONG ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
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
            case "log":
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
                .setTitle(co.getString(R.string.pref_help))
                .setMessage(co.getString(R.string.help))
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
}
