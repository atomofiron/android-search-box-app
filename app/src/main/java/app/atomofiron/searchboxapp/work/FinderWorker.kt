package app.atomofiron.searchboxapp.work

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import app.atomofiron.searchboxapp.*
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.finder.ItemCounter
import app.atomofiron.searchboxapp.model.finder.SearchParams
import app.atomofiron.searchboxapp.model.finder.SearchResult.FinderResult
import app.atomofiron.searchboxapp.model.textviewer.SearchTask
import app.atomofiron.searchboxapp.model.textviewer.toEnded
import app.atomofiron.searchboxapp.model.textviewer.toError
import app.atomofiron.searchboxapp.model.textviewer.withError
import app.atomofiron.searchboxapp.screens.main.MainActivity
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.Const.UNDEFINEDL
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.update
import app.atomofiron.searchboxapp.utils.Shell
import app.atomofiron.searchboxapp.utils.escapeQuotes
import app.atomofiron.searchboxapp.utils.updateNotificationChannel
import kotlinx.coroutines.*
import java.util.regex.Pattern
import javax.inject.Inject

class FinderWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    companion object {
        @SuppressLint("InlinedApi")
        private const val UPDATING_FLAG = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        private const val UNDEFINED = -1
        private const val PERIOD = 100L

        private const val KEY_EXCEPTION = "KEY_EXCEPTION"

        private const val KEY_QUERY = "KEY_QUERY"
        private const val KEY_USE_SU = "KEY_USE_SU"
        private const val KEY_USE_REGEX = "KEY_USE_REGEX"
        private const val KEY_MAX_SIZE = "KEY_MAX_SIZE"
        private const val KEY_CASE_INSENSITIVE = "KEY_CASE_INSENSITIVE"
        private const val KEY_EXCLUDE_DIRS = "KEY_EXCLUDE_DIRS"
        private const val KEY_MULTILINE = "KEY_MULTILINE"
        private const val KEY_FOR_CONTENT = "KEY_FOR_CONTENT"
        private const val KEY_MAX_DEPTH = "KEY_MAX_DEPTH"
        private const val KEY_WHERE_PATHS = "KEY_WHERE_PATHS"

        fun inputData(
            query: String, useSu: Boolean, useRegex: Boolean, maxSize: Int, ignoreCase: Boolean, excludeDirs: Boolean,
            isMultiline: Boolean, forContent: Boolean, maxDepth: Int, where: Array<String>,
        ) = Data.Builder()
            .putString(KEY_QUERY, query)
            .putBoolean(KEY_USE_SU, useSu)
            .putBoolean(KEY_USE_REGEX, useRegex)
            .putInt(KEY_MAX_SIZE, maxSize)
            .putBoolean(KEY_CASE_INSENSITIVE, ignoreCase)
            .putBoolean(KEY_EXCLUDE_DIRS, excludeDirs)
            .putBoolean(KEY_MULTILINE, isMultiline)
            .putBoolean(KEY_FOR_CONTENT, forContent)
            .putInt(KEY_MAX_DEPTH, maxDepth)
            .putStringArray(KEY_WHERE_PATHS, where)
            .build()
    }
    private val useSu = inputData.getBoolean(KEY_USE_SU, false)
    private val useRegex = inputData.getBoolean(KEY_USE_REGEX, false)
    private val query: String = inputData.getString(KEY_QUERY) ?: ""
    private lateinit var pattern: Pattern
    private val maxSize = inputData.getLong(KEY_MAX_SIZE, UNDEFINEDL)
    private val ignoreCase = inputData.getBoolean(KEY_CASE_INSENSITIVE, false)
    private val excludeDirs = inputData.getBoolean(KEY_EXCLUDE_DIRS, false)
    private val forContent = inputData.getBoolean(KEY_FOR_CONTENT, false)
    private val maxDepth = inputData.getInt(KEY_MAX_DEPTH, UNDEFINED)

    private var task: SearchTask = SearchTask.Progress(
        id, isLocal = false,
        SearchParams(query, useRegex, ignoreCase),
        if (forContent) FinderResult.forContent() else FinderResult.forNames(),
    )
    private var process: Process? = null
    private var delayedJob: Job? = null
    private val cacheConfig by lazy(LazyThreadSafetyMode.NONE) { CacheConfig(preferenceStore.useSu.value) }
    private val jobs = mutableListOf<Job>()

    @Inject
    lateinit var finderStore: FinderStore
    @Inject
    lateinit var notificationManager: NotificationManager
    @Inject
    lateinit var scope: CoroutineScope
    @Inject
    lateinit var preferenceStore: PreferenceStore

    init {
        if (useRegex && !forContent) {
            var flags = 0
            val isMultiline = inputData.getBoolean(KEY_MULTILINE, false)
            if (isMultiline) flags += Pattern.MULTILINE
            if (ignoreCase) flags += Pattern.CASE_INSENSITIVE
            pattern = Pattern.compile(query, flags)
        }
        DaggerInjector.appComponent.inject(this)
    }

    private val processObserver: (Process) -> Unit = { process = it }

    private suspend fun searchForContent(where: List<Node>) {
        forLoop@for (item in where) {
            if (isStopped) {
                return
            }
            val template = when {
                item.isDirectory && useRegex && ignoreCase -> Shell[Shell.FIND_GREP_CS_IE]
                item.isDirectory && useRegex && !ignoreCase -> Shell[Shell.FIND_GREP_CS_E]
                item.isDirectory && !useRegex && ignoreCase -> Shell[Shell.FIND_GREP_CS_I]
                item.isDirectory && !useRegex && !ignoreCase -> Shell[Shell.FIND_GREP_CS]
                useRegex && ignoreCase -> Shell[Shell.GREP_CS_IE]
                useRegex && !ignoreCase -> Shell[Shell.GREP_CS_E]
                !useRegex && ignoreCase -> Shell[Shell.GREP_CS_I]
                else -> Shell[Shell.GREP_CS]
            }
            val command = when {
                item.isDirectory -> template.format(item.path, maxDepth, query.escapeQuotes())
                item.isFile -> template.format(query.escapeQuotes(), item.path)
                else -> continue@forLoop
            }
            val output = Shell.exec(command, useSu, processObserver, forContentLineListener)
            if (!output.success && output.error.isNotBlank()) {
                logE(output.error)
                updateTask {
                    withError(output.error)
                }
            }
        }
    }

    private suspend fun updateTask(now: Boolean = false, action: SearchTask.() -> SearchTask) {
        task = task.action()
        if (now) {
            delayedJob?.cancel()
            finderStore.addOrUpdate(task)
        } else if (delayedJob?.isActive != true) {
            delayedJob = scope.launch {
                sleep(PERIOD)
                finderStore.addOrUpdate(task)
            }
        }
    }

    private val forContentLineListener: (String) -> Unit = { line ->
        scope.launch {
            val index = line.lastIndexOf(':')
            val count = line.substring(index.inc()).toInt()
            if (count > 0) {
                val path = line.substring(0, index)
                addToResults(path, count)
            }
        }.let { jobs.add(it) }
    }

    private suspend fun searchForName(where: List<Node>) {
        for (item in where) {
            if (isStopped) {
                return
            }
            val template = when {
                excludeDirs -> Shell[Shell.FIND_F]
                else -> Shell[Shell.FIND_FD]
            }
            val command = template.format(item.path, maxDepth)
            val output = Shell.exec(command, useSu, processObserver, forNameLineListener)
            if (!output.success && output.error.isNotBlank()) {
                logE(output.error)
                updateTask {
                    withError(output.error)
                }
            }
        }
    }

    private val forNameLineListener: (String) -> Unit = { line ->
        scope.launch {
            when {
                useRegex && pattern.matcher(line).find() -> addToResults(line)
                !useRegex && line.contains(query, ignoreCase) -> addToResults(line)
            }
        }.let { jobs.add(it) }
    }

    private suspend fun addToResults(path: String, count: Int = -1) {
        val item = Node(path, content = NodeContent.File.Unknown).update(cacheConfig)
        val itemCounter = ItemCounter(item, count)
        var result = task.result as FinderResult
        result = result.add(itemCounter, dCountMax = 1)
        updateTask {
            copyWith(result)
        }
    }

    override suspend fun doWork(): Result {
        return coroutineScope {
            async {
                work()
            }.also { job ->
                job.invokeOnCompletion { throwable ->
                    if (throwable is CancellationException) process?.destroy()
                }
            }.await()
        }
    }

    private suspend fun CoroutineScope.work(): Result {
        context.updateNotificationChannel(
            Const.FOREGROUND_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.foreground_notification_name),
        )
        val info = ForegroundInfo(Const.FOREGROUND_NOTIFICATION_ID, foregroundNotification())
        setForegroundAsync(info)

        if (query.isEmpty()) {
            logE("Query is empty")
            return Result.success()
        }
        val data = try {
            finderStore.add(task)

            delay(3000)

            val where = inputData.getStringArray(KEY_WHERE_PATHS)!!.map { path ->
                Node(path, content = NodeContent.File.Unknown).update(cacheConfig)
            }
            when {
                forContent -> searchForContent(where)
                else -> searchForName(where)
            }
            updateTask(now = true) {
                toEnded(isCompleted = true)
            }
            Data.Builder().build()
        } catch (e: CancellationException) {
            updateTask(now = true) {
                toEnded(isCompleted = false)
            }
            process?.destroy()
            Data.Builder().build()
        } catch (e: Exception) {
            logE("$e")
            updateTask(now = true) {
                toError(e.toString())
            }
            Data.Builder().putString(KEY_EXCEPTION, e.toString()).build()
        } finally {
            jobs.forEach { it.join() }
            showNotification()
        }
        return Result.success(data)
    }

    private fun showNotification() {
        val task = task
        val id = task.uniqueId
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, id, intent, UPDATING_FLAG)
        val error = (task as? SearchTask.Error)?.error
        val icon = when {
            error != null -> R.drawable.ic_notification_error
            task.isDone -> R.drawable.ic_notification_done
            else -> R.drawable.ic_notification_stopped
        }
        val titleId = when {
            task !is SearchTask.Done -> R.string.search_failed
            !task.isCompleted -> R.string.search_stopped
            task.result.isEmpty -> R.string.search_empty
            else -> R.string.search_succeed
        }
        var (subText, text) = task.result.getCounters().takeIf { c -> c.any { it > 0 } }?.let { counters ->
            val subText = counters.joinToString(separator = " / ") { it.toString() }
            val text = when (counters.size) {
                3 -> context.getString(R.string.search_for_content_result, counters[0], counters[1], counters[2])
                2 -> context.getString(R.string.search_for_names_result, counters[0], counters[1])
                else -> null
            }
            subText to text
        } ?: (null to null)
        text = arrayOf(text, error).filterNotNull().joinToString(separator = ".\n")
        val notification = NotificationCompat.Builder(context, Const.RESULT_NOTIFICATION_CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(context.getString(titleId))
                .setSubText(subText)
                .setContentText(text)
                .setSmallIcon(icon)
                .setColor(ContextCompat.getColor(context, R.color.day_night_primary))
                .setContentIntent(pendingIntent)
                .build()

        notification.flags = notification.flags or NotificationCompat.FLAG_AUTO_CANCEL

        context.updateNotificationChannel(
            Const.RESULT_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.result_notification_name),
            NotificationManagerCompat.IMPORTANCE_DEFAULT,
        )

        notificationManager.notify(id, notification)
    }

    private fun foregroundNotification(): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, Const.FOREGROUND_INTENT_REQUEST_CODE, intent, UPDATING_FLAG)
        return NotificationCompat.Builder(context, Const.FOREGROUND_NOTIFICATION_CHANNEL_ID)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentTitle(context.getString(R.string.searching))
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.day_night_primary))
            .setContentIntent(pendingIntent)
            .build()
    }
}