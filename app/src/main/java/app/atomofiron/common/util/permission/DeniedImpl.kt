package app.atomofiron.common.util.permission

internal class DeniedImpl(private val callback: Callback?) : Permissions.Denied {
    private var used = false

    @Throws(Grabber.AlreadyDefinedException::class)
    override infix fun forbidden(action: (String) -> Unit) {
        if (used) {
            throw Exception()
        }
        used = true
        callback?.setForbidden(action)
    }

    interface Callback {
        @Throws(Grabber.AlreadyDefinedException::class)
        fun setForbidden(action: (String) -> Unit)
    }
}
