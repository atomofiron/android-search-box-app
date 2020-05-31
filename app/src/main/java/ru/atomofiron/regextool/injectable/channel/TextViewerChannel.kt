package ru.atomofiron.regextool.injectable.channel

import app.atomofiron.common.util.KObservable

class TextViewerChannel {
    val textFromFile = KObservable<List<String>>()
    val textFromFileLoading = KObservable<Boolean>()
}