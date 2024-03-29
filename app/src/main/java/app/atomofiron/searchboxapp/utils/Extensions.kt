package app.atomofiron.searchboxapp.utils

import android.app.PendingIntent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import androidx.core.graphics.ColorUtils
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainPresenterParams
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.math.min

fun String.escapeQuotes(): String = this.replace(Const.QUOTE, "\\" + Const.QUOTE)

private fun String.getExt(): String {
    var index = lastIndexOf('/')
    index = kotlin.math.max(0, index)
    index = lastIndexOf('.', index)
    return substring(index.inc()).lowercase()
}

fun Number.toHumanReadable(suffixes: Array<String?>): String {
    var order = 0
    var byteCount = toDouble()
    while (byteCount >= 970) {
        byteCount /= 1024f
        order++
    }
    return String.format(Locale.US, "%1$.2f %2\$s", byteCount, suffixes[order]).replace("[.,]00|(?<=[.,][0-9])0".toRegex(), "")
}

fun String.endingDot(): String = "${this}."

fun InputStream.writeTo(stream: OutputStream): Boolean {
    var remaining = available()
    val bytes = ByteArray(1024)
    while (remaining > 0) {
        val length = min(bytes.size, remaining)
        val read = read(bytes, 0, length)
        if (read < 0) break
        stream.write(bytes, 0, length)
        remaining -= read
    }
    return remaining == 0
}

fun Int.convert(suffixes: Array<String>, lossless: Boolean = true): String = toLong().convert(suffixes, lossless)

fun Long.convert(suffixes: Array<String>, lossless: Boolean = true, separator: String = ""): String {
    var value = this
    for (i in suffixes.indices) {
        if (value / 1024 == 0L) return "$value$separator${suffixes[i]}"
        if (lossless && value % 1024 != 0L) return "$value${suffixes[i]}"
        if (i < suffixes.lastIndex) value /= 1024
    }
    return "$value$separator${suffixes.last()}"
}

fun String.convert(): Int {
    val digits = Regex("\\d+|0")
    val metrics = Regex("([gGгГ]|[mMмМ]|[kKкК])?[bBбБ]")
    var value = digits.find(this)?.value?.toFloat()
    value ?: return 0
    val rate = metrics.find(this)?.value
    rate ?: return 0
    value *= when (rate.first()) {
        'g', 'G', 'г', 'Г' -> 1024 * 1024 * 1024
        'm', 'M', 'м', 'М' -> 1024 * 1024
        'k', 'K', 'к', 'К' -> 1024
        else -> 1
    }
    return value.toInt()
}

fun BaseRouter.showCurtain(recipient: String, layoutId: Int) {
    navigation {
        val args = CurtainPresenterParams.args(recipient, layoutId)
        navigate(R.id.curtainFragment, args, BaseRouter.curtainOptions)
    }
}

fun Int.immutable(): Int = when {
    SDK_INT >= M -> this or PendingIntent.FLAG_IMMUTABLE
    else -> this
}

inline fun <E> Iterable<E>.findIndexed(predicate: (E) -> Boolean): Pair<Int, E?> {
    for ((index, item) in this.withIndex()) {
        if (predicate(item)) return index to item
    }
    return -1 to null
}

fun getStateMut(
    enabled: Boolean? = null,
    checked: Boolean? = null,
    activated: Boolean? = null,
): MutableList<Int> {
    val flags = mutableListOf<Int>()
    enabled?.let { flags.add(android.R.attr.state_enabled * it.toInt()) }
    checked?.let { flags.add(android.R.attr.state_checked * it.toInt()) }
    activated?.let { flags.add(android.R.attr.state_activated * it.toInt()) }
    return flags
}

fun Boolean.toInt(): Int = if (this) 1 else -1

fun <E> MutableList<E>.removeOneIf(predicate: (E) -> Boolean): E? {
    val each = listIterator()
    while (each.hasNext()) {
        val item = each.next()
        if (predicate(item)) {
            each.remove()
            return item
        }
    }
    return null
}

fun Float.toIntAlpha(): Int = (Const.ALPHA_VISIBLE_INT * this).toInt().coerceIn(0, 255)

fun Int.setColorAlpha(alpha: Float): Int = setColorAlpha(alpha.toIntAlpha())

fun Int.setColorAlpha(alpha: Int): Int = ColorUtils.setAlphaComponent(this, alpha)

fun Int.asOverlayOn(background: Int): Int = ColorUtils.compositeColors(this, background)
