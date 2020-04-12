package ru.atomofiron.regextool.screens.finder

import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.injectable.channel.PreferenceChannel
import ru.atomofiron.regextool.injectable.interactor.FinderInteractor
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.SettingsStore
import ru.atomofiron.regextool.screens.finder.presenter.FinderAdapterPresenterDelegate
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
        @BindsInstance
        fun bind(viewMode: FinderViewModel): Builder
        @BindsInstance
        fun bind(fragment: WeakProperty<FinderFragment>): Builder
        fun dependencies(dependencies: FinderDependencies): Builder
        fun build(): FinderComponent
    }

    fun inject(target: FinderViewModel)
    fun inject(target: FinderFragment)
}

@Module
class FinderModule {
    @Provides
    @FinderScope
    fun presenter(
            viewModel: FinderViewModel,
            router: FinderRouter,
            finderAdapterDelegate: FinderAdapterPresenterDelegate,
            explorerStore: ExplorerStore,
            settingsStore: SettingsStore,
            preferenceChannel: PreferenceChannel
    ): FinderPresenter {
        return FinderPresenter(viewModel, router, finderAdapterDelegate, explorerStore, settingsStore, preferenceChannel)
    }

    @Provides
    @FinderScope
    fun finderAdapterOutput(viewModel: FinderViewModel): FinderAdapterPresenterDelegate {
        return FinderAdapterPresenterDelegate(viewModel)
    }

    @Provides
    @FinderScope
    fun router(fragment: WeakProperty<FinderFragment>): FinderRouter {
        return FinderRouter(fragment)
    }

    @Provides
    @FinderScope
    fun interactor(explorerStore: ExplorerStore): FinderInteractor {
        return FinderInteractor(explorerStore)
    }
}

interface FinderDependencies {
    fun preferenceChannel(): PreferenceChannel
    fun explorerStore(): ExplorerStore
    fun settingsStore(): SettingsStore
}
