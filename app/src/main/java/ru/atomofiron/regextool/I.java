package ru.atomofiron.regextool;


import android.content.SharedPreferences;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


public class I {

    public static final String toMainActivity = "ru.atomofiron.regextool.toMainActivity";
    public static final String closeService = "ru.atomofiron.regextool.closeService";

    public static final String PREF_FIRST_START = "PREF_FIRST_START";
    public static final String PREF_STORAGE_PATH = "pref_storage_path";
    public static final String PREF_EXTRA_FORMATS = "pref_extra_formats";
    public static final String PREF_ORIENTATION = "pref_orientation";
    public static final String PREF_DARK_THEME = "PREF_DARK_THEME";
    public static final String PREF_USE_ROOT = "PREF_USE_ROOT";
    public static final String PREF_HISTORY = "PREF_HISTORY";

    public static final int SEARCH_NOTHING = 0;
    public static final int SEARCH_ERROR = -1;
    public static final int SEARCH_OK = 1;
    public static final int REQUEST_FOR_INIT = 1;
    public static final int REQUEST_FOR_SEARCH = 2;
    public static final String SEARCH_CODE = "SEARCH_CODE";
    public static final String TARGET = "TARGET";
    public static final String SEARCH_LIST = "SEARCH_LIST";
    public static final String RESULT_LIST = "RESULT_LIST";
    public static final String RESULT_LIST_COUNTS = "RESULT_LIST_COUNTS";
    public static final String RESULT_LINE_NUMS = "RESULT_LINE_NUMS";
    public static final String RESULT_PATH = "RESULT_PATH";
    public static final String CASE_SENSE = "CASE_SENSE";
    public static final String REGEX = "REGEX";
    public static final String SEARCH_IN_FILES = "SEARCH_IN_FILES";
    public static final String SEARCH_REGEX = "SEARCH_REGEX";
    public static final String MAX_SIZE = "MAX_SIZE";
    public static final String SELECTED_LIST = "SELECTED_LIST";
    public static final String RES_PERM = "android.permission.READ_EXTERNAL_STORAGE";

    public static void Log(String message) {
        Log.e("atomofiron", message);
    }

    public static void Toast(Context context, String message, int time) {
        Toast.makeText(context, message, time).show();
    }
    public static void Snack(View fab, String message, boolean lengthLong) {
        Snackbar.make(fab, message, lengthLong?Snackbar.LENGTH_LONG:Snackbar.LENGTH_SHORT).show();
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
		if (index >= 0)
			path = path.substring(index);

		index = path.lastIndexOf('.');
		return index == -1 ? "" : path.substring(index + 1).toLowerCase();
	}

    public static SharedPreferences SP(Context co) {
        return PreferenceManager.getDefaultSharedPreferences(co);
    }

    public static void sleep(int sec) { try { Thread.sleep(sec * 1000); } catch (Exception ignored) {} }

    public interface SnackListener {
        public void Snack(String str);
    }

}
