package app.atomofiron.searchboxapp.model.preference

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import java.util.*

sealed class AppTheme(val name: String) {

    object System : AppTheme(NAME_SYSTEM)
    object Light : AppTheme(NAME_LIGHT)
    data class Dark(val deepBlack: Boolean) : AppTheme(NAME_DARK) {
        override fun equals(other: Any?) = when (other) {
            !is Dark -> false
            else -> other.deepBlack == deepBlack
        }

        override fun hashCode(): Int = Objects.hash(this, deepBlack)
    }

    companion object {

        private const val NAME_SYSTEM = "system"
        private const val NAME_LIGHT = "light"
        private const val NAME_DARK = "dark"

        fun default(): AppTheme = when {
            SDK_INT >= Q -> System
            else -> Light
        }

        fun fromString(name: String?) = when (name) {
            NAME_SYSTEM -> System
            NAME_LIGHT -> Light
            NAME_DARK -> Dark(deepBlack = false)
            else -> System
        }
    }
}