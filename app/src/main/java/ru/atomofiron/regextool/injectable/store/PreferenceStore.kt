package ru.atomofiron.regextool.injectable.store

import android.content.Context
import android.content.SharedPreferences
import android.view.Gravity
import leakcanary.AppWatcher
import ru.atomofiron.regextool.injectable.store.util.PreferenceNode
import ru.atomofiron.regextool.model.preference.*
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Tool

class PreferenceStore(context: Context, sp: SharedPreferences) {

    fun getCurrentValue(key: String): Any? {
        return when (key) {
            Const.PREF_STORAGE_PATH -> storagePath.value
            Const.PREF_TEXT_FORMATS -> textFormats.value
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
            default = Tool.getExternalStorageDirectory(context) ?: Const.ROOT
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

    val excludeDirs = PreferenceNode.forBoolean<Boolean>(
            sp,
            key = Const.PREF_EXCLUDE_DIRS,
            default = false
    )

    val textFormats = PreferenceNode.forString(
            sp,
            key = Const.PREF_TEXT_FORMATS,
            default = Const.DEFAULT_TEXT_FORMATS,
            toValue = { it.joinToString(separator = " ") },
            fromValue = { it.split(" ").toTypedArray() }
    )

    val maxFileSizeForSearch = PreferenceNode.forLong<Long>(
            sp,
            key = Const.PREF_MAX_SIZE,
            default = Const.DEFAULT_MAX_SIZE
    )

    val maxDepthForSearch = PreferenceNode.forInt<Int>(
            sp,
            key = Const.PREF_MAX_DEPTH,
            default = Const.DEFAULT_MAX_DEPTH
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

    val explorerItemComposition = PreferenceNode.forInt(
            sp,
            key = Const.PREF_EXPLORER_ITEM,
            default = Const.DEFAULT_EXPLORER_ITEM,
            toValue = { it.flags },
            fromValue = { ExplorerItemComposition(it) }
    )

    val joystickComposition = PreferenceNode.forInt(
            sp,
            key = Const.PREF_JOYSTICK,
            default = Const.DEFAULT_JOYSTICK,
            toValue = { it.data },
            fromValue = { JoystickComposition(it) }
    )

    val toyboxVariant = PreferenceNode.forSet(
            sp,
            key = Const.PREF_TOYBOX,
            default = setOf(Const.VALUE_TOYBOX_CUSTOM, Const.DEFAULT_TOYBOX_PATH),
            toValue = { throw Exception() },
            fromValue = { ToyboxVariant.fromSet(context, it) }
    )
}