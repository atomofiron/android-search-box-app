package ru.atomofiron.regextool.channel

import ru.atomofiron.regextool.common.util.KObservable

object PreferencesChannel {
    val historyImportedEvent = KObservable(Unit, single = true)
}