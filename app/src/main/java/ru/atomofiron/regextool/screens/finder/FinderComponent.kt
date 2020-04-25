package ru.atomofiron.regextool.screens.finder

import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.injectable.channel.PreferenceChannel
import ru.atomofiron.regextool.injectable.interactor.FinderInteractor
import ru.atomofiron.regextool.injectable.service.FinderService
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
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
            preferenceStore: PreferenceStore,
            preferenceChannel: PreferenceChannel
    ): FinderPresenter {
        return FinderPresenter(viewModel, router, finderAdapterDelegate, explorerStore, preferenceStore, preferenceChannel)
    }

    @Provides
    @FinderScope
    fun finderAdapterOutput(viewModel: FinderViewModel, interactor: FinderInteractor): FinderAdapterPresenterDelegate {
        return FinderAdapterPresenterDelegate(viewModel, interactor)
    }

    @Provides
    @FinderScope
    fun router(fragment: WeakProperty<FinderFragment>): FinderRouter {
        return FinderRouter(fragment)
    }

    @Provides
    @FinderScope
    fun interactor(finderService: FinderService): FinderInteractor {
        return FinderInteractor(finderService)
    }
}

interface FinderDependencies {
    fun preferenceChannel(): PreferenceChannel
    fun explorerStore(): ExplorerStore
    fun settingsStore(): PreferenceStore
    fun finderService(): FinderService
}
