package ru.atomofiron.regextool.model

data class ExplorerItemComposition(
        val visibleAccess: Boolean,
        val visibleOwner: Boolean,
        val visibleGroup: Boolean,
        val visibleDate: Boolean,
        val visibleTime: Boolean,
        val visibleSize: Boolean,
        val visibleBox: Boolean
) {
    companion object {
        private const val ACCESS = 0b1000000
        private const val OWNER = 0b0100000
        private const val GROUP = 0b0010000
        private const val DATE = 0b0001000
        private const val TIME = 0b0000100
        private const val SIZE = 0b0000010
        private const val BOX = 0b0000001
    }

    constructor(flags: Int) : this(
            flags and ACCESS == ACCESS,
            flags and OWNER == OWNER,
            flags and GROUP == GROUP,
            flags and DATE == DATE,
            flags and TIME == TIME,
            flags and SIZE == SIZE,
            flags and BOX == BOX
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
        if (visibleSize) {
            flags += SIZE
        }
        if (visibleBox) {
            flags += BOX
        }
        return flags
    }
}