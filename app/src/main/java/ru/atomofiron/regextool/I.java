package ru.atomofiron.regextool;


import android.content.pm.PackageManager;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


public class I {

    static final String toMainActivity = "ru.atomofiron.regextool.toMainActivity";
    static final String closeService = "ru.atomofiron.regextool.closeService";
    static final String PREFS = "ru.atomofiron.regextool_preferences";
    static final String PREF_FIRST_START = "PREF_FIRST_START";
    static final String STORAGE_PATH = "STORAGE_PATH";
    static final int SEARCH_NOTHING = 0;
    static final int SEARCH_ERROR = -1;
    static final int SEARCH_OK = 1;
    static final int REQUEST_FOR_SEARCH = 1;
    static final int REQUEST_FOR_SELECT = 2;
    static final int OK = 61;
    static final int NO_OK = 60;
    static final String SEARCH_CODE = "SEARCH_CODE";
    static final String TARGET = "TARGET";
    static final String SEARCH_LIST = "SEARCH_LIST";
    static final String RESULT_LIST = "RESULT_LIST";
    static final String RESULT_LIST_LINES = "RESULT_LIST_LINES";
    static final String RESULT_LINE_COUNTS = "RESULT_LINE_COUNTS";
    static final String RESULT_LIST_LINE_COUNTS = "RESULT_LIST_LINE_COUNTS";
    static final String RESULT_PATH = "RESULT_PATH";
    static final String CASE_SENSE = "CASE_SENSE";
    static final String REGEX = "REGEX";
    static final String SEARCH_IN_FILES = "SEARCH_IN_FILES";
    static final String SEARCH_SIMPLE = "SEARCH_SIMPLE";
    static final String RES_PERM = "android.permission.WRITE_EXTERNAL_STORAGE";

    static void Log(String message) {
        Log.e("atomofiron", message);
    }

    static void Toast(Context context, String message, int time) {
        Toast.makeText(context,message,time).show();
    }
    static void Snack(View fab, String message, boolean lengthLong) {
        Snackbar.make(fab, message, lengthLong?Snackbar.LENGTH_LONG:Snackbar.LENGTH_SHORT).show();
    }

    static Boolean granted(Context context, String permission) {
        return (context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
    }

    static boolean isTxtFile(String format) {
        format = format.toLowerCase();
        switch (format) {
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
        return false;
    }

    static void sleep(int sec) { try { Thread.sleep(sec * 1000); } catch (Exception ignored) {} }
}
