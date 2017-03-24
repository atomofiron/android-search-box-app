package ru.atomofiron.regextool.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;

import ru.atomofiron.regextool.I;
import ru.atomofiron.regextool.MainActivity;
import ru.atomofiron.regextool.R;

public class Permissions {

	public static Boolean granted(Context context, String permission) {
		return (context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
	}

	public static boolean checkPerm(MainActivity ac, int code) {
		if (I.granted(ac, I.RES_PERM))
			return true;
		if (Build.VERSION.SDK_INT >= 23) {
			if (ac.shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE))
				new AlertDialog.Builder(ac)
						///
						.create().show();
			ac.requestPermissions(new String[]{ I.RES_PERM }, code);
		}
		else
			ac.Snack(R.string.storage_err);
		return false;
	}
}
