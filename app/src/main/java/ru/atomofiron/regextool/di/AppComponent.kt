package ru.atomofiron.regextool.di

import android.content.Context
import android.content.res.AssetManager
import dagger.BindsInstance
import dagger.Component
import ru.atomofiron.regextool.android.ForegroundService
import ru.atomofiron.regextool.di.module.ChannelModule
import ru.atomofiron.regextool.di.module.CommonModule
import ru.atomofiron.regextool.di.module.ServiceModule
import ru.atomofiron.regextool.di.module.StoreModule
import ru.atomofiron.regextool.screens.explorer.ExplorerDependencies
import ru.atomofiron.regextool.screens.finder.FinderDependencies
import ru.atomofiron.regextool.screens.preferences.PreferenceDependencies
import ru.atomofiron.regextool.screens.result.ResultDependencies
import ru.atomofiron.regextool.screens.root.RootDependencies
import ru.atomofiron.regextool.work.FinderWorker
import ru.atomofiron.regextool.work.NotificationWorker
import javax.inject.Singleton

@Component(modules = [
    ChannelModule::class,
    CommonModule::class,
    ServiceModule::class,
    StoreModule::class
])
@Singleton
interface AppComponent :
        ResultDependencies,
        FinderDependencies,
        ExplorerDependencies,
        PreferenceDependencies,
        RootDependencies
{

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun appContext(context: Context): Builder

        @BindsInstance
        fun assetManager(assetManager: AssetManager): Builder

        fun build(): AppComponent
    }

    fun inject(target: ForegroundService)
    fun inject(target: NotificationWorker)
    fun inject(target: FinderWorker)
}
