package app.atomofiron.searchboxapp.android

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.Settings
import app.atomofiron.searchboxapp.screens.root.RootActivity
import app.atomofiron.searchboxapp.utils.Const

object Intents {
    const val ACTION_UPDATE = "ACTION_UPDATE"
    const val ACTION_INSTALL_UPDATE = "ACTION_INSTALL_UPDATE"

    const val REQUEST_UPDATE = 7453
    const val REQUEST_REMINDER = 7454
    const val REQUEST_REMIND_IN_10_MIN = 7455
    const val REQUEST_REMIND_IN_AN_HOUR = 7456
    const val REQUEST_RESTORE_BACKUP = 34576
    const val REQUEST_CODE_UPDATE_APP = 12345

    const val KEY_REMIND_IN_MINUTES = "KEY_REMIND_IN_MINUTES"
    const val KEY_WITH_SOUND = "KEY_WITH_SOUND"

    //val telegramLink get() = Intent(Intent.ACTION_VIEW, Uri.parse(Const.TELEGRAM_LINK))

    fun rootActivity(context: Context, action: String? = null) = Intent(context, RootActivity::class.java).setAction(action)

    fun appDetails(context: Context) = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .addCategory(Intent.CATEGORY_DEFAULT)
        .setData(Uri.fromParts(Const.SCHEME_PACKAGE, context.packageName, null))
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun send(uri: Uri) = Intent(Intent.ACTION_SEND)
        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        .setType(Const.MIME_TYPE_ANY)
        .putExtra(Intent.EXTRA_STREAM, uri)

}