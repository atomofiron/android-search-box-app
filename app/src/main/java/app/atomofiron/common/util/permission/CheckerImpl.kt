package app.atomofiron.common.util.permission

internal class CheckerImpl(
    private val grabber: Callback,
    private val granted: Permissions.Granted,
    private val denied: Permissions.Denied
) : Permissions.Checker {

    @Throws(Grabber.AlreadyDefinedException::class)
    override infix fun granted(action: () -> Unit): Permissions.Granted {
        grabber.setGranted(action)
        return granted
    }

    @Throws(Grabber.AlreadyDefinedException::class)
    override infix fun denied(action: (String) -> Unit): Permissions.Denied {
        grabber.setDenied(action)
        return denied
    }

    @Throws(Grabber.AlreadyDefinedException::class)
    override infix fun forbidden(action: (String) -> Unit): Unit? = grabber.setForbidden(action)

    interface Callback {
        @Throws(Grabber.AlreadyDefinedException::class)
        fun setGranted(action: () -> Unit)
        @Throws(Grabber.AlreadyDefinedException::class)
        fun setDenied(action: (String) -> Unit)
        @Throws(Grabber.AlreadyDefinedException::class)
        fun setForbidden(action: (String) -> Unit)
    }
}
