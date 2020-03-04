package app.atomofiron.common.util.permission

internal class GrantedImpl(
        private val callback: Callback?,
        private val denied: Permissions.Denied
) : Permissions.Granted {
    override infix fun denied(action: (String) -> Unit): Permissions.Denied {
        callback?.setDenied(action)
        return denied
    }

    override infix fun forbidden(action: (String) -> Unit): Unit? = callback?.setForbidden(action)

    interface Callback {
        fun setDenied(action: (String) -> Unit)
        fun setForbidden(action: (String) -> Unit)
    }
}
