package ru.atomofiron.regextool.common.util.permission

class Grabber(
    private val permission: String,
    private val isGranted: Boolean,
    private val isForbidden: Boolean
) : CheckerImpl.Callback, GrantedImpl.Callback, DeniedImpl.Callback {
    private var granted: (() -> Unit)? = null
    private var denied: ((String) -> Unit)? = null
    private var forbidden: ((String) -> Unit)? = null

    override fun setGranted(action: () -> Unit) {
        if (isGranted)
            action()
        else
            granted = action
    }

    override fun setDenied(action: (String) -> Unit) {
        denied = action
    }

    override fun setForbidden(action: (String) -> Unit) {
        if (isForbidden)
            action(permission)
        else
            forbidden = action
    }

    fun onGranted() = granted?.invoke()

    fun onDenied(permission: String) = denied?.invoke(permission)

    fun onForbidden(permission: String) = forbidden?.invoke(permission)
}