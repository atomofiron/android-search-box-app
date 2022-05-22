package app.atomofiron.searchboxapp.screens.curtain

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainPresenterParams
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class CurtainScope

@CurtainScope
@Component(dependencies = [CurtainDependencies::class], modules = [CurtainModule::class])
interface CurtainComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(viewModel: CurtainViewModel): Builder
        @BindsInstance
        fun bind(fragment: WeakProperty<Fragment>): Builder
        @BindsInstance
        fun bind(params: CurtainPresenterParams): Builder
        fun dependencies(dependencies: CurtainDependencies): Builder
        fun build(): CurtainComponent
    }

    fun inject(target: CurtainViewModel)
}

@Module
class CurtainModule {

    @Provides
    @CurtainScope
    fun presenter(
        params: CurtainPresenterParams,
        viewModel: CurtainViewModel,
        router: CurtainRouter,
        channel: CurtainChannel,
    ): CurtainPresenter {
        return CurtainPresenter(params, viewModel, router, channel)
    }

    @Provides
    @CurtainScope
    fun router(fragment: WeakProperty<Fragment>): CurtainRouter = CurtainRouter(fragment)
}

interface CurtainDependencies {
    fun curtainChannel(): CurtainChannel
}
