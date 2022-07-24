package app.atomofiron.searchboxapp

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import app.atomofiron.searchboxapp.utils.Const
import java.util.*
import kotlin.collections.ArrayList
import com.yandex.metrica.YandexMetrica

private var timestamp: Long = 0
private var nanotimestamp: Long = 0

private const val mute = false
private const val delay = false

fun Any.sleep(t: Long) = if (delay) Thread.sleep(t) else Unit

fun Any.logE(s: String) {
    if (!BuildConfig.DEBUG) {
        YandexMetrica.reportError(s, null)
    }
    Log.e("searchboxapp", "[ERROR] [${this.javaClass.simpleName}] $s")
}

fun Any.poop(s: String) = poop(this.javaClass.simpleName, s)

fun Any.poop(context: Any, s: String) = poop(context.javaClass.simpleName, s)

fun Any.poop(label: String, s: String) {
    Log.e("searchboxapp", "[$label] $s")
}

fun Any.logI(s: String) {
    if (mute) return

    Log.d("searchboxapp", "[${this.javaClass.simpleName}] $s")
}

fun Any.tik(s: String) {
    if (mute) return

    val now = System.currentTimeMillis()
    val dif = now - timestamp
    timestamp = now
    Log.e("searchboxapp", "[${this.javaClass.simpleName}] $dif $s")
}

fun Any.natik(s: String) {
    if (mute) return

    val now = System.nanoTime()
    val dif = now - nanotimestamp
    nanotimestamp = now
    Log.e("searchboxapp", "[${this.javaClass.simpleName}] $dif $s")
}

fun Resources.getLocale(): Locale = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> configuration.locales.get(0)
    else -> configuration.locale
}

fun Context.findResIdByAttr(@AttrRes attr: Int): Int = findResIdsByAttr(attr)[0]

fun Context.findResIdsByAttr(@AttrRes vararg attrs: Int): IntArray {
    @SuppressLint("ResourceType")
    val array = obtainStyledAttributes(attrs)

    val values = IntArray(attrs.size)
    for (i in attrs.indices) {
        values[i] = array.getResourceId(i, 0)
    }
    array.recycle()

    return values
}

fun Context.getColorByAttr(@AttrRes attr: Int): Int = ContextCompat.getColor(this, findResIdByAttr(attr))

fun Context.getAttr(attr: Int, fallbackAttr: Int): Int {
    val value = TypedValue()
    theme.resolveAttribute(attr, value, true)
    return when {
        value.resourceId != 0 -> attr
        else -> fallbackAttr
    }
}

fun Context.getPackageUri(): Uri = Uri.parse("package:$packageName")

fun Context.getMarketUrl(): String = "https://play.google.com/store/apps/details?id=$packageName"

fun <T> List<T>.subListLastReversedWhile(predicate: (T) -> Boolean): List<T> {
    val list = ArrayList<T>()
    loop@ for (i in 0 until size) {
        val item = this[i]
        when {
            predicate(item) -> list.add(item)
            else -> break@loop
        }
    }
    return list
}

fun <I> ActivityResultLauncher<I>.resolve(context: Context, input: I?): Boolean {
    val intent = contract.createIntent(context, input)
    val info = intent.resolveActivity(context.packageManager)
    return info != null
}
