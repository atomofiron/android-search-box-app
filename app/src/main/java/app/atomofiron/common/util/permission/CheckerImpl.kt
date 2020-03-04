package app.atomofiron.common.util.permission

internal class CheckerImpl(
        private val callback: Callback,
        private val granted: Permissions.Granted
) : Permissions.Checker {
    override infix fun granted(action: () -> Unit): Permissions.Granted {
        callback.setGranted(action)
        return granted
    }

    interface Callback {
        fun setGranted(action: () -> Unit)
    }
}
