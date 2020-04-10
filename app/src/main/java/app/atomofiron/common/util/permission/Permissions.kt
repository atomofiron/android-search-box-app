package app.atomofiron.common.util.permission

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import ru.atomofiron.regextool.BuildConfig

@TargetApi(Build.VERSION_CODES.M)
open class Permissions private constructor(
    private val activity: Activity?,
    private val fragment: Fragment?,
    private val property: WeakProperty<out Fragment>?
) : PermissionResultListener {
    companion object {
        private const val PACKAGE_SCHEME = "package:"
        private const val MAX_REQUEST_CODE = 65536

        val settingsIntent: Intent
            get() {
                val packageUri = Uri.parse(PACKAGE_SCHEME + BuildConfig.APPLICATION_ID)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                return intent
            }

        fun checkSelfPermission(context: Context, permission: String): Boolean {
            return PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED
        }
    }
    private val map = HashMap<Int, Grabber>()
    private var nextRequestCode: Int = 0
        get() = field++ % MAX_REQUEST_CODE
        set(_) = Unit

    private val granted = ArrayList<String>()

    val context: Context get() = property?.value?.requireContext() ?: fragment?.requireContext() ?: activity!!

    constructor(activity: Activity) : this(activity, null, null)

    constructor(fragment: Fragment) : this(null, fragment, null)

    constructor(property: WeakProperty<out Fragment>) : this(null, null, property)

    protected constructor(helper: Permissions) : this(helper.activity, helper.fragment, helper.property)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val grabber = map.remove(requestCode)!!
        when {
            grantResults[0] == PermissionChecker.PERMISSION_GRANTED -> {
                granted.add(permissions[0])
                grabber.onGranted()
            }
            shouldShowRequestPermissionRationale(permissions[0]) -> grabber.onDenied(permissions[0])
            else -> grabber.onForbidden(permissions[0])
        }
    }

    fun checkPermission(context: Context, permission: String): Boolean {
        return PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED
    }

    private fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        var should = activity?.shouldShowRequestPermissionRationale(permission)
        should = should ?: fragment?.shouldShowRequestPermissionRationale(permission)
        return should ?: throw NullPointerException()
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        activity?.requestPermissions(arrayOf(permission), requestCode)
        fragment?.requestPermissions(arrayOf(permission), requestCode)
    }

    fun check(vararg permissions: String): Checker? {
        return null
    }

    fun check(permission: String): Checker = when {
        granted.contains(permission) || checkPermission(context, permission) -> {
            val grabber = Grabber(permission, isGranted = true, isForbidden = false)
            val denied = DeniedImpl(null)
            CheckerImpl(grabber, GrantedImpl(null, denied), denied)
        }
        !shouldShowRequestPermissionRationale(permission) -> {
            val grabber = Grabber(permission, isGranted = false, isForbidden = false)
            val requestCode = nextRequestCode
            map[requestCode] = grabber
            requestPermission(permission, requestCode)
            val denied = DeniedImpl(grabber)
            CheckerImpl(grabber, GrantedImpl(grabber, denied), denied)
        }
        else -> {
            val grabber = Grabber(permission, isGranted = false, isForbidden = true)
            val denied = DeniedImpl(grabber)
            CheckerImpl(grabber, GrantedImpl(grabber, denied), denied)
        }
    }

    interface Checker {
        infix fun granted(action: () -> Unit): Granted
        infix fun denied(action: (permission: String) -> Unit): Denied
        infix fun forbidden(action: (permission: String) -> Unit): Unit?
    }

    interface Granted {
        infix fun denied(action: (permission: String) -> Unit): Denied
        infix fun forbidden(action: (permission: String) -> Unit): Unit?
    }

    interface Denied {
        infix fun forbidden(action: (permission: String) -> Unit): Unit?
    }
}