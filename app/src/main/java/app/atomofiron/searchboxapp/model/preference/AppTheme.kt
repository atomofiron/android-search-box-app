package app.atomofiron.searchboxapp.model.preference

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import java.util.*

sealed class AppTheme(
    val name: String,
) {
    open val deepBlack: Boolean = false

    class System(override val deepBlack: Boolean = false) : AppTheme(NAME_SYSTEM) {
        override fun copy(deepBlack: Boolean): AppTheme = System(deepBlack)
    }
    object Light : AppTheme(NAME_LIGHT) {
        override fun copy(deepBlack: Boolean): AppTheme = this
    }
    class Dark(override val deepBlack: Boolean = false) : AppTheme(NAME_DARK) {
        override fun copy(deepBlack: Boolean): AppTheme = Dark(deepBlack)
    }

    abstract fun copy(deepBlack: Boolean): AppTheme

    override fun equals(other: Any?) = when {
        other == null -> false
        other !is AppTheme -> false
        other::class != this::class -> false
        else -> other.deepBlack == deepBlack
    }

    override fun hashCode(): Int = Objects.hash(this::class, deepBlack)

    companion object {

        private const val NAME_SYSTEM = "system"
        private const val NAME_LIGHT = "light"
        private const val NAME_DARK = "dark"

        fun defaultName(): String = when {
            SDK_INT >= Q -> NAME_SYSTEM
            else -> NAME_LIGHT
        }

        fun fromString(name: String?, deepBlack: Boolean = false) = when (name) {
            NAME_SYSTEM -> System(deepBlack)
            NAME_LIGHT -> Light
            NAME_DARK -> Dark(deepBlack)
            else -> System(deepBlack)
        }
    }
}