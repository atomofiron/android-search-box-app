package ru.atomofiron.regextool.iss.store

import android.view.Gravity
import ru.atomofiron.regextool.iss.store.util.PreferenceNode
import ru.atomofiron.regextool.model.AppOrientation
import ru.atomofiron.regextool.model.AppTheme
import ru.atomofiron.regextool.utils.Const

object SettingsStore {
    fun getCurrentValue(key: String): Any? {
        return when (key) {
            Const.PREF_STORAGE_PATH -> storagePath.value
            Const.PREF_EXTRA_FORMATS -> extraFormats.value
            Const.PREF_SPECIAL_CHARACTERS -> specialCharacters.value
            Const.PREF_APP_THEME -> appTheme.value
            Const.PREF_APP_ORIENTATION -> appOrientation.value

            Const.PREF_MAX_SIZE -> maxFileSizeForSearch.value

            Const.PREF_USE_SU -> useSu.value
            else -> throw Exception("Key = $key.")
        }
    }

    val useSu = PreferenceNode.forBoolean<Boolean>(
            key = Const.PREF_USE_SU,
            default = false
    )

    val storagePath = PreferenceNode.forString<String>(
            key = Const.PREF_STORAGE_PATH,
            default = Const.ROOT
    )

    val openedDirPath = PreferenceNode.forNullableString<String?>(
            key = Const.PREF_OPENED_DIR_PATH,
            default = null
    )

    val dockGravity = PreferenceNode.forInt<Int>(
            key = Const.PREF_DOCK_GRAVITY,
            default = Gravity.START
    )

    val specialCharacters = PreferenceNode.forString(
            key = Const.PREF_SPECIAL_CHARACTERS,
            default = Const.DEFAULT_SPECIAL_CHARACTERS,
            toValue = { it.joinToString(separator = " ") },
            fromValue = { it.split(" ").toTypedArray() }
    )

    val extraFormats = PreferenceNode.forString(
            key = Const.PREF_EXTRA_FORMATS,
            default = Const.DEFAULT_EXTRA_FORMATS,
            toValue = { it.joinToString(separator = " ") },
            fromValue = { it.split(" ").toTypedArray() }
    )

    val maxFileSizeForSearch = PreferenceNode.forInt<Int>(
            key = Const.PREF_MAX_SIZE,
            default = Const.DEFAULT_MAX_SIZE
    )

    val appTheme = PreferenceNode.forString(
            key = Const.PREF_APP_THEME,
            default = AppTheme.WHITE.ordinal.toString(),
            toValue = { it.ordinal.toString() },
            fromValue = { AppTheme.values()[it.toInt()] }
    )

    val appOrientation = PreferenceNode.forString(
            Const.PREF_APP_ORIENTATION,
            AppOrientation.UNDEFINED.ordinal.toString(),
            toValue = { it.ordinal.toString() },
            fromValue = { AppOrientation.values()[it.toInt()] }
    )
}