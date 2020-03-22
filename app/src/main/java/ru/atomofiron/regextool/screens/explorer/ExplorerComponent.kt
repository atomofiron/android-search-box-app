package ru.atomofiron.regextool.screens.explorer

import android.content.SharedPreferences
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.iss.interactor.ExplorerInteractor
import ru.atomofiron.regextool.iss.service.explorer.ExplorerService
import ru.atomofiron.regextool.iss.store.ExplorerStore
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
            preferences: SharedPreferences,
            explorerStore: ExplorerStore
    ): ExplorerService = ExplorerService(preferences, explorerStore)

    @Provides
    @ExplorerScope
    fun interactor(explorerService: ExplorerService): ExplorerInteractor {
        return ExplorerInteractor(explorerService)
    }
}

interface ExplorerDependencies {
    fun sharedPreferences(): SharedPreferences
    fun explorerStore(): ExplorerStore
}
