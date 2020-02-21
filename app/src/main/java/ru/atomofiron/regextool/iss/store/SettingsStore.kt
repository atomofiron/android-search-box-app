package ru.atomofiron.regextool.iss.store

import android.view.Gravity
import ru.atomofiron.regextool.model.AppOrientation
import ru.atomofiron.regextool.model.AppTheme
import ru.atomofiron.regextool.utils.Const

object SettingsStore {
    val useSu = PreferenceStore.forBoolean<Boolean>(
            key = Const.PREF_USE_SU,
            default = false
    )

    val storagePath = PreferenceStore.forString<String>(
            key = Const.PREF_STORAGE_PATH,
            default = Const.ROOT
    )

    val openedDirPath = PreferenceStore.forNullableString<String>(
            key = Const.PREF_OPENED_DIR_PATH,
            default = null
    )

    val dockGravity = PreferenceStore.forInt<Int>(
            key = Const.PREF_DOCK_GRAVITY,
            default = Gravity.START
    )

    val specialCharacters = PreferenceStore.forString<String>(
            key = Const.PREF_SPECIAL_CHARACTERS,
            default = Const.DEFAULT_SPECIAL_CHARACTERS
    )

    val extraFormats = PreferenceStore.forString<String>(
            key = Const.PREF_EXTRA_FORMATS,
            default = Const.DEFAULT_EXTRA_FORMATS
    )

    val maxFileSizeForSearch = PreferenceStore.forInt<Int>(
            key = Const.PREF_MAX_SIZE,
            default = Const.DEFAULT_MAX_SIZE
    )

    val appTheme = PreferenceStore.forInt(
            key = Const.PREF_APP_THEME,
            default = AppTheme.WHITE.ordinal,
            toValue = { it.ordinal },
            fromValue = { AppTheme.values()[it] }
    )

    val appOrientation = PreferenceStore.forInt(
            Const.PREF_APP_ORIENTATION,
            AppOrientation.UNDEFINED.ordinal,
            toValue = { it.ordinal },
            fromValue = { AppOrientation.values()[it] }
    )
}