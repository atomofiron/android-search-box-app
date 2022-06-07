package app.atomofiron.searchboxapp.screens.curtain

import com.google.android.material.snackbar.Snackbar
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.emitNow
import app.atomofiron.common.util.flow.isInitialized
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.channel.CurtainResponse
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainAction
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainPresenterParams
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi

class CurtainPresenter(
    private val params: CurtainPresenterParams,
    viewModel: CurtainViewModel,
    router: CurtainRouter,
    private val curtainChannel: CurtainChannel,
) : BasePresenter<CurtainViewModel, CurtainRouter>(viewModel, router), CurtainApi.Controller {
    private var adapter: CurtainApi.Adapter<*>? = null

    override val requestFrom: String = params.recipient
    override val requestId: Int = params.layoutId

    init {
        curtainChannel.emit(CurtainResponse(params.recipient, this))
    }

    override fun onSubscribeData() = Unit

    override fun onCleared() {
        adapter?.clear()
        curtainChannel.emit(CurtainResponse(params.recipient, null))
    }

    override fun setAdapter(adapter: CurtainApi.Adapter<*>) {
        viewModel.setCurtainAdapter(adapter)
        this.adapter?.clear()
        this.adapter = adapter
    }

    override fun showNext(layoutId: Int) = viewModel.action.emitNow(CurtainAction.ShowNext(layoutId))

    override fun showPrev() = viewModel.action.emitNow(CurtainAction.ShowPrev)

    override fun close(immediately: Boolean) = when {
        immediately -> router.navigateBack()
        else -> viewModel.action.emitNow(CurtainAction.Hide)
    }

    override fun showSnackbar(string: String, duration: Int) = showSnackbar {
        Snackbar.make(it, string, duration)
    }

    override fun showSnackbar(stringId: Int, duration: Int) = showSnackbar {
        Snackbar.make(it, stringId, duration)
    }

    override fun showSnackbar(provider: CurtainApi.SnackbarProvider) = viewModel.action.emitNow(CurtainAction.ShowSnackbar(provider))

    override fun setCancelable(value: Boolean) = viewModel.cancelable.emitNow(value)

    fun onShown() {
        if (!viewModel.adapter.isInitialized) router.navigateBack()
    }

    fun onHidden() = router.navigateBack()

    fun onNullViewGot() = router.navigateBack()
}