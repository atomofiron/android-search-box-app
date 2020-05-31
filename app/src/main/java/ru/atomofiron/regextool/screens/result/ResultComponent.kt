package ru.atomofiron.regextool.screens.result

import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import ru.atomofiron.regextool.injectable.channel.ResultChannel
import ru.atomofiron.regextool.injectable.interactor.ResultInteractor
import ru.atomofiron.regextool.injectable.service.ResultService
import ru.atomofiron.regextool.injectable.store.FinderStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.injectable.store.ResultStore
import ru.atomofiron.regextool.screens.result.presenter.BottomSheetMenuListenerDelegate
import ru.atomofiron.regextool.screens.result.presenter.ResultItemActionDelegate
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
        fun bind(activity: WeakProperty<ResultFragment>): Builder
        fun dependencies(dependencies: ResultDependencies): Builder
        fun build(): ResultComponent
    }

    fun inject(target: ResultViewModel)
    fun inject(target: ResultFragment)
}

@Module
class ResultModule {

    @Provides
    @ResultScope
    fun presenter(
            viewModel: ResultViewModel,
            scope: CoroutineScope,
            resultStore: ResultStore,
            finderStore: FinderStore,
            preferenceStore: PreferenceStore,
            interactor: ResultInteractor,
            router: ResultRouter,
            resultChannel: ResultChannel,
            itemActionDelegate: ResultItemActionDelegate,
            menuListenerDelegate: BottomSheetMenuListenerDelegate
    ): ResultPresenter {
        return ResultPresenter(viewModel, scope, resultStore, finderStore, preferenceStore, interactor, router, resultChannel, itemActionDelegate, menuListenerDelegate)
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
    fun menuListenerDelegate(viewModel: ResultViewModel, interactor: ResultInteractor): BottomSheetMenuListenerDelegate {
        return BottomSheetMenuListenerDelegate(viewModel, interactor)
    }

    @Provides
    @ResultScope
    fun scope(): CoroutineScope {
        return CoroutineScope(Job() + Dispatchers.Main.immediate)
    }

    @Provides
    @ResultScope
    fun interactor(scope: CoroutineScope, resultService: ResultService): ResultInteractor {
        return ResultInteractor(scope, resultService)
    }

    @Provides
    @ResultScope
    fun router(activity: WeakProperty<ResultFragment>): ResultRouter = ResultRouter(activity)
}

interface ResultDependencies {
    fun finderStore(): FinderStore
    fun preferenceStore(): PreferenceStore
    fun resultService(): ResultService
    fun resultStore(): ResultStore
    fun resultChannel(): ResultChannel
}
