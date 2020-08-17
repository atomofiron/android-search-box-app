package app.atomofiron.searchboxapp.work

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.atomofiron.common.util.ServiceConnectionImpl
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.android.ForegroundService
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.logE
import app.atomofiron.searchboxapp.logI
import app.atomofiron.searchboxapp.model.explorer.MutableXFile
import app.atomofiron.searchboxapp.model.finder.FinderQueryParams
import app.atomofiron.searchboxapp.model.finder.FinderResult
import app.atomofiron.searchboxapp.model.finder.MutableFinderTask
import app.atomofiron.searchboxapp.screens.root.RootActivity
import app.atomofiron.searchboxapp.utils.ChannelUtil
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.Shell
import app.atomofiron.searchboxapp.utils.Util
import app.atomofiron.searchboxapp.utils.escapeQuotes
import java.util.regex.Pattern
import javax.inject.Inject

class FinderWorker(
        private val context: Context,
        workerParams: WorkerParameters
) : Worker(context, workerParams) {
    companion object {
        private const val UNDEFINED = -1

        private const val KEY_EXCEPTION = "KEY_EXCEPTION"

        private const val KEY_QUERY = "KEY_QUERY"
        private const val KEY_USE_SU = "KEY_USE_SU"
        private const val KEY_USE_REGEX = "KEY_USE_REGEX"
        private const val KEY_MAX_SIZE = "KEY_MAX_SIZE"
        private const val KEY_CASE_INSENSITIVE = "KEY_CASE_INSENSITIVE"
        private const val KEY_EXCLUDE_DIRS = "KEY_EXCLUDE_DIRS"
        private const val KEY_MULTILINE = "KEY_MULTILINE"
        private const val KEY_FOR_CONTENT = "KEY_FOR_CONTENT"
        private const val KEY_TEXT_FORMATS = "KEY_TEXT_FORMATS"
        private const val KEY_MAX_DEPTH = "KEY_MAX_DEPTH"
        private const val KEY_WHERE_PATHS = "KEY_WHERE_PATHS"

        fun inputData(query: String, useSu: Boolean, useRegex: Boolean, maxSize: Long,
                      ignoreCase: Boolean, excludeDirs: Boolean, isMultiline: Boolean,
                      forContent: Boolean, textFormats: Array<String>, maxDepth: Int, where: Array<String>): Data {
            val builder = Data.Builder()
                    .putString(KEY_QUERY, query)
                    .putBoolean(KEY_USE_SU, useSu)
                    .putBoolean(KEY_USE_REGEX, useRegex)
                    .putLong(KEY_MAX_SIZE, maxSize)
                    .putBoolean(KEY_CASE_INSENSITIVE, ignoreCase)
                    .putBoolean(KEY_EXCLUDE_DIRS, excludeDirs)
                    .putBoolean(KEY_MULTILINE, isMultiline)
                    .putBoolean(KEY_FOR_CONTENT, forContent)
                    .putStringArray(KEY_TEXT_FORMATS, textFormats)
                    .putInt(KEY_MAX_DEPTH, maxDepth)
                    .putStringArray(KEY_WHERE_PATHS, where)

            return builder.build()
        }
    }
    private var useSu = false
    private var useRegex = false
    private lateinit var query: String
    private lateinit var pattern: Pattern
    private var maxSize = UNDEFINED.toLong()
    private var ignoreCase = false
    private var excludeDirs = false
    private var forContent = false
    private lateinit var textFormats: Array<String>
    private var maxDepth = UNDEFINED

    private val task: MutableFinderTask by lazy(LazyThreadSafetyMode.NONE) {
        val params = FinderQueryParams(query, useRegex, ignoreCase)
        MutableFinderTask(id, params)
    }
    private val connection = ServiceConnectionImpl()
    private var process: Process? = null

    @Inject
    lateinit var finderStore: FinderStore
    @Inject
    lateinit var notificationManager: NotificationManager

    init {
        DaggerInjector.appComponent.inject(this)
    }

    override fun onStopped() {
        super.onStopped()
        process?.destroy()
    }

    private val processObserver: (Process) -> Unit = { process = it }

    private fun searchForContent(where: List<MutableXFile>) {
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
                !useRegex && !ignoreCase -> Shell[Shell.GREP_CS]
                else -> throw Exception()
            }
            val nameArgs = textFormats.joinToString(" -o ") { "-name '*.$it'" }
            val isTextFile = item.isFile && Util.isTextFile(item.completedPath, textFormats)
            val command = when {
                item.isDirectory -> template.format(item.completedPath, maxDepth, nameArgs, query.escapeQuotes())
                isTextFile -> template.format(query.escapeQuotes(), item.completedPath)
                else -> continue@forLoop
            }
            val output = Shell.exec(command, useSu, processObserver, forContentLineListener)
            if (!output.success && output.error.isNotBlank()) {
                logE(output.error)
                task.error = output.error
            }
        }
    }

    private val forContentLineListener: (String) -> Unit = { line ->
        val index = line.lastIndexOf(':')
        val count = line.substring(index.inc()).toInt()
        if (count > 0) {
            val path = line.substring(0, index)
            addToResults(path, count)
        }
        task.count++
    }

    private fun searchForName(where: List<MutableXFile>) {
        for (item in where) {
            val template = when {
                excludeDirs -> Shell[Shell.FIND_F]
                else -> Shell[Shell.FIND_FD]
            }
            val command = template.format(item.completedPath, maxDepth)
            val output = Shell.exec(command, useSu, processObserver, forNameLineListener)
            if (!output.success && output.error.isNotBlank()) {
                logE(output.error)
                task.error = output.error
            }
        }
    }

    private val forNameLineListener: (String) -> Unit = { line ->
        when {
            useRegex && pattern.matcher(line).find() -> addToResults(line)
            !useRegex && line.contains(query, ignoreCase) -> addToResults(line)
        }
        task.count++
    }

    private fun addToResults(path: String, count: Int = 0) {
        val xFile = MutableXFile.byPath(path)
        val result = FinderResult(xFile, count)
        task.results.add(result)
    }

    override fun doWork(): Result {
        logI("doWork")

        val queryString = inputData.getString(KEY_QUERY)

        if (queryString.isNullOrEmpty()) {
            logE("Query is empty.")
            return Result.success()
        }
        query = queryString

        useSu = inputData.getBoolean(KEY_USE_SU, useSu)
        useRegex = inputData.getBoolean(KEY_USE_REGEX, useRegex)
        maxSize = inputData.getLong(KEY_MAX_SIZE, UNDEFINED.toLong())
        ignoreCase = inputData.getBoolean(KEY_CASE_INSENSITIVE, ignoreCase)
        excludeDirs = inputData.getBoolean(KEY_EXCLUDE_DIRS, excludeDirs)
        val isMultiline = inputData.getBoolean(KEY_MULTILINE, false)
        forContent = inputData.getBoolean(KEY_FOR_CONTENT, forContent)
        textFormats = inputData.getStringArray(KEY_TEXT_FORMATS)!!
        maxDepth = inputData.getInt(KEY_MAX_DEPTH, UNDEFINED)

        finderStore.add(task)
        val intent = Intent(applicationContext, ForegroundService::class.java)
        applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        val where = ArrayList<MutableXFile>()
        val paths = inputData.getStringArray(KEY_WHERE_PATHS)!!
        for (i in paths.indices) {
            val item = MutableXFile.byPath(paths[i])
            where.add(item)
        }

        if (useRegex && !forContent) {
            var flags = 0
            if (isMultiline) flags += Pattern.MULTILINE
            if (ignoreCase) flags += Pattern.CASE_INSENSITIVE
            pattern = Pattern.compile(queryString, flags)
        }

        val data = try {
            when {
                forContent -> searchForContent(where)
                else -> searchForName(where)
            }
            Data.Builder().build()
        } catch (e: Exception) {
            logI("$e")
            task.error = e.toString()
            Data.Builder().putString(KEY_EXCEPTION, e.toString()).build()
        }

        task.isDone = !isStopped
        task.inProgress = false
        finderStore.notifyObservers()

        showNotification()
        applicationContext.unbindService(connection)

        return Result.success(data)
    }

    private fun showNotification() {
        val id = task.id.toInt()
        val intent = Intent(context, RootActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val color = ContextCompat.getColor(context, R.color.colorPrimaryLight)
        val icon = when {
            task.error != null -> R.drawable.ic_notification_error
            task.isDone -> R.drawable.ic_notification_done
            else -> R.drawable.ic_notification_stopped
        }
        val notification = NotificationCompat.Builder(context, Const.RESULT_NOTIFICATION_CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(context.getString(R.string.search_completed, task.results.size, task.count))
                .setContentText(task.error)
                .setSmallIcon(icon)
                .setColor(color)
                .setContentIntent(pendingIntent)
                .build()

        notification.flags = notification.flags or NotificationCompat.FLAG_AUTO_CANCEL

        ChannelUtil.id(Const.RESULT_NOTIFICATION_CHANNEL_ID)
                .name(context.getString(R.string.result_notification_name))
                .importance(NotificationManager.IMPORTANCE_DEFAULT)
                .fix(context)

        notificationManager.notify(id, notification)
    }
}