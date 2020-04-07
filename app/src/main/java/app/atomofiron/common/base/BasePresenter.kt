package app.atomofiron.common.base

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.log2

abstract class BasePresenter<M : BaseViewModel<BaseRouter>, R : BaseRouter>(
        protected val viewModel: M
) {
    protected val context: Context get() = viewModel.getApplication<App>().applicationContext
    protected abstract val router: R
    private val screenScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Main.immediate)

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
        screenScope.cancel()
    }

    fun onAttachChildFragment(childFragment: Fragment) = router.onAttachChildFragment(childFragment)

    open fun onBackButtonClick(): Boolean = router.onBack()

    open fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = Unit
}
