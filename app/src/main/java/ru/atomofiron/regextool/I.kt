package ru.atomofiron.regextool

import android.util.Log

private var timestamp: Long = 0
private var nanotimestamp: Long = 0

private const val mute = true

fun Any.log(s: String) = log(this.javaClass.simpleName, s)

fun Any.log(label: String, s: String) {
    Log.e("regextool", "[$label] $s")
}

fun Any.log2(s: String) {
    if (mute) return

    Log.e("regextool", "[${this.javaClass.simpleName}] $s")
}

fun Any.tik(s: String) {
    if (mute) return

    val now = System.currentTimeMillis()
    val dif = now - timestamp
    timestamp = now
    Log.e("regextool", "[${this.javaClass.simpleName}] $dif $s")
}

fun Any.natik(s: String) {
    if (mute) return

    val now = System.nanoTime()
    val dif = now - nanotimestamp
    nanotimestamp = now
    Log.e("regextool", "[${this.javaClass.simpleName}] $dif $s")
}
