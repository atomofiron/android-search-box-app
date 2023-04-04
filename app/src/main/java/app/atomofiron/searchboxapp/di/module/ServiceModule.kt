package app.atomofiron.searchboxapp.di.module

import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.content.res.AssetManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.channel.ResultChannel
import app.atomofiron.searchboxapp.injectable.service.*
import app.atomofiron.searchboxapp.injectable.store.*
import kotlinx.coroutines.CoroutineScope
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
        packageManager: PackageManager,
    ): ExplorerService = ExplorerService(context, packageManager, assets, appStore, explorerStore, preferenceStore)

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

    @Provides
    @Singleton
    fun apkService(
        appStore: AppStore,
        packageInstaller: PackageInstaller,
        contentResolver: ContentResolver,
    ): ApkService = ApkService(appStore.context, packageInstaller, contentResolver)

    @Provides
    @Singleton
    fun textViewerService(
        scope: CoroutineScope,
        preferenceStore: PreferenceStore,
        textViewerStore: TextViewerStore,
        finderStore: FinderStore,
    ): TextViewerService = TextViewerService(scope, preferenceStore, textViewerStore, finderStore)
}
