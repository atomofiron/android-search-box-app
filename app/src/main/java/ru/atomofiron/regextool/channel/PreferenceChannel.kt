package ru.atomofiron.regextool.channel

import app.atomofiron.common.util.KObservable

class PreferenceChannel {
    val historyImportedEvent = KObservable(Unit, single = true)
}