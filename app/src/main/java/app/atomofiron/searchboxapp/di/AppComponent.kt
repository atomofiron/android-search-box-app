package app.atomofiron.searchboxapp.di

import android.content.Context
import android.content.res.AssetManager
import dagger.BindsInstance
import dagger.Component
import app.atomofiron.searchboxapp.android.App
import app.atomofiron.searchboxapp.android.ForegroundService
import app.atomofiron.searchboxapp.di.module.ChannelModule
import app.atomofiron.searchboxapp.di.module.CommonModule
import app.atomofiron.searchboxapp.di.module.ServiceModule
import app.atomofiron.searchboxapp.di.module.StoreModule
import app.atomofiron.searchboxapp.screens.curtain.CurtainDependencies
import app.atomofiron.searchboxapp.screens.explorer.ExplorerDependencies
import app.atomofiron.searchboxapp.screens.finder.FinderDependencies
import app.atomofiron.searchboxapp.screens.preferences.PreferenceDependencies
import app.atomofiron.searchboxapp.screens.result.ResultDependencies
import app.atomofiron.searchboxapp.screens.main.MainDependencies
import app.atomofiron.searchboxapp.screens.root.RootDependencies
import app.atomofiron.searchboxapp.screens.viewer.TextViewerDependencies
import app.atomofiron.searchboxapp.work.FinderWorker
import app.atomofiron.searchboxapp.work.NotificationWorker
import javax.inject.Singleton

@Component(modules = [
    ChannelModule::class,
    CommonModule::class,
    ServiceModule::class,
    StoreModule::class
])
@Singleton
interface AppComponent :
    MainDependencies,
    RootDependencies,
    CurtainDependencies,
    PreferenceDependencies,
    ExplorerDependencies,
    FinderDependencies,
    ResultDependencies,
    TextViewerDependencies
{

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun appContext(context: Context): Builder

        @BindsInstance
        fun assetManager(assetManager: AssetManager): Builder

        fun build(): AppComponent
    }

    fun inject(target: App)
    fun inject(target: ForegroundService)
    fun inject(target: NotificationWorker)
    fun inject(target: FinderWorker)
}
