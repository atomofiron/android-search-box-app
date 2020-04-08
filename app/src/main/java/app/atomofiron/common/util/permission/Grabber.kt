package app.atomofiron.common.util.permission

class Grabber(
    private val permission: String,
    private val isGranted: Boolean,
    private val isForbidden: Boolean
) : CheckerImpl.Callback, GrantedImpl.Callback, DeniedImpl.Callback {
    private var granted: (() -> Unit)? = null
    private var denied: ((String) -> Unit)? = null
    private var forbidden: ((String) -> Unit)? = null

    @Throws(AlreadyDefinedException::class)
    override fun setGranted(action: () -> Unit) {
        when {
            granted != null -> throw AlreadyDefinedException()
            isGranted -> action()
            else -> granted = action
        }
    }

    @Throws(AlreadyDefinedException::class)
    override fun setDenied(action: (String) -> Unit) {
        granted = granted ?: { }
        when {
            isGranted -> return
            denied != null -> throw AlreadyDefinedException()
            else -> denied = action
        }
    }

    @Throws(AlreadyDefinedException::class)
    override fun setForbidden(action: (String) -> Unit) {
        granted = granted ?: { }
        denied = denied ?: { }
        when {
            isGranted -> return
            forbidden != null -> throw AlreadyDefinedException()
            isForbidden -> action(permission)
            else -> forbidden = action
        }
    }

    fun onGranted() = granted?.invoke()

    fun onDenied(permission: String) = denied?.invoke(permission)

    fun onForbidden(permission: String) = forbidden?.invoke(permission)

    class AlreadyDefinedException : Exception()
}