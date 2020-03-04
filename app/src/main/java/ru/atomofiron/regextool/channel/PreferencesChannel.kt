package ru.atomofiron.regextool.channel

import app.atomofiron.common.util.KObservable

object PreferencesChannel {
    val historyImportedEvent = KObservable(Unit, single = true)
}