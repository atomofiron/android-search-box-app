package app.atomofiron.common.arch.view

interface Backable {
    /** @return event was consumed */
    fun onBack(): Boolean = false
}