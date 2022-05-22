package app.atomofiron.searchboxapp.screens.result

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import app.atomofiron.searchboxapp.injectable.channel.ResultChannel
import app.atomofiron.searchboxapp.injectable.interactor.ResultInteractor
import app.atomofiron.searchboxapp.injectable.service.ResultService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.injectable.store.ResultStore
import app.atomofiron.searchboxapp.screens.result.presenter.BottomSheetMenuListenerDelegate
import app.atomofiron.searchboxapp.screens.result.presenter.ResultItemActionDelegate
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class ResultScope

@ResultScope
@Component(dependencies = [ResultDependencies::class], modules = [ResultModule::class])
interface ResultComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(viewModel: ResultViewModel): Builder
        @BindsInstance
        fun bind(activity: WeakProperty<Fragment>): Builder
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(params: ResultPresenterParams): Builder
        fun dependencies(dependencies: ResultDependencies): Builder
        fun build(): ResultComponent
    }

    fun inject(target: ResultViewModel)
}

@Module
class ResultModule {

    @Provides
    @ResultScope
    fun presenter(
        params: ResultPresenterParams,
        viewModel: ResultViewModel,
        resultStore: ResultStore,
        finderStore: FinderStore,
        preferenceStore: PreferenceStore,
        interactor: ResultInteractor,
        router: ResultRouter,
        resultChannel: ResultChannel,
        appStore: AppStore,
        itemActionDelegate: ResultItemActionDelegate,
        menuListenerDelegate: BottomSheetMenuListenerDelegate,
    ): ResultPresenter {
        return ResultPresenter(
            params,
            viewModel,
            resultStore,
            finderStore,
            preferenceStore,
            interactor,
            router,
            resultChannel,
            appStore,
            itemActionDelegate,
            menuListenerDelegate,
        )
    }

    @Provides
    @ResultScope
    fun resultItemActionDelegate(
        viewModel: ResultViewModel,
        router: ResultRouter,
        interactor: ResultInteractor,
        preferenceStore: PreferenceStore
    ): ResultItemActionDelegate {
        return ResultItemActionDelegate(viewModel, router, interactor, preferenceStore)
    }

    @Provides
    @ResultScope
    fun menuListenerDelegate(
        viewModel: ResultViewModel,
        interactor: ResultInteractor,
        appStore: AppStore,
    ): BottomSheetMenuListenerDelegate {
        return BottomSheetMenuListenerDelegate(viewModel, interactor, appStore)
    }

    @Provides
    @ResultScope
    fun interactor(scope: CoroutineScope, resultService: ResultService): ResultInteractor {
        return ResultInteractor(scope, resultService)
    }

    @Provides
    @ResultScope
    fun router(fragment: WeakProperty<Fragment>): ResultRouter = ResultRouter(fragment)
}

interface ResultDependencies {
    fun finderStore(): FinderStore
    fun preferenceStore(): PreferenceStore
    fun resultService(): ResultService
    fun resultStore(): ResultStore
    fun resultChannel(): ResultChannel
    fun appStore(): AppStore
}
