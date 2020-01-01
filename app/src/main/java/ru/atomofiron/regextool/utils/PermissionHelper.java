package ru.atomofiron.regextool.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.fragment.app.Fragment;

public class PermissionHelper {
	public static final String PERMISSION = "android.permission.READ_EXTERNAL_STORAGE";

	public static Boolean granted(Context context, String permission) {
		return (context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
	}

	public static boolean checkPerm(Fragment fragment, int requestCode) {
		if (granted(fragment.getContext(), PERMISSION))
			return true;

		if (Build.VERSION.SDK_INT >= 23)
			fragment.requestPermissions(new String[]{ PERMISSION }, requestCode);

		return false;
	}
}
