package app.atomofiron.searchboxapp.screens.main

import androidx.fragment.app.FragmentActivity
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.service.WindowService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class MainScope

@MainScope
@Component(dependencies = [MainDependencies::class], modules = [MainModule::class])
interface MainComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(viewModel: MainViewModel): Builder
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(viewModel: WeakProperty<FragmentActivity>): Builder
        fun dependencies(dependencies: MainDependencies): Builder
        fun build(): MainComponent
    }

    fun inject(target: MainViewModel)
    fun inject(target: MainActivity)
}

@Module
class MainModule {

    @Provides
    @MainScope
    fun presenter(
        viewModel: MainViewModel,
        router: MainRouter,
        windowService: WindowService,
        appStore: AppStore,
        preferenceStore: PreferenceStore,
    ): MainPresenter {
        return MainPresenter(viewModel, router, windowService, appStore, preferenceStore)
    }

    @Provides
    @MainScope
    fun router(activity: WeakProperty<FragmentActivity>): MainRouter = MainRouter(activity)
}

interface MainDependencies {
    fun windowService(): WindowService
    fun appStore(): AppStore
    fun preferenceStore(): PreferenceStore
}
