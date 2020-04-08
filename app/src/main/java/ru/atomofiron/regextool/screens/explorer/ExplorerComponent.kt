package ru.atomofiron.regextool.screens.explorer

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import androidx.lifecycle.ViewModelProvider
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import ru.atomofiron.regextool.injectable.interactor.ExplorerInteractor
import ru.atomofiron.regextool.injectable.service.explorer.ExplorerService
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.SettingsStore
import ru.atomofiron.regextool.screens.explorer.presenter.BottomSheetMenuListenerDelegate
import ru.atomofiron.regextool.screens.explorer.presenter.ExplorerItemActionListenerDelegate
import ru.atomofiron.regextool.screens.explorer.presenter.ExplorerPresenter
import ru.atomofiron.regextool.screens.explorer.presenter.PlacesActionListenerDelegate
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

        @BindsInstance
        fun fragment(fragment: ExplorerFragment): Builder

        fun dependencies(dependencies: ExplorerDependencies): Builder

        fun build(): ExplorerComponent
    }

    fun inject(target: ExplorerFragment)
}

@Module
class ExplorerModule {
    @Provides
    @ExplorerScope
    fun viewModel(fragment: ExplorerFragment, scope: CoroutineScope): ExplorerViewModel {
        val viewModel =  ViewModelProvider(fragment.requireActivity()).get(ExplorerViewModel::class.java)
        viewModel.scope = scope
        return viewModel
    }

    @Provides
    @ExplorerScope
    fun itemListener(fragment: ExplorerFragment,
                     viewModel: ExplorerViewModel,
                     explorerStore: ExplorerStore,
                     settingsStore: SettingsStore,
                     router: ExplorerRouter,
                     explorerInteractor: ExplorerInteractor): ExplorerItemActionListenerDelegate {
        return ExplorerItemActionListenerDelegate(fragment, viewModel, explorerStore, settingsStore, router, explorerInteractor)
    }

    @Provides
    @ExplorerScope
    fun placesListener(viewModel: ExplorerViewModel): PlacesActionListenerDelegate {
        return PlacesActionListenerDelegate(viewModel)
    }

    @Provides
    @ExplorerScope
    fun menuListener(viewModel: ExplorerViewModel,
                     explorerStore: ExplorerStore,
                     explorerInteractor: ExplorerInteractor): BottomSheetMenuListenerDelegate {
        return BottomSheetMenuListenerDelegate(viewModel, explorerStore, explorerInteractor)

    }
    @Provides
    @ExplorerScope
    fun presenter(viewModel: ExplorerViewModel,
                  scope: CoroutineScope,
                  router: ExplorerRouter,
                  explorerStore: ExplorerStore,
                  settingsStore: SettingsStore,
                  explorerInteractor: ExplorerInteractor,
                  itemListener: ExplorerItemActionListenerDelegate,
                  placesListener: PlacesActionListenerDelegate,
                  menuListener: BottomSheetMenuListenerDelegate): ExplorerPresenter {
        return ExplorerPresenter(
                viewModel, scope, router, explorerStore, settingsStore, explorerInteractor,
                itemListener, placesListener, menuListener)
    }

    @Provides
    @ExplorerScope
    fun interactor(scope: CoroutineScope, explorerService: ExplorerService): ExplorerInteractor {
        return ExplorerInteractor(scope, explorerService)
    }

    @Provides
    @ExplorerScope
    fun scope(): CoroutineScope {
        return CoroutineScope(Job() + Dispatchers.Main.immediate)
    }

    @Provides
    @ExplorerScope
    fun router(fragment: ExplorerFragment): ExplorerRouter = ExplorerRouter(fragment)
}

interface ExplorerDependencies {
    fun context(): Context
    fun assetManager(): AssetManager
    fun sharedPreferences(): SharedPreferences
    fun explorerService(): ExplorerService
    fun explorerStore(): ExplorerStore
    fun settingsStore(): SettingsStore
}
