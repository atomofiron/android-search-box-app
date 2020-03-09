package app.atomofiron.common.base

interface Backable {
    /** @return event was consumed */
    fun onBack(): Boolean
}