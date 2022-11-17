package app.atomofiron.searchboxapp.screens.main.presenter

import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import androidx.appcompat.app.AppCompatActivity
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.injectable.channel.MainChannel
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.AppTheme
import kotlinx.coroutines.*

interface AppEventDelegateApi {
    fun onActivityCreate(activity: AppCompatActivity)
    fun onIntent(intent: Intent)
    fun onDestroy(activity: AppCompatActivity)
    fun onActivityFinish()
}

class AppEventDelegate(
    private val scope: CoroutineScope,
    private val appStore: AppStore,
    private val preferenceStore: PreferenceStore,
    private val mainChannel: MainChannel,
) : AppEventDelegateApi {

    private val init = Job()
    private var theme: AppTheme? = null

    init {
        scope.launch(Dispatchers.Default) {
            runCatching {
                init()
            }.onFailure {
                // track the fail
            }
        }
    }

    private fun CoroutineScope.init() {
        preferenceStore.appTheme.collect(this) { theme ->
            if (theme != this@AppEventDelegate.theme) {
                preferenceStore.setAppTheme(theme)
            }
        }
        init.complete()
    }

    override fun onActivityCreate(activity: AppCompatActivity) {
        appStore.onActivityCreate(activity)
        appStore.onResourcesChange(activity.resources)
    }

    override fun onIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = when {
                    SDK_INT >= TIRAMISU -> intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    else -> intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri?
                }
                // todo alerts
                uri ?: return
                mainChannel.fileToReceive[scope] = uri
            }
        }
    }

    override fun onDestroy(activity: AppCompatActivity) {
        appStore.onActivityDestroy()
    }

    override fun onActivityFinish() = Unit// todo appUpdateService.tryCompleteUpdate(forced = false)
}