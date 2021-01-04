package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.DataFlow
import app.atomofiron.searchboxapp.model.finder.FinderResult
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.finder.FinderTaskChange
import app.atomofiron.searchboxapp.model.finder.MutableFinderTask
import java.util.*
import kotlin.collections.ArrayList

class FinderStore {
    private val mutableTasks = ArrayList<MutableFinderTask>()
    val tasks: List<FinderTask> = mutableTasks
    val notifications = DataFlow<FinderTaskChange>(single = true)

    fun add(item: MutableFinderTask) {
        mutableTasks.add(item)
        notifications.value = FinderTaskChange.Add(item)
    }

    fun drop(item: FinderTask) {
        mutableTasks.remove(item)
        notifications.value = FinderTaskChange.Drop(item)
    }

    fun dropTaskError(taskId: Long) {
        val task = mutableTasks.find { it.id == taskId }
        task?.dropError()
    }

    fun notifyObservers() = notifications.emit(FinderTaskChange.Update(tasks))

    fun deleteResultFromTask(item: FinderResult, uuid: UUID) {
        val task = mutableTasks.find { it.uuid == uuid }
        task ?: return
        task.results.remove(item)
        notifications.value = FinderTaskChange.Update(listOf(task))
    }
}