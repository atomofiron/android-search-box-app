package app.atomofiron.searchboxapp.di.module

import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.res.AssetManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.channel.ResultChannel
import app.atomofiron.searchboxapp.injectable.service.FinderService
import app.atomofiron.searchboxapp.injectable.service.ResultService
import app.atomofiron.searchboxapp.injectable.service.WindowService
import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.injectable.store.*
import javax.inject.Singleton

@Module
open class ServiceModule {

    @Provides
    @Singleton
    fun explorerService(
        context: Context,
        assets: AssetManager,
        appStore: AppStore,
        explorerStore: ExplorerStore,
        preferenceStore: PreferenceStore,
    ): ExplorerService = ExplorerService(context, assets, appStore, explorerStore, preferenceStore)

    @Provides
    @Singleton
    fun finderService(
        workManager: WorkManager,
        notificationManager: NotificationManager,
        finderStore: FinderStore,
        preferenceStore: PreferenceStore,
    ): FinderService = FinderService(workManager, notificationManager, finderStore, preferenceStore)

    @Provides
    @Singleton
    fun resultService(
        workManager: WorkManager,
        resultChannel: ResultChannel,
        resultStore: ResultStore,
        finderStore: FinderStore,
        preferenceStore: PreferenceStore,
        clipboardManager: ClipboardManager,
    ): ResultService = ResultService(workManager, resultChannel, resultStore , finderStore, preferenceStore, clipboardManager)

    @Provides
    @Singleton
    fun windowService(
        appStore: AppStore,
    ): WindowService = WindowService(appStore)
}
