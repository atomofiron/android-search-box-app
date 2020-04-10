package app.atomofiron.common.arch

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty

abstract class BaseRouter(
        private val fragmentProperty: WeakProperty<out Fragment>? = null,
        private val activityProperty: WeakProperty<out AppCompatActivity>? = null
) : app.atomofiron.common.base.BaseRouter() {
    override var fragment: Fragment? = null
        get() = fragmentProperty?.value
    override var activity: AppCompatActivity? = null
        get() = activityProperty?.value ?: fragmentProperty?.value?.requireActivity() as AppCompatActivity

    init {
        require(fragmentProperty != null || activityProperty != null) { NullPointerException() }
    }
}