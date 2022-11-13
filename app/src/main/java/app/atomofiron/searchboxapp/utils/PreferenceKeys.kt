package app.atomofiron.searchboxapp.utils

import androidx.datastore.preferences.core.*

object PreferenceKeys {
    val KEY_STORAGE_PATH = stringPreferencesKey("pref_storage_path")
    val KEY_OPENED_DIR_PATH = stringPreferencesKey("pref_opened_dir_path")
    val KEY_DOCK_GRAVITY = intPreferencesKey("pref_drawer_gravity")
    val KEY_SPECIAL_CHARACTERS = stringPreferencesKey("pref_special_characters")
    val KEY_TEXT_FORMATS = stringPreferencesKey("pref_text_formats")
    val KEY_APP_ORIENTATION = stringPreferencesKey("pref_app_orientation")
    val KEY_APP_THEME = stringPreferencesKey("pref_app_theme")
    val KEY_DEEP_BLACK = booleanPreferencesKey("pref_deep_black")
    val KEY_MAX_SIZE = longPreferencesKey("pref_max_size")
    val KEY_MAX_DEPTH = intPreferencesKey("pref_max_depth")
    val KEY_EXCLUDE_DIRS = booleanPreferencesKey("pref_exclude_dirs")
    val KEY_USE_SU = booleanPreferencesKey("pref_use_su")
    val KEY_EXPLORER_ITEM = intPreferencesKey("pref_explorer_item")
    val KEY_JOYSTICK = intPreferencesKey("pref_joystick")
    val KEY_TOYBOX = stringSetPreferencesKey("pref_toybox")

    const val PREF_ABOUT = "pref_about"
    const val PREF_EXPORT_IMPORT = "pref_export_import"
    const val PREF_LEAK_CANARY = "pref_leak_canary"
    const val PREF_CATEGORY_DEBUG = "pref_category_debug"
}