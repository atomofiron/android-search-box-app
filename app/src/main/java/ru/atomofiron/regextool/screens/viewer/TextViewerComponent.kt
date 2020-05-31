package ru.atomofiron.regextool.screens.viewer

import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import ru.atomofiron.regextool.injectable.channel.TextViewerChannel
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
    fun presenter(
            viewModel: TextViewerViewModel,
            router: TextViewerRouter,
            textViewerInteractor: TextViewerInteractor,
            textViewerChannel: TextViewerChannel
    ): TextViewerPresenter {
        return TextViewerPresenter(viewModel, router, textViewerInteractor, textViewerChannel)
    }

    @Provides
    @TextViewerScope
    fun textViewerService(
            textViewerChannel: TextViewerChannel,
            preferenceStore: PreferenceStore
    ): TextViewerService = TextViewerService(textViewerChannel, preferenceStore)

    @Provides
    @TextViewerScope
    fun textViewerInteractor(
            scope: CoroutineScope,
            textViewerService: TextViewerService
    ): TextViewerInteractor = TextViewerInteractor(scope, textViewerService)

    @Provides
    @TextViewerScope
    fun textViewerChannel(): TextViewerChannel = TextViewerChannel()

    @Provides
    @TextViewerScope
    fun router(activity: WeakProperty<TextViewerFragment>): TextViewerRouter = TextViewerRouter(activity)

    @Provides
    @TextViewerScope
    fun scope(): CoroutineScope {
        return CoroutineScope(Job() + Dispatchers.Main.immediate)
    }
}

interface TextViewerDependencies {
    fun preferenceStore(): PreferenceStore
}
