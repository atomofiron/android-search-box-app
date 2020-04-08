package app.atomofiron.common.base

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import app.atomofiron.common.util.permission.PermissionResultListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.log2

abstract class BasePresenter<M : BaseViewModel<BaseRouter>, R : BaseRouter>(
        protected val viewModel: M,
        protected val scope: CoroutineScope,
        private val permissionResultListener: PermissionResultListener
) : PermissionResultListener {
    protected val context: Context get() = viewModel.getApplication<App>().applicationContext
    protected abstract val router: R

    protected val onClearedCallback = viewModel.onClearedCallback

    init {
        log2("init")

        viewModel.onClearedCallback.addOneTimeObserver {
            log2("onCleared")
            onCleared()
        }
    }

    fun onCreate(context: Context, arguments: Bundle?) = Unit

    open fun onSubscribeData() = Unit

    open fun onVisibleChanged(visible: Boolean) = Unit

    open fun onViewDestroy() = router.onViewDestroy()

    protected open fun onCleared() {
        scope.cancel("${this.javaClass.simpleName}.onCleared()")
    }

    fun onAttachChildFragment(childFragment: Fragment) = router.onAttachChildFragment(childFragment)

    open fun onBackButtonClick(): Boolean = router.onBack()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionResultListener.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
