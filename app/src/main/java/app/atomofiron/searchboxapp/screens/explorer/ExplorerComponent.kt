package app.atomofiron.searchboxapp.screens.explorer

import android.content.Context
import android.content.res.AssetManager
import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.router.FileSharingDelegateImpl
import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerCurtainMenuDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerItemActionListenerDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.PlacesActionListenerDelegate
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
        fun bind(viewModel: ExplorerViewModel): Builder
        @BindsInstance
        fun bind(fragment: WeakProperty<Fragment>): Builder
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        fun dependencies(dependencies: ExplorerDependencies): Builder
        fun build(): ExplorerComponent
    }

    fun inject(target: ExplorerViewModel)
}

@Module
class ExplorerModule {
    @Provides
    @ExplorerScope
    fun itemListener(
        viewModel: ExplorerViewModel,
        menuListenerDelegate: ExplorerCurtainMenuDelegate,
        explorerStore: ExplorerStore,
        preferenceStore: PreferenceStore,
        router: ExplorerRouter,
        explorerInteractor: ExplorerInteractor,
    ): ExplorerItemActionListenerDelegate {
        return ExplorerItemActionListenerDelegate(
            viewModel,
            menuListenerDelegate,
            explorerStore,
            preferenceStore,
            router,
            explorerInteractor,
        )
    }

    @Provides
    @ExplorerScope
    fun placesListener(viewModel: ExplorerViewModel): PlacesActionListenerDelegate {
        return PlacesActionListenerDelegate(viewModel)
    }

    @Provides
    @ExplorerScope
    fun menuListener(
        viewModel: ExplorerViewModel,
        router: ExplorerRouter,
        explorerStore: ExplorerStore,
        explorerInteractor: ExplorerInteractor,
        curtainChannel: CurtainChannel,
    ): ExplorerCurtainMenuDelegate {
        return ExplorerCurtainMenuDelegate(viewModel, router, explorerStore, explorerInteractor, curtainChannel)
    }

    @Provides
    @ExplorerScope
    fun presenter(
        viewModel: ExplorerViewModel,
        router: ExplorerRouter,
        explorerStore: ExplorerStore,
        preferenceStore: PreferenceStore,
        appStore: AppStore,
        explorerInteractor: ExplorerInteractor,
        itemListener: ExplorerItemActionListenerDelegate,
        placesListener: PlacesActionListenerDelegate,
        curtainMenuDelegate: ExplorerCurtainMenuDelegate,
    ): ExplorerPresenter {
        return ExplorerPresenter(
            viewModel,
            router,
            explorerStore,
            preferenceStore,
            appStore,
            explorerInteractor,
            itemListener,
            placesListener,
            curtainMenuDelegate,
        )
    }

    @Provides
    @ExplorerScope
    fun interactor(scope: CoroutineScope, explorerService: ExplorerService): ExplorerInteractor {
        return ExplorerInteractor(scope, explorerService)
    }

    @Provides
    @ExplorerScope
    fun router(fragment: WeakProperty<Fragment>, appStore: AppStore): ExplorerRouter {
        return ExplorerRouter(fragment, FileSharingDelegateImpl(appStore))
    }
}

interface ExplorerDependencies {
    fun context(): Context
    fun assetManager(): AssetManager
    fun explorerService(): ExplorerService
    fun explorerStore(): ExplorerStore
    fun preferenceStore(): PreferenceStore
    fun appStore(): AppStore
    fun curtainChannel(): CurtainChannel
}
