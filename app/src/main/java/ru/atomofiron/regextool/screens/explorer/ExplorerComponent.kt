package ru.atomofiron.regextool.screens.explorer

import android.content.SharedPreferences
import android.content.res.AssetManager
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.injectable.interactor.ExplorerInteractor
import ru.atomofiron.regextool.injectable.service.explorer.ExplorerService
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.SettingsStore
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class ExplorerScope

@ExplorerScope
@Component(dependencies = [ExplorerDependencies::class], modules = [ExplorerModule::class])
interface ExplorerComponent {
    @Component.Builder
    interface Builder {

        fun dependencies(dependencies: ExplorerDependencies): Builder

        fun build(): ExplorerComponent
    }

    fun inject(target: ExplorerViewModel)
}

@Module
class ExplorerModule {
    @Provides
    @ExplorerScope
    fun explorerService(
            assets: AssetManager,
            preferences: SharedPreferences,
            explorerStore: ExplorerStore,
            settingsStore: SettingsStore
    ): ExplorerService = ExplorerService(assets, preferences, explorerStore, settingsStore)

    @Provides
    @ExplorerScope
    fun interactor(explorerService: ExplorerService): ExplorerInteractor {
        return ExplorerInteractor(explorerService)
    }
}

interface ExplorerDependencies {
    fun assetManager(): AssetManager
    fun sharedPreferences(): SharedPreferences
    fun explorerStore(): ExplorerStore
    fun settingsStore(): SettingsStore
}
