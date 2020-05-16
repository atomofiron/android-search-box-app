package ru.atomofiron.regextool.injectable.store

import app.atomofiron.common.util.KObservable
import ru.atomofiron.regextool.model.finder.FinderResult
import ru.atomofiron.regextool.model.finder.FinderTask
import ru.atomofiron.regextool.model.finder.FinderTaskChange
import ru.atomofiron.regextool.model.finder.MutableFinderTask
import java.util.*
import kotlin.collections.ArrayList

class FinderStore {
    private val mutableTasks = ArrayList<MutableFinderTask>()
    val tasks: List<FinderTask> = mutableTasks
    val notifications = KObservable<FinderTaskChange>(single = true)

    fun add(item: MutableFinderTask) {
        mutableTasks.add(item)
        notifications.setAndNotify(FinderTaskChange.Add(item))
    }

    fun drop(item: FinderTask) {
        mutableTasks.remove(item)
        notifications.setAndNotify(FinderTaskChange.Drop(item))
    }

    fun dropTaskError(taskId: Long) {
        val task = mutableTasks.find { it.id == taskId }
        task?.dropError()
    }

    fun notifyObservers() = notifications.setAndNotify(FinderTaskChange.Update(tasks))

    fun deleteResultFromTask(item: FinderResult, uuid: UUID) {
        val task = mutableTasks.find { it.uuid == uuid }
        task ?: return
        task.results.remove(item)
        notifications.setAndNotify(FinderTaskChange.Update(listOf(task)))
    }
}