package ru.atomofiron.regextool.screens.result

import android.content.Context
import android.content.Intent
import app.atomofiron.common.arch.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.injectable.channel.FinderStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.log2
import ru.atomofiron.regextool.screens.explorer.adapter.util.ExplorerItemBinder
import ru.atomofiron.regextool.screens.result.ResultFragment.Companion.KEY_TASK_ID
import ru.atomofiron.regextool.screens.result.presenter.ResultItemActionDelegate

class ResultPresenter(
        viewModel: ResultViewModel,
        private val scope: CoroutineScope,
        private val finderStore: FinderStore,
        private val preferenceStore: PreferenceStore,
        router: ResultRouter,
        itemActionDelegate: ResultItemActionDelegate
) : BasePresenter<ResultViewModel, ResultRouter>(viewModel, router),
        ExplorerItemBinder.ExplorerItemBinderActionListener by itemActionDelegate {
    companion object {
        private const val UNDEFINED = -1L
    }
    private var taskId = UNDEFINED

    override fun onCreate(context: Context, intent: Intent) {
        if (taskId == UNDEFINED) {
            taskId = intent.getLongExtra(KEY_TASK_ID, UNDEFINED)
            val task = finderStore.tasks.find { it.id == taskId }
            if (task == null) {
                log2("No task found!")
                router.popScreen()
            } else {
                viewModel.task.value = task.copyTask()
                onSubscribeData()
            }
        }
    }

    override fun onSubscribeData() {
        super.onSubscribeData()
        finderStore.notifications.addObserver(onClearedCallback) { update ->
            scope.launch {
                viewModel.updateState(update)
            }
        }
        preferenceStore.explorerItemComposition.addObserver(onClearedCallback) {
            viewModel.composition.value = it
        }
    }
}