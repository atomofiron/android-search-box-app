package ru.atomofiron.regextool.model

import android.content.pm.ActivityInfo

enum class AppOrientation(val constant: Int) {
    UNDEFINED(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED),
    PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
    LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
}