package app.atomofiron.searchboxapp.utils

import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min

object Tool {

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
}