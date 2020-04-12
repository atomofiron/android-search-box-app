package ru.atomofiron.regextool.screens.preferences

import android.content.Context
import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.injectable.channel.PreferenceChannel
import ru.atomofiron.regextool.injectable.service.PreferenceService
import ru.atomofiron.regextool.injectable.store.SettingsStore
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
            context: Context, viewModel: PreferenceViewModel, settingsStore: SettingsStore
    ): PreferenceUpdatePresenterDelegate {
        return PreferenceUpdatePresenterDelegate(context, viewModel, settingsStore)
    }

    @Provides
    @PreferenceScope
    fun joystickPreferenceDelegate(settingsStore: SettingsStore): JoystickPresenterDelegate {
        return JoystickPresenterDelegate(settingsStore.joystickComposition)
    }

    @Provides
    @PreferenceScope
    fun exportImportPresenterDelegate(
            context: Context, viewModel: PreferenceViewModel, preferenceService: PreferenceService, preferenceChannel: PreferenceChannel
    ): ExportImportPresenterDelegate {
        return ExportImportPresenterDelegate(context, viewModel, preferenceService, preferenceChannel)
    }

    @Provides
    @PreferenceScope
    fun presenter(
            viewModel: PreferenceViewModel, router: PreferenceRouter, joystickDelegate: JoystickPresenterDelegate,
            exportImportDelegate: ExportImportPresenterDelegate, preferenceUpdateDelegate: PreferenceUpdatePresenterDelegate
    ): PreferencePresenter {
        return PreferencePresenter(viewModel, router, joystickDelegate, exportImportDelegate, preferenceUpdateDelegate)
    }

    @Provides
    @PreferenceScope
    fun preferenceService(context: Context): PreferenceService = PreferenceService(context)

    @Provides
    @PreferenceScope
    fun router(fragment: WeakProperty<PreferenceFragment>): PreferenceRouter = PreferenceRouter(fragment)
}

interface PreferenceDependencies {
    fun preferenceChannel(): PreferenceChannel
    fun settingsStore(): SettingsStore
    fun context(): Context
}
