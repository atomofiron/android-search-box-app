package ru.atomofiron.regextool.model.preference

import android.graphics.Color

data class JoystickComposition(
        val invForDark: Boolean,
        val invGlowing: Boolean,
        val red: Int,
        val green: Int,
        val blue: Int
) {
    companion object {
        private const val INV_FOR_DARK_MASK  = 0x01000000
        private const val INV_GLOWING_MASK = 0x02000000
        private const val BYTE = 256
        private const val FF = 255
    }

    constructor(flags: Int) : this(
            flags and INV_FOR_DARK_MASK == INV_FOR_DARK_MASK,
            flags and INV_GLOWING_MASK == INV_GLOWING_MASK,
            flags / BYTE / BYTE % BYTE,
            flags / BYTE % BYTE,
            flags % BYTE
    )

    val data: Int

    init {
        var data = red * BYTE * BYTE + green * BYTE + blue
        if (invForDark) {
            data += INV_FOR_DARK_MASK
        }
        if (invGlowing) {
            data += INV_GLOWING_MASK
        }
        this.data = data
    }

    fun color(isDark: Boolean): Int = when (isDark && invForDark) {
        true -> Color.argb(FF, FF - red, FF - green, FF - blue)
        else -> Color.argb(FF, red, green, blue)
    }

    fun glow(isDark: Boolean): Int = when ((isDark && invForDark) xor invGlowing) {
        true -> Color.argb(FF, FF - red, FF - green, FF - blue)
        else -> Color.argb(FF, red, green, blue)
    }

    fun text(): String {
        val builder = StringBuilder("#")
        val color = red * BYTE * BYTE + green * BYTE + blue
        val hex = Integer.toHexString(color)
        for (i in 0..(5 - hex.length)) {
            builder.append("0")
        }
        builder.append(hex)
        return builder.toString()
    }
}