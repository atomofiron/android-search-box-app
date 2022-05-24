package app.atomofiron.common.util.permission

interface PermissionCallback

fun interface GrantedCallback : PermissionCallback {
    operator fun invoke(permission: String)
}

fun interface DeniedCallback : PermissionCallback {
    operator fun invoke(permission: String, shouldShowRequestPermissionRationale: Boolean)
}

fun interface ExactGrantedCallback : PermissionCallback {
    operator fun invoke()
}

fun interface ExactDeniedCallback : PermissionCallback {
    operator fun invoke(shouldShowRequestPermissionRationale: Boolean)
}