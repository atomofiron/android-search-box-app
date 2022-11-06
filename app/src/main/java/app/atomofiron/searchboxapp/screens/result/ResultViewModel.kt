package app.atomofiron.searchboxapp.screens.result

import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.*
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.logI
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.finder.FinderTaskChange
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class ResultViewModel : BaseViewModel<ResultComponent, ResultFragment, ResultPresenter>() {
    val checked = mutableListOf<Node>()

    val oneFileOptions = listOf(R.id.menu_copy_path, R.id.menu_remove)
    val manyFilesOptions = listOf(R.id.menu_remove)

    val task = DeferredStateFlow<FinderTask>()
    val composition = DeferredStateFlow<ExplorerItemComposition>()
    val enableOptions = MutableStateFlow(false)
    val showOptions = ChannelFlow<Pair<ExplorerItemOptions, CurtainApi.Controller>>()
    val notifyTaskHasChanged = ChannelFlow<Unit>()
    val alerts = ChannelFlow<String>()

    @Inject
    override lateinit var presenter: ResultPresenter
    private lateinit var params: ResultPresenterParams

    override fun inject(view: ResultFragment) {
        params = ResultPresenterParams.params(view.requireArguments())
        super.inject(view)
        component.inject(this)
    }

    override fun createComponent(fragmentProperty: WeakProperty<Fragment>) = DaggerResultComponent
        .builder()
        .bind(this)
        .bind(fragmentProperty)
        .bind(viewModelScope)
        .bind(params)
        .dependencies(DaggerInjector.appComponent)
        .build()

    fun updateState(update: FinderTaskChange? = null) {
        when (update) {
            null -> notifyTaskHasChanged(viewModelScope)
            is FinderTaskChange.Update -> {
                val task = task.value
                val newTask = update.tasks.find { it.id == task.id }
                when {
                    newTask == null -> logI("[ERROR] newTask == null")
                    !task.areContentsTheSame(newTask) -> this.task.value = newTask.copyTask()
                }
            }
            is FinderTaskChange.Add -> Unit
            is FinderTaskChange.Drop -> Unit
        }
    }

    fun sendAlert(value: String) {
        alerts[viewModelScope] = value
    }
}