package ru.atomofiron.regextool.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.atomofiron.regextool.di.module.CommonModule
import ru.atomofiron.regextool.di.module.StoreModule
import ru.atomofiron.regextool.screens.explorer.ExplorerDependencies
import ru.atomofiron.regextool.screens.finder.FinderDependencies
import javax.inject.Singleton

@Component(modules = [
    CommonModule::class,
    StoreModule::class
])
@Singleton
abstract class AppComponent :
        FinderDependencies,
        ExplorerDependencies
{

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun appContext(context: Context): Builder

        fun build(): AppComponent
    }
}
