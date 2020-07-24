package ru.atomofiron.regextool.screens.preferences

import android.content.Context
import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import ru.atomofiron.regextool.injectable.channel.PreferenceChannel
import ru.atomofiron.regextool.injectable.service.PreferenceService
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.screens.preferences.presenter.ExportImportPresenterDelegate
import ru.atomofiron.regextool.screens.preferences.presenter.JoystickPresenterDelegate
import ru.atomofiron.regextool.screens.preferences.presenter.PreferenceUpdatePresenterDelegate
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class PreferenceScope

@PreferenceScope
@Component(dependencies = [PreferenceDependencies::class], modules = [PreferenceModule::class])
interface PreferenceComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(viewModel: PreferenceViewModel): Builder
        @BindsInstance
        fun bind(fragment: WeakProperty<PreferenceFragment>): Builder
        fun dependencies(dependencies: PreferenceDependencies): Builder
        fun build(): PreferenceComponent
    }

    fun inject(target: PreferenceViewModel)
    fun inject(target: PreferenceFragment)
}

@Module
class PreferenceModule {
    @Provides
    @PreferenceScope
    fun preferenceUpdatePresenterDelegate(
            context: Context, viewModel: PreferenceViewModel, preferenceStore: PreferenceStore
    ): PreferenceUpdatePresenterDelegate {
        return PreferenceUpdatePresenterDelegate(context, viewModel, preferenceStore)
    }

    @Provides
    @PreferenceScope
    fun joystickPreferenceDelegate(preferenceStore: PreferenceStore): JoystickPresenterDelegate {
        return JoystickPresenterDelegate(preferenceStore.joystickComposition)
    }

    @Provides
    @PreferenceScope
    fun exportImportPresenterDelegate(
            context: Context,
            viewModel: PreferenceViewModel,
            preferenceService: PreferenceService,
            preferenceChannel: PreferenceChannel
    ): ExportImportPresenterDelegate {
        return ExportImportPresenterDelegate(context, viewModel, preferenceService, preferenceChannel)
    }

    @Provides
    @PreferenceScope
    fun presenter(
            scope: CoroutineScope,
            viewModel: PreferenceViewModel,
            router: PreferenceRouter,
            joystickDelegate: JoystickPresenterDelegate,
            exportImportDelegate: ExportImportPresenterDelegate,
            preferenceUpdateDelegate: PreferenceUpdatePresenterDelegate
    ): PreferencePresenter {
        return PreferencePresenter(scope, viewModel, router, joystickDelegate, exportImportDelegate, preferenceUpdateDelegate)
    }

    @Provides
    @PreferenceScope
    fun scope(): CoroutineScope {
        return CoroutineScope(Job() + Dispatchers.Main.immediate)
    }

    @Provides
    @PreferenceScope
    fun preferenceService(context: Context, preferenceStore: PreferenceStore): PreferenceService {
        return PreferenceService(context, preferenceStore)
    }

    @Provides
    @PreferenceScope
    fun router(fragment: WeakProperty<PreferenceFragment>): PreferenceRouter = PreferenceRouter(fragment)
}

interface PreferenceDependencies {
    fun preferenceChannel(): PreferenceChannel
    fun preferenceStore(): PreferenceStore
    fun context(): Context
}
