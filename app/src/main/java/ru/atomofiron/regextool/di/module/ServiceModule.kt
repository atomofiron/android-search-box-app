package ru.atomofiron.regextool.di.module

import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.injectable.service.FinderService
import ru.atomofiron.regextool.injectable.service.ResultService
import ru.atomofiron.regextool.injectable.service.explorer.ExplorerService
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.FinderStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.injectable.store.ResultStore
import javax.inject.Singleton

@Module
open class ServiceModule {

    @Provides
    @Singleton
    fun explorerService(
            context: Context,
            assets: AssetManager,
            preferences: SharedPreferences,
            explorerStore: ExplorerStore,
            preferenceStore: PreferenceStore
    ): ExplorerService = ExplorerService(context, assets, preferences, explorerStore, preferenceStore)

    @Provides
    @Singleton
    fun finderService(
            workManager: WorkManager,
            notificationManager: NotificationManager,
            finderStore: FinderStore,
            preferenceStore: PreferenceStore
    ): FinderService = FinderService(workManager, notificationManager, finderStore, preferenceStore)

    @Provides
    @Singleton
    fun resultService(
            workManager: WorkManager,
            resultStore: ResultStore,
            finderStore: FinderStore,
            preferenceStore: PreferenceStore,
            clipboardManager: ClipboardManager
    ): ResultService = ResultService(workManager, resultStore , finderStore, preferenceStore, clipboardManager)
}
