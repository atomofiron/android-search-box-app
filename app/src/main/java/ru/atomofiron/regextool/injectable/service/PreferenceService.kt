package ru.atomofiron.regextool.injectable.service

import android.content.Context
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.utils.Shell

class PreferenceService(val context: Context) {
    private val packageName = context.packageName
    private val toybox = App.pathToybox
    private val internalPath = context.applicationInfo.dataDir
    private val externalPath: String get() = context.getExternalFilesDir(null)!!.absolutePath

    fun exportPreferences(): Shell.Output {
        return Shell.exec("$toybox cp -f $internalPath/shared_prefs/${packageName}_preferences.xml $externalPath/")
    }

    fun exportHistory(): Shell.Output {
        return Shell.exec("$toybox cp -f $internalPath/databases/history* $externalPath/")
    }

    fun importPreferences(): Shell.Output {
        return Shell.exec("$toybox cp -f $externalPath/${packageName}_preferences.xml $internalPath/shared_prefs/")
    }

    fun importHistory(): Shell.Output {
        return Shell.exec("$toybox cp -f $externalPath/history* $internalPath/databases/")
    }
}