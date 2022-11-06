package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.*
import app.atomofiron.searchboxapp.model.finder.FinderResult
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.finder.FinderTaskChange
import app.atomofiron.searchboxapp.model.finder.MutableFinderTask
import kotlinx.coroutines.CoroutineScope
import java.util.*

class FinderStore(
    private val scope: CoroutineScope,
) {
    private val mutableTasks = mutableListOf<MutableFinderTask>()
    val tasks: List<FinderTask> = mutableTasks
    val notifications = ChannelFlow<FinderTaskChange>()

    fun add(item: MutableFinderTask) {
        mutableTasks.add(item)
        scope.send(notifications, FinderTaskChange.Add(item))
        notifications[scope] = FinderTaskChange.Add(item)
    }

    fun drop(item: FinderTask) {
        mutableTasks.remove(item)
        notifications[scope] = FinderTaskChange.Drop(item)
    }

    fun dropTaskError(taskId: Long) {
        val task = mutableTasks.find { it.id == taskId }
        task?.dropError()
    }

    fun notifyObservers() {
        notifications[scope] = FinderTaskChange.Update(tasks)
    }

    fun deleteResultFromTask(item: FinderResult, uuid: UUID) {
        val task = mutableTasks.find { it.uuid == uuid }
        task ?: return
        task.results.remove(item)
        notifications[scope] = FinderTaskChange.Update(listOf(task))
    }
}