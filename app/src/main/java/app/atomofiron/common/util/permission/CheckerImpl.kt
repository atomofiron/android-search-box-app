package app.atomofiron.common.util.permission

internal class CheckerImpl(
        private val callback: Callback,
        private val granted: Permissions.Granted
) : Permissions.Checker {
    private var used = false

    @Throws(Grabber.AlreadyDefinedException::class)
    override infix fun granted(action: () -> Unit): Permissions.Granted {
        if (used) {
            throw Exception()
        }
        used = true
        callback.setGranted(action)
        return granted
    }

    interface Callback {
        @Throws(Grabber.AlreadyDefinedException::class)
        fun setGranted(action: () -> Unit)
    }
}
