package ru.atomofiron.regextool.model

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

    val data: Int get() {
        var flags = red * BYTE * BYTE + green * BYTE + blue
        if (invForDark) {
            flags += INV_FOR_DARK_MASK
        }
        if (invGlowing) {
            flags += INV_GLOWING_MASK
        }
        return flags
    }

    fun color(isDark: Boolean): Int = when (isDark && invForDark) {
        true -> Color.argb(FF, FF - red, FF - green, FF - blue)
        else -> Color.argb(FF, red, green, blue)
    }

    fun glow(isDark: Boolean): Int = when ((isDark && invForDark) xor invGlowing) {
        true -> Color.argb(FF, FF - red, FF - green, FF - blue)
        else -> Color.argb(FF, red, green, blue)
    }
}