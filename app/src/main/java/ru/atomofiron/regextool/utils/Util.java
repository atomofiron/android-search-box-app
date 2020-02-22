package ru.atomofiron.regextool.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Collection;
import java.util.Locale;

public class Util {

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
            case "log":
            case "js":
            case "css":
            case "json":
            case "kt":
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

    public static boolean equivalent(Collection a, Collection b) {
        if (a.size() != b.size())
            return false;

        for (Object o : a)
            if (!b.contains(o))
                return false;

        return true;
    }

    public static String intToHumanReadable(final int bytes, String[] suffixes) {
    	int order = 0;
    	float byteCount = (float) bytes;
    	while (byteCount >= 970) {
    		byteCount /= 1024;
            order++;
		}
		return String.format(Locale.US, "%1$.2f %2$s", byteCount, suffixes[order])
				.replaceAll("[.,]00|(?<=[.,][0-9])0", "");
    }
}
