package ru.atomofiron.regextool.model

data class ExplorerItemComposition(
        val visibleAccess: Boolean,
        val visibleOwner: Boolean,
        val visibleGroup: Boolean,
        val visibleDate: Boolean,
        val visibleTime: Boolean
) {
    companion object {
        private const val ACCESS = 0b10000
        private const val OWNER = 0b01000
        private const val GROUP = 0b00100
        private const val DATE = 0b00010
        private const val TIME = 0b00001
    }

    constructor(flags: Int) : this(
            flags and ACCESS == ACCESS,
            flags and OWNER == OWNER,
            flags and GROUP == GROUP,
            flags and DATE == DATE,
            flags and TIME == TIME
    )

    val flags: Int get() {
        var flags = 0
        if (visibleAccess) {
            flags += ACCESS
        }
        if (visibleOwner) {
            flags += OWNER
        }
        if (visibleGroup) {
            flags += GROUP
        }
        if (visibleDate) {
            flags += DATE
        }
        if (visibleTime) {
            flags += TIME
        }
        return flags
    }
}