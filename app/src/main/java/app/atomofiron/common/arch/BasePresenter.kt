package app.atomofiron.common.arch

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

abstract class BasePresenter<M : ViewModel, R : BaseRouter>(
    protected val viewModel: M,
    protected val router: R,
    private val nullableResponseRecipient: String? = null,
) : Recipient {
    protected val scope = viewModel.viewModelScope
    protected val responseRecipient: String get() = nullableResponseRecipient!!

    abstract fun onSubscribeData()

    fun onNavigationClick() = router.navigateBack()

    open fun setRecipient(recipient: String) = Unit

    open fun onSaveInstanceState(outState: Bundle) = Unit

    open fun onCleared() = Unit
}