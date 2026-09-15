package app.atomofiron.searchboxapp.model.textviewer

data class Reading(
    val loaded: Int,
    val length: Int,
    val denominator: Int,
) {
    companion object {
        val Stub = Reading(0, 0, 1)
    }
}
