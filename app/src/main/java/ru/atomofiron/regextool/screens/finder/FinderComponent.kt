package ru.atomofiron.regextool.screens.finder

import dagger.Component
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.iss.interactor.FinderInteractor
import ru.atomofiron.regextool.iss.store.ExplorerStore
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class FinderScope

@FinderScope
@Component(dependencies = [FinderDependencies::class], modules = [FinderModule::class])
interface FinderComponent {
    @Component.Builder
    interface Builder {
        fun dependencies(dependencies: FinderDependencies): Builder
        fun build(): FinderComponent
    }

    fun inject(target: FinderViewModel)
}

@Module
class FinderModule {
    @Provides
    @FinderScope
    fun interactor(explorerStore: ExplorerStore): FinderInteractor {
        return FinderInteractor(explorerStore)
    }
}

interface FinderDependencies {
    fun explorerStore(): ExplorerStore
}
