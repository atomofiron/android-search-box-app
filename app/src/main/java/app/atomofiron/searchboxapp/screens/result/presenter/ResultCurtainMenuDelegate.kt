package app.atomofiron.searchboxapp.screens.result.presenter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.Recipient
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.interactor.ResultInteractor
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.model.finder.FinderResult
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.presenter.curtain.OptionsDelegate
import app.atomofiron.searchboxapp.screens.result.ResultRouter
import app.atomofiron.searchboxapp.screens.result.ResultViewModel
import app.atomofiron.searchboxapp.utils.showCurtain

class ResultCurtainMenuDelegate(
    private val viewModel: ResultViewModel,
    private val router: ResultRouter,
    private val interactor: ResultInteractor,
    appStore: AppStore,
    curtainChannel: CurtainChannel,
) : Recipient, CurtainApi.Adapter<CurtainApi.ViewHolder>(), MenuListener {

    private val resources by appStore.resourcesProperty
    private val optionsDelegate = OptionsDelegate(R.menu.item_options_result, this)
    override var data: ExplorerItemOptions? = null

    init {
        curtainChannel.flow.filterForMe().collect(viewModel.viewModelScope, ::setController)
    }

    override fun getHolder(inflater: LayoutInflater, container: ViewGroup, layoutId: Int): CurtainApi.ViewHolder? {
        val data = data ?: return null
        val view = optionsDelegate.getView(data, inflater, container)
        return CurtainApi.ViewHolder(view)
    }

    override fun onMenuItemSelected(id: Int) {
        val data = data ?: return
        controller?.close()
        val items = data.items
        when (id) {
            R.id.menu_copy_path -> {
                interactor.copyToClipboard(items.first() as FinderResult)
                viewModel.sendAlert(resources.getString(R.string.copied))
            }
            R.id.menu_remove -> interactor.deleteItems(items, viewModel.task.value.uuid)
        }
    }

    fun showOptions(options: ExplorerItemOptions) {
        data = options
        router.showCurtain(recipient, 0)
    }
}