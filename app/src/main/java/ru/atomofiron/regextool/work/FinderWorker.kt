package ru.atomofiron.regextool.work

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.atomofiron.regextool.android.ForegroundService
import ru.atomofiron.regextool.injectable.service.explorer.model.MutableXFile
import ru.atomofiron.regextool.log
import ru.atomofiron.regextool.log2
import java.util.regex.Pattern

class FinderWorker(
        context: Context,
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
                      caseSensitive: Boolean, excludeDirs: Boolean, isMultiline: Boolean,
                      forContent: Boolean, textFormats: Array<String>, maxDepth: Int, where: Array<String>): Data {
            val builder = Data.Builder()
                    .putString(KEY_QUERY, query)
                    .putBoolean(KEY_USE_SU, useSu)
                    .putBoolean(KEY_USE_REGEX, useRegex)
                    .putLong(KEY_MAX_SIZE, maxSize)
                    .putBoolean(KEY_CASE_INSENSITIVE, !caseSensitive)
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

    private val results = ArrayList<Output>()
    private var passed = 0

    private val connection = Connection()

    private fun searchForContent(where: List<MutableXFile>, depth: Int) {
        // todo next
    }

    private fun searchForName(where: List<MutableXFile>, depth: Int) {
        log("searchForName $depth ${results.size}/$passed")
        for (item in where) {
            if (isStopped) {
                return
            }
            if (!item.isDirectory || !excludeDirs) {
                passed++
                if (useRegex) {
                    val matcher = pattern.matcher(item.name)
                    if (matcher.find()) {
                        val result = Output(item.completedPath)
                        results.add(result)
                    }
                } else if (item.name.contains(query, ignoreCase)) {
                    val result = Output(item.completedPath)
                    results.add(result)
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
                    val items = item.files
                    if (items?.isNotEmpty() == true) {
                        searchForName(items, depth.inc())
                    }
                }
            }
        }
    }

    override fun doWork(): Result {
        log2("doWork")
        val intent = Intent(applicationContext, ForegroundService::class.java)
        applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        val queryString = inputData.getString(KEY_QUERY)!!
        useSu = inputData.getBoolean(KEY_USE_SU, useSu)
        useRegex = inputData.getBoolean(KEY_QUERY, useRegex)
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
            val item = MutableXFile.byPath(paths[i])
            where.add(item)
        }

        require(queryString.isNotEmpty()) { Exception("Query is empty.") }

        if (useRegex) {
            var flags = 0
            if (isMultiline) flags += Pattern.MULTILINE
            if (ignoreCase) flags += Pattern.CASE_INSENSITIVE
            pattern = Pattern.compile(queryString, flags)
        } else {
            query = queryString
        }

        val data = try {
            if (forContent) {
                searchForContent(where, depth = 0)
            } else {
                searchForName(where, depth = 0)
            }
            Data.Builder().build()
        } catch (e: Exception) {
            Data.Builder().putString(KEY_EXCEPTION, e.toString()).build()
        }

        applicationContext.unbindService(connection)

        return Result.success(data)
    }

    class Output(
            val completedPath: String,
            val matches: List<Int>? = null
    )

    private class Connection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        }
    }
}