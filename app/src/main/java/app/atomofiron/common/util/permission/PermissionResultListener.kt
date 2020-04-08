package app.atomofiron.common.util.permission

interface PermissionResultListener {
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
}