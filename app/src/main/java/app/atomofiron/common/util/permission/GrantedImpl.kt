package app.atomofiron.common.util.permission

internal class GrantedImpl(
        private val callback: Callback?,
        private val denied: Permissions.Denied
) : Permissions.Granted {
    private var used = false

    @Throws(Grabber.AlreadyDefinedException::class)
    override infix fun denied(action: (String) -> Unit): Permissions.Denied {
        if (used) {
            throw Exception()
        }
        used = true
        callback?.setDenied(action)
        return denied
    }

    @Throws(Grabber.AlreadyDefinedException::class)
    override infix fun forbidden(action: (String) -> Unit): Unit? = callback?.setForbidden(action)

    interface Callback {
        @Throws(Grabber.AlreadyDefinedException::class)
        fun setDenied(action: (String) -> Unit)
        @Throws(Grabber.AlreadyDefinedException::class)
        fun setForbidden(action: (String) -> Unit)
    }
}
