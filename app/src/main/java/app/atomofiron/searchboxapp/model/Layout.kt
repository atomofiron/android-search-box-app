package app.atomofiron.searchboxapp.model

@JvmInline
value class Layout private constructor(val value: Int = 0) {
    enum class Ground(val isBottom: Boolean = false) {
        Left, Bottom(true), Right,
    }
    companion object {
        private const val JOYSTICK =    0b001
        private const val GROUND_MASK = 0b110
        private const val LEFT =        0b010
        private const val RIGHT =       0b100
        private const val BOTTOM =      0b110

        private fun get(ground: Ground, withJoystick: Boolean): Int {
            var value = if (withJoystick) JOYSTICK else 0
            value = when (ground) {
                Ground.Left -> value or LEFT
                Ground.Right -> value or RIGHT
                Ground.Bottom -> value or BOTTOM
            }
            return value
        }
    }

    val withJoystick: Boolean get() = (value and JOYSTICK) == JOYSTICK
    val isLeft: Boolean get() = (value and GROUND_MASK) == LEFT
    val isRight: Boolean get() = (value and GROUND_MASK) == RIGHT
    val isBottom: Boolean get() = (value and GROUND_MASK) == BOTTOM
    val isWide: Boolean get() = !isBottom

    constructor(side: Ground, withJoystick: Boolean) : this(get(side, withJoystick))
}
