package app.atomofiron.searchboxapp.screens.result

import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.dataFlow
import app.atomofiron.common.util.flow.value
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.logI
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.finder.FinderTaskChange
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.result.adapter.FinderResultItem
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import javax.inject.Inject

class ResultViewModel : BaseViewModel<ResultComponent, ResultFragment, ResultPresenter>() {
    val checked = mutableListOf<XFile>()

    val oneFileOptions = listOf(R.id.menu_copy_path, R.id.menu_remove)
    val manyFilesOptions = listOf(R.id.menu_remove)

    val task = dataFlow<FinderTask>()
    val composition = dataFlow<ExplorerItemComposition>()
    val enableOptions = dataFlow(value = false)
    val showOptions = dataFlow<ExplorerItemOptions>(single = true)
    val notifyTaskHasChanged = dataFlow(Unit, single = true)
    val notifyItemChanged = dataFlow<FinderResultItem.Item>(single = true)
    val alerts = dataFlow<String>(single = true)

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
            null -> notifyTaskHasChanged.invoke()
            is FinderTaskChange.Update -> {
                val task = task.value
                val newTask = update.tasks.find { it.id == task.id }
                when {
                    newTask == null -> logI("[ERROR] newTask == null")
                    !task.areContentsTheSame(newTask) -> this.task.value = newTask.copyTask()
                }
            }
        }
    }
}