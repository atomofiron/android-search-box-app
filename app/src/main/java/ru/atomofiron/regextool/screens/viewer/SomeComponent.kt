package ru.atomofiron.regextool.screens.viewer

import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.injectable.interactor.TextViewerInteractor
import ru.atomofiron.regextool.injectable.service.TextViewerService
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class TextViewerScope

@TextViewerScope
@Component(dependencies = [TextViewerDependencies::class], modules = [TextViewerModule::class])
interface TextViewerComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(viewModel: TextViewerViewModel): Builder
        @BindsInstance
        fun bind(activity: WeakProperty<TextViewerFragment>): Builder
        fun dependencies(dependencies: TextViewerDependencies): Builder
        fun build(): TextViewerComponent
    }

    fun inject(target: TextViewerViewModel)
    fun inject(target: TextViewerFragment)
}

@Module
class TextViewerModule {

    @Provides
    @TextViewerScope
    fun presenter(viewModel: TextViewerViewModel, router: TextViewerRouter, textViewerService: TextViewerInteractor, preferenceStore: PreferenceStore): TextViewerPresenter {
        return TextViewerPresenter(viewModel, router, textViewerService, preferenceStore)
    }

    @Provides
    @TextViewerScope
    fun textViewerInteractor(textViewerService: TextViewerService): TextViewerInteractor = TextViewerInteractor(textViewerService)

    @Provides
    @TextViewerScope
    fun router(activity: WeakProperty<TextViewerFragment>): TextViewerRouter = TextViewerRouter(activity)
}

interface TextViewerDependencies {
    fun preferenceStore(): PreferenceStore
}
