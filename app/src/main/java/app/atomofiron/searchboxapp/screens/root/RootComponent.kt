package app.atomofiron.searchboxapp.screens.root

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
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
        fun bind(activity: WeakProperty<Fragment>): Builder
        fun dependencies(dependencies: RootDependencies): Builder
        fun build(): RootComponent
    }

    fun inject(target: RootViewModel)
}

@Module
class RootModule {

    @Provides
    @RootScope
    fun presenter(
        viewModel: RootViewModel,
        router: RootRouter,
        preferenceStore: PreferenceStore,
    ): RootPresenter {
        return RootPresenter(viewModel, router, preferenceStore)
    }


    @Provides
    @RootScope
    fun router(fragment: WeakProperty<Fragment>): RootRouter = RootRouter(fragment)
}

interface RootDependencies {
    fun preferenceStore(): PreferenceStore
}
