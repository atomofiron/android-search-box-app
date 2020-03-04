package ru.atomofiron.regextool.common.util.permission

internal class GrantedImpl(
        private val callback: Callback?,
        private val denied: Permissions.Denied
) : Permissions.Granted {
    override infix fun denied(action: (String) -> Unit): Permissions.Denied {
        callback?.setDenied(action)
        return denied
    }

    interface Callback {
        fun setDenied(action: (String) -> Unit)
    }
}
