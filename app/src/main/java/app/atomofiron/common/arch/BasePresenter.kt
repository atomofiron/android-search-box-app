package app.atomofiron.common.arch

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

abstract class BasePresenter<M : ViewModel, R : BaseRouter>(
    private val vM: M? = null,
    protected val scope: CoroutineScope = vM!!.viewModelScope,
    protected val router: R,
    private val nullableResponseRecipient: String? = null,
) : Recipient {
    protected val viewModel: M get() = vM!!
    protected val responseRecipient: String get() = nullableResponseRecipient!!

    abstract fun onSubscribeData()

    fun onNavigationClick() = router.navigateBack()

    open fun setRecipient(recipient: String) = Unit

    open fun onSaveInstanceState(outState: Bundle) = Unit

    open fun onCleared() = Unit
}