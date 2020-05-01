package ru.atomofiron.regextool.screens.result

import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.LateinitLiveData
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.log2
import ru.atomofiron.regextool.model.finder.FinderTask
import ru.atomofiron.regextool.model.finder.FinderTaskChange
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition

class ResultViewModel : BaseViewModel<ResultComponent, ResultFragment>() {

    val task = LateinitLiveData<FinderTask>()
    val composition = LateinitLiveData<ExplorerItemComposition>()

    override val component = DaggerResultComponent
            .builder()
            .bind(this)
            .bind(viewProperty)
            .dependencies(DaggerInjector.appComponent)
            .build()

    override fun inject(view: ResultFragment) {
        super.inject(view)
        component.inject(this)
        component.inject(view)
    }

    fun updateState(update: FinderTaskChange) {
        if (update is FinderTaskChange.Update) {
            val task = task.value
            val newTask = update.tasks.find { it.id == task.id }
            when {
                newTask == null -> log2("[ERROR] newTask == null")
                task.count != newTask.count -> this.task.value = newTask.copyTask()
                task.inProgress != newTask.inProgress -> this.task.value = newTask.copyTask()
            }
        }
    }
}