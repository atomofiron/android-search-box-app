package app.atomofiron.searchboxapp.screens.main.presenter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
) : AppEventDelegateApi {

    private val init = Job()

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
        preferenceStore.deepBlack.collect(this) {
            val theme = preferenceStore.appTheme.entity
            if (theme is AppTheme.Dark && (theme.deepBlack xor it)) {
                preferenceStore.appTheme.pushByEntity(AppTheme.Dark(deepBlack = it))
            }
        }
        init.complete()
    }

    override fun onActivityCreate(activity: AppCompatActivity) {
        appStore.onActivityCreate(activity)
        appStore.onResourcesChange(activity.resources)
    }

    override fun onIntent(intent: Intent) {
    }

    override fun onDestroy(activity: AppCompatActivity) {
        appStore.onActivityDestroy()
    }

    override fun onActivityFinish() = Unit// todo appUpdateService.tryCompleteUpdate(forced = false)
}