package ru.atomofiron.regextool.di.module

import android.content.SharedPreferences
import android.content.res.AssetManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.injectable.channel.FinderStore
import ru.atomofiron.regextool.injectable.service.FinderService
import ru.atomofiron.regextool.injectable.service.explorer.ExplorerService
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import javax.inject.Singleton

@Module
open class ServiceModule {

    @Provides
    @Singleton
    fun explorerService(
            assets: AssetManager,
            preferences: SharedPreferences,
            explorerStore: ExplorerStore,
            preferenceStore: PreferenceStore
    ): ExplorerService = ExplorerService(assets, preferences, explorerStore, preferenceStore)

    @Provides
    @Singleton
    fun finderService(
            workManager: WorkManager,
            finderStore: FinderStore,
            explorerStore: ExplorerStore,
            preferenceStore: PreferenceStore
    ): FinderService = FinderService(workManager, explorerStore, finderStore, preferenceStore)
}
