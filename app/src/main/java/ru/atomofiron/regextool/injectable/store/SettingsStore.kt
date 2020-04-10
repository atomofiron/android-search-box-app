package ru.atomofiron.regextool.injectable.store

import android.content.SharedPreferences
import android.view.Gravity
import leakcanary.AppWatcher
import ru.atomofiron.regextool.injectable.store.util.PreferenceNode
import ru.atomofiron.regextool.model.AppOrientation
import ru.atomofiron.regextool.model.AppTheme
import ru.atomofiron.regextool.model.ExplorerItemComposition
import ru.atomofiron.regextool.model.JoystickComposition
import ru.atomofiron.regextool.utils.Const

class SettingsStore(sp: SharedPreferences) {
    fun getCurrentValue(key: String): Any? {
        return when (key) {
            Const.PREF_STORAGE_PATH -> storagePath.value
            Const.PREF_EXTRA_FORMATS -> extraFormats.value
            Const.PREF_SPECIAL_CHARACTERS -> specialCharacters.value
            Const.PREF_APP_THEME -> appTheme.value
            Const.PREF_APP_ORIENTATION -> appOrientation.value

            Const.PREF_MAX_SIZE -> maxFileSizeForSearch.value

            Const.PREF_USE_SU -> useSu.value
            Const.PREF_LEAK_CANARY -> AppWatcher.config.enabled
            else -> throw Exception("Key = $key.")
        }
    }

    val useSu = PreferenceNode.forBoolean<Boolean>(
            sp,
            key = Const.PREF_USE_SU,
            default = false
    )

    val storagePath = PreferenceNode.forString<String>(
            sp,
            key = Const.PREF_STORAGE_PATH,
            default = Const.ROOT
    )

    val openedDirPath = PreferenceNode.forNullableString<String?>(
            sp,
            key = Const.PREF_OPENED_DIR_PATH,
            default = null
    )

    val dockGravity = PreferenceNode.forInt<Int>(
            sp,
            key = Const.PREF_DOCK_GRAVITY,
            default = Gravity.START
    )

    val specialCharacters = PreferenceNode.forString(
            sp,
            key = Const.PREF_SPECIAL_CHARACTERS,
            default = Const.DEFAULT_SPECIAL_CHARACTERS,
            toValue = { it.joinToString(separator = " ") },
            fromValue = { it.split(" ").toTypedArray() }
    )

    val extraFormats = PreferenceNode.forString(
            sp,
            key = Const.PREF_EXTRA_FORMATS,
            default = Const.DEFAULT_EXTRA_FORMATS,
            toValue = { it.joinToString(separator = " ") },
            fromValue = { it.split(" ").toTypedArray() }
    )

    val maxFileSizeForSearch = PreferenceNode.forInt<Int>(
            sp,
            key = Const.PREF_MAX_SIZE,
            default = Const.DEFAULT_MAX_SIZE
    )

    val appTheme = PreferenceNode.forString(
            sp,
            key = Const.PREF_APP_THEME,
            default = AppTheme.WHITE.ordinal.toString(),
            toValue = { it.ordinal.toString() },
            fromValue = { AppTheme.values()[it.toInt()] }
    )

    val appOrientation = PreferenceNode.forString(
            sp,
            key = Const.PREF_APP_ORIENTATION,
            default = AppOrientation.UNDEFINED.ordinal.toString(),
            toValue = { it.ordinal.toString() },
            fromValue = { AppOrientation.values()[it.toInt()] }
    )

    val explorerItem = PreferenceNode.forInt(
            sp,
            key = Const.PREF_EXPLORER_ITEM,
            default = Const.DEFAULT_EXPLORER_ITEM,
            toValue = { it.flags },
            fromValue = { ExplorerItemComposition(it) }
    )

    val escColor = PreferenceNode.forInt(
            sp,
            key = Const.PREF_ESC_COLOR,
            default = Const.DEFAULT_ESC_COLOR,
            toValue = { it.data },
            fromValue = { JoystickComposition(it) }
    )
}