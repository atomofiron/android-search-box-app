package app.atomofiron.common.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import app.atomofiron.common.util.KObservable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.log2

abstract class BaseViewModel<R : BaseRouter>(app: Application) : AndroidViewModel(app) {
    protected abstract val router: R
    private val screenScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Main.immediate)

    protected val app: App get() = getApplication()

    val onClearedCallback = KObservable.RemoveObserverCallback()

    init {
        log2("init")
        buildComponentAndInject()
    }

    protected open fun buildComponentAndInject() = Unit

    open fun onFragmentAttach(fragment: Fragment) = router.onFragmentAttach(fragment)

    open fun onActivityAttach(activity: Activity) = router.onActivityAttach(activity as AppCompatActivity)

    fun onCreate(context: Context, arguments: Bundle?) {
        onCreate(context, Intent().putExtras(arguments ?: Bundle()))
    }

    open fun onCreate(context: Context, intent: Intent) = Unit

    open fun onVisibleChanged(visible: Boolean) = Unit

    open fun onViewDestroy() = router.onViewDestroy()

    override fun onCleared() {
        screenScope.cancel()
        onClearedCallback.invoke()
    }

    fun onAttachChildFragment(childFragment: Fragment) = router.onAttachChildFragment(childFragment)

    open fun onBackButtonClick(): Boolean = router.onBack()

    open fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = Unit
}
