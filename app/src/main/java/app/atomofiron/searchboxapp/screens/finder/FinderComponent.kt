package app.atomofiron.searchboxapp.screens.finder

import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.interactor.FinderInteractor
import app.atomofiron.searchboxapp.injectable.service.FinderService
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.finder.presenter.FinderAdapterPresenterDelegate
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
            finderStore: FinderStore,
            explorerStore: ExplorerStore,
            preferenceStore: PreferenceStore,
            preferenceChannel: PreferenceChannel
    ): FinderPresenter {
        return FinderPresenter(viewModel, router, finderAdapterDelegate, explorerStore,
                preferenceStore, finderStore, preferenceChannel)
    }

    @Provides
    @FinderScope
    fun finderAdapterOutput(viewModel: FinderViewModel, router: FinderRouter, interactor: FinderInteractor): FinderAdapterPresenterDelegate {
        return FinderAdapterPresenterDelegate(viewModel, router, interactor)
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
    fun preferenceStore(): PreferenceStore
    fun finderService(): FinderService
    fun finderStore(): FinderStore
}
