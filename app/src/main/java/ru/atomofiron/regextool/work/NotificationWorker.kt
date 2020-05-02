package ru.atomofiron.regextool.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.injectable.store.FinderStore
import ru.atomofiron.regextool.log2
import javax.inject.Inject

class NotificationWorker(
        context: Context,
        workerParams: WorkerParameters
) : Worker(context, workerParams) {
    companion object {
        const val NAME = "NotificationWorker"
        private const val PERIOD = 100L
    }

    @Inject
    lateinit var finderStore: FinderStore

    init {
        DaggerInjector.appComponent.inject(this)
    }

    override fun doWork(): Result {
        log2("doWork")

        while (!isStopped) {
            Thread.sleep(PERIOD)
            finderStore.notifyObservers()
        }
        log2("doWork END")

        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        log2("onStopped")
    }
}