package app.atomofiron.common.util.permission

interface PermissionDelegateApi {
    fun check(vararg permissions: String): PermissionDelegateApi
    fun request(vararg permissions: String): PermissionDelegateApi
    fun granted(callback: GrantedCallback): PermissionDelegateApi
    fun denied(callback: DeniedCallback): PermissionDelegateApi
    fun granted(permission: String, callback: ExactGrantedCallback): PermissionDelegateApi
    fun denied(permission: String, callback: ExactDeniedCallback): PermissionDelegateApi
}
