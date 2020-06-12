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
import ru.atomofiron.regextool.screens.viewer.presenter.SearchOutputDelegate
import ru.atomofiron.regextool.screens.viewer.sheet.SearchDelegate
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
            searchOutputDelegate: SearchOutputDelegate,
            textViewerInteractor: TextViewerInteractor,
            preferenceStore: PreferenceStore,
            textViewerChannel: TextViewerChannel
    ): TextViewerPresenter {
        return TextViewerPresenter(viewModel, router, searchOutputDelegate, textViewerInteractor, preferenceStore, textViewerChannel)
    }

    @Provides
    @TextViewerScope
    fun searchOutputDelegate(
            viewModel: TextViewerViewModel,
            preferenceStore: PreferenceStore
    ): SearchOutputDelegate {
        return SearchOutputDelegate(viewModel, preferenceStore)
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
