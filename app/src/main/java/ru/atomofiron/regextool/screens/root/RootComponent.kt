package ru.atomofiron.regextool.screens.root

import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.injectable.channel.RootChannel
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class RootScope

@RootScope
@Component(dependencies = [RootDependencies::class], modules = [RootModule::class])
interface RootComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(viewModel: RootViewModel): Builder
        @BindsInstance
        fun bind(activity: WeakProperty<RootActivity>): Builder
        fun dependencies(dependencies: RootDependencies): Builder
        fun build(): RootComponent
    }

    fun inject(target: RootViewModel)
    fun inject(target: RootActivity)
}

@Module
class RootModule {

    @Provides
    @RootScope
    fun presenter(viewModel: RootViewModel, router: RootRouter, rootChannel: RootChannel, preferenceStore: PreferenceStore): RootPresenter {
        return RootPresenter(viewModel, router, rootChannel, preferenceStore)
    }

    @Provides
    @RootScope
    fun router(activity: WeakProperty<RootActivity>): RootRouter = RootRouter(activity)
}

interface RootDependencies {
    fun rootChannel(): RootChannel
    fun preferenceStore(): PreferenceStore
}
