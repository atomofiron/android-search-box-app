package ru.atomofiron.regextool.utils

import android.content.Context
import android.preference.PreferenceManager

fun Context.sp() = PreferenceManager.getDefaultSharedPreferences(this)!!