package ru.atomofiron.regextool.di

import android.content.Context
import android.content.res.AssetManager
import dagger.BindsInstance
import dagger.Component
import ru.atomofiron.regextool.di.module.CommonModule
import ru.atomofiron.regextool.di.module.StoreModule
import ru.atomofiron.regextool.screens.explorer.ExplorerDependencies
import ru.atomofiron.regextool.screens.finder.FinderDependencies
import ru.atomofiron.regextool.screens.preferences.PreferenceDependencies
import ru.atomofiron.regextool.screens.root.RootDependencies
import javax.inject.Singleton

@Component(modules = [
    CommonModule::class,
    StoreModule::class
])
@Singleton
abstract class AppComponent :
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
}
