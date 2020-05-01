package ru.atomofiron.regextool.utils

object Const {
    const val ROOT = "/"
    const val SLASH = "/"
    const val SDCARD = "/sdcard/"
    const val ANDROID_DIR = "/Android/"
    const val PREF_STORAGE_PATH = "pref_storage_path"
    const val PREF_OPENED_DIR_PATH = "pref_opened_dir_path"
    const val PREF_DOCK_GRAVITY = "pref_drawer_gravity"
    const val PREF_SPECIAL_CHARACTERS = "pref_special_characters"
    const val PREF_TEXT_FORMATS = "pref_text_formats"
    const val PREF_APP_ORIENTATION = "pref_app_orientation"
    const val PREF_APP_THEME = "pref_app_theme"
    const val PREF_MAX_SIZE = "pref_max_size"
    const val PREF_MAX_DEPTH = "pref_max_depth"
    const val PREF_EXCLUDE_DIRS = "pref_exclude_dirs"
    const val PREF_USE_SU = "pref_use_su"
    const val PREF_CURRENT_DIR = "pref_current_dir"
    const val PREF_EXPORT_IMPORT = "pref_export_import"
    const val PREF_EXPLORER_ITEM = "pref_explorer_item"
    const val PREF_JOYSTICK = "pref_joystick"
    const val PREF_LEAK_CANARY = "pref_leak_canary"

    const val DEFAULT_TEXT_FORMATS = "txt java xml html htm smali log js css json kt md mkd markdown cm ad adoc"
    const val DEFAULT_SPECIAL_CHARACTERS = "\\ [ { ? + * ^ $"
    const val DEFAULT_MAX_SIZE = 10485760L
    const val DEFAULT_MAX_DEPTH = 1024
    const val DEFAULT_EXPLORER_ITEM = 2 * 2 * 2 * 2 * 2 * 2 * 2 * 2 - 1
    const val DEFAULT_JOYSTICK = 16732754 // 0ff5252

    const val FOREGROUND_NOTIFICATION_CHANNEL_ID = "foreground_channel_id"
    const val FOREGROUND_NOTIFICATION_ID = 101
    const val FOREGROUND_INTENT_REQUEST_CODE = 102

    const val DATE_PATTERN = "YYYY-MM-DD_HH-mm-ss"
    const val MIME_TYPE_TEXT = "text/plain"
}