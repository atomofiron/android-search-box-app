package ru.atomofiron.regextool.injectable.channel

import app.atomofiron.common.util.KObservable
import ru.atomofiron.regextool.model.finder.FinderTask
import ru.atomofiron.regextool.model.finder.FinderTaskChange

class FinderStore {
    private val mutableTasks = ArrayList<FinderTask>()
    val tasks: List<FinderTask> = mutableTasks
    val notifications = KObservable<FinderTaskChange>(single = true)

    fun add(item: FinderTask) {
        mutableTasks.add(item)
        notifications.setAndNotify(FinderTaskChange.Add(item))
    }

    fun drop(item: FinderTask) {
        mutableTasks.remove(item)
        notifications.setAndNotify(FinderTaskChange.Drop(item))
    }

    fun notifyObservers() = notifications.setAndNotify(FinderTaskChange.Update)
}