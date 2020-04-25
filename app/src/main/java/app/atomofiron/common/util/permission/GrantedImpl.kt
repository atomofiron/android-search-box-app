package app.atomofiron.common.util.permission

internal class GrantedImpl(
        private val grabber: Callback?,
        private val denied: Permissions.Denied
) : Permissions.Granted {

    @Throws(Grabber.AlreadyDefinedException::class)
    override infix fun denied(action: (String) -> Unit): Permissions.Denied {
        grabber?.setDenied(action)
        return denied
    }

    @Throws(Grabber.AlreadyDefinedException::class)
    override infix fun forbidden(action: (String) -> Unit): Unit? = grabber?.setForbidden(action)

    interface Callback {
        @Throws(Grabber.AlreadyDefinedException::class)
        fun setDenied(action: (String) -> Unit)
        @Throws(Grabber.AlreadyDefinedException::class)
        fun setForbidden(action: (String) -> Unit)
    }
}
