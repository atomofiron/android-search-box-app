package ru.atomofiron.regextool.common.util.permission

internal class DeniedImpl(private val callback: Callback?) : Permissions.Denied {
    override infix fun forbidden(action: (String) -> Unit) = callback?.setForbidden(action)

    interface Callback {
        fun setForbidden(action: (String) -> Unit)
    }
}
