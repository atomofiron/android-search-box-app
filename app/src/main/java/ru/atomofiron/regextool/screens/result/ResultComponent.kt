package ru.atomofiron.regextool.screens.result

import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import ru.atomofiron.regextool.injectable.channel.FinderStore
import ru.atomofiron.regextool.injectable.interactor.ResultInteractor
import ru.atomofiron.regextool.injectable.service.FinderService
import ru.atomofiron.regextool.injectable.store.PreferenceStore
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
            finderStore: FinderStore,
            preferenceStore: PreferenceStore,
            interactor: ResultInteractor,
            router: ResultRouter,
            itemActionDelegate: ResultItemActionDelegate
    ): ResultPresenter {
        return ResultPresenter(viewModel, scope, finderStore, preferenceStore, interactor, router, itemActionDelegate)
    }

    @Provides
    @ResultScope
    fun resultItemActionDelegate(): ResultItemActionDelegate = ResultItemActionDelegate()

    @Provides
    @ResultScope
    fun scope(): CoroutineScope {
        return CoroutineScope(Job() + Dispatchers.Main.immediate)
    }

    @Provides
    @ResultScope
    fun interactor(finderService: FinderService): ResultInteractor = ResultInteractor(finderService)

    @Provides
    @ResultScope
    fun router(activity: WeakProperty<ResultFragment>): ResultRouter = ResultRouter(activity)
}

interface ResultDependencies {
    fun finderStore(): FinderStore
    fun preferenceStore(): PreferenceStore
    fun finderService(): FinderService
}
