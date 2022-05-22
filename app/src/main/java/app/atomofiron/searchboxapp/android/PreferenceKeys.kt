package app.atomofiron.searchboxapp.android

import app.atomofiron.common.util.PreferenceKey

object PreferenceKeys {
    const val APP_UPDATE = "pref_app_update"

    val LAST_NOTIFICATION_CODE = PreferenceKey.int("PREF_LAST_NOTIFICATION_CODE")
    val APP_THEME = PreferenceKey.string("pref_app_theme")
    val APP_LOCALE = PreferenceKey.string("pref_app_locale")
    val RED_DOT_VERSION = PreferenceKey.int("pref_red_dot_version")
    val RED_DOT_REASONS = PreferenceKey.stringSet("pref_red_dot_reasons")
}