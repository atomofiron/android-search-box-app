package ru.atomofiron.regextool

import android.util.Log

fun Any.log(s: String) {
    Log.e("estimoji", "[${this.javaClass.simpleName}] $s")
}
