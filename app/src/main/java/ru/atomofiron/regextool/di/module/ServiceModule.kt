package ru.atomofiron.regextool.di.module

import android.content.SharedPreferences
import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.injectable.service.explorer.ExplorerService
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.SettingsStore
import javax.inject.Singleton

@Module
open class ServiceModule {

    @Provides
    @Singleton
    fun explorerService(
            assets: AssetManager,
            preferences: SharedPreferences,
            explorerStore: ExplorerStore,
            settingsStore: SettingsStore
    ): ExplorerService = ExplorerService(assets, preferences, explorerStore, settingsStore)
}
