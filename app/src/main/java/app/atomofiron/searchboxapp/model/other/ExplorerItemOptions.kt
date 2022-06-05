package app.atomofiron.searchboxapp.model.other

import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition

data class ExplorerItemOptions constructor(
    val ids: List<Int>,
    val items: List<XFile>,
    val composition: ExplorerItemComposition,
)