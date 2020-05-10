package ru.atomofiron.regextool.work

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
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.android.ForegroundService
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.injectable.store.FinderStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.logE
import ru.atomofiron.regextool.logI
import ru.atomofiron.regextool.model.explorer.MutableXFile
import ru.atomofiron.regextool.model.finder.FinderQueryParams
import ru.atomofiron.regextool.model.finder.FinderResult
import ru.atomofiron.regextool.model.finder.MutableFinderTask
import ru.atomofiron.regextool.screens.root.RootActivity
import ru.atomofiron.regextool.utils.ChannelUtil
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Shell
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
    private val toyboxPath: String by lazy { preferenceStore.toyboxVariant.entity.toyboxPath }
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

    private val task = MutableFinderTask(id)
    private val connection = ServiceConnectionImpl()
    private var process: Process? = null

    @Inject
    lateinit var finderStore: FinderStore
    @Inject
    lateinit var preferenceStore: PreferenceStore
    @Inject
    lateinit var notificationManager: NotificationManager

    init {
        DaggerInjector.appComponent.inject(this)
    }

    override fun onStopped() {
        super.onStopped()
        process?.destroy()
    }

    private fun searchForContent(where: List<MutableXFile>) {
        for (item in where) {
            if (isStopped) {
                return
            }
            val template = when {
                useRegex && ignoreCase -> Shell.FIND_GREP_I
                useRegex && !ignoreCase -> Shell.FIND_GREP
                !useRegex && ignoreCase -> Shell.FIND_GREP_IF
                !useRegex && !ignoreCase -> Shell.FIND_GREP_F
                else -> throw Exception()
            }
            val nameArgs = textFormats.joinToString(" -o ") { "-name '*.$it'" }
            val command = template.format(toyboxPath, item.completedPath, maxDepth, nameArgs, toyboxPath, query)
            val output = Shell.exec(command, useSu) { line ->
                val index = line.lastIndexOf(':')
                val count = line.substring(index.inc()).toInt()
                if (count > 0) {
                    val path = line.substring(0, index)
                    val xFile = MutableXFile.byPath(path)
                    val params = FinderQueryParams(query, useRegex, ignoreCase)
                    val result = FinderResult(xFile, count, params)
                    task.results.add(result)
                }
                task.count++
            }
            if (!output.success) {
                task.error = output.error
                logE(output.error)
            }
        }
    }

    private fun searchForName(where: List<MutableXFile>, depth: Int) {
        for (item in where) {
            Thread.sleep(1000)
            if (isStopped) {
                return
            }
            if (!item.isDirectory || !excludeDirs) {
                task.count++
                if (useRegex) {
                    val matcher = pattern.matcher(item.name)
                    if (matcher.find()) {
                        val result = FinderResult(item)
                        task.results.add(result)
                    }
                } else if (item.name.contains(query, ignoreCase)) {
                    val result = FinderResult(item)
                    task.results.add(result)
                }
            }
        }
        if (depth < maxDepth) {
            for (item in where) {
                if (item.isDirectory) {
                    if (isStopped) {
                        return
                    }
                    item.updateCache(useSu)
                    val items = item.children
                    if (items?.isNotEmpty() == true) {
                        searchForName(items, depth.inc())
                    }
                }
            }
        } else {
            logI("searchForName depth limit $depth")
        }
    }

    override fun doWork(): Result {
        logI("doWork")

        val queryString = inputData.getString(KEY_QUERY)

        if (queryString.isNullOrEmpty()) {
            logE("Query is empty.")
            return Result.success()
        }
        query = queryString

        finderStore.add(task)
        val intent = Intent(applicationContext, ForegroundService::class.java)
        applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        useSu = inputData.getBoolean(KEY_USE_SU, useSu)
        useRegex = inputData.getBoolean(KEY_USE_REGEX, useRegex)
        maxSize = inputData.getLong(KEY_MAX_SIZE, UNDEFINED.toLong())
        ignoreCase = inputData.getBoolean(KEY_CASE_INSENSITIVE, ignoreCase)
        excludeDirs = inputData.getBoolean(KEY_EXCLUDE_DIRS, excludeDirs)
        val isMultiline = inputData.getBoolean(KEY_MULTILINE, false)
        forContent = inputData.getBoolean(KEY_FOR_CONTENT, forContent)
        textFormats = inputData.getStringArray(KEY_TEXT_FORMATS)!!
        maxDepth = inputData.getInt(KEY_MAX_DEPTH, UNDEFINED)
        val where = ArrayList<MutableXFile>()
        val paths = inputData.getStringArray(KEY_WHERE_PATHS)!!

        for (i in paths.indices) {
            val item = MutableXFile.asRoot(paths[i])
            where.add(item)
        }

        if (useRegex && !forContent) {
            var flags = 0
            if (isMultiline) flags += Pattern.MULTILINE
            if (ignoreCase) flags += Pattern.CASE_INSENSITIVE
            pattern = Pattern.compile(queryString, flags)
        }

        val data = try {
            if (forContent) {
                searchForContent(where)
            } else {
                searchForName(where, depth = 0)
            }
            Data.Builder().build()
        } catch (e: Exception) {
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