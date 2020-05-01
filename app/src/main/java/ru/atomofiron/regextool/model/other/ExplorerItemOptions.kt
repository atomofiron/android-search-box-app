package ru.atomofiron.regextool.model.other

import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition

data class ExplorerItemOptions(
        val ids: List<Int>,
        val items: List<XFile>,
        val composition: ExplorerItemComposition
)