package app.atomofiron.searchboxapp.screens.preferences

import android.content.Context
import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.service.PreferenceService
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.preferences.presenter.ExportImportPresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.JoystickPresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.PreferenceUpdatePresenterDelegate
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
        fun bind(fragment: WeakProperty<Fragment>): Builder
        fun dependencies(dependencies: PreferenceDependencies): Builder
        fun build(): PreferenceComponent
    }

    fun inject(target: PreferenceViewModel)
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
            viewModel: PreferenceViewModel,
            router: PreferenceRouter,
            joystickDelegate: JoystickPresenterDelegate,
            exportImportDelegate: ExportImportPresenterDelegate,
            preferenceUpdateDelegate: PreferenceUpdatePresenterDelegate
    ): PreferencePresenter {
        return PreferencePresenter(viewModel, router, joystickDelegate, exportImportDelegate, preferenceUpdateDelegate)
    }

    @Provides
    @PreferenceScope
    fun preferenceService(context: Context, preferenceStore: PreferenceStore): PreferenceService {
        return PreferenceService(context, preferenceStore)
    }

    @Provides
    @PreferenceScope
    fun router(fragment: WeakProperty<Fragment>): PreferenceRouter = PreferenceRouter(fragment)
}

interface PreferenceDependencies {
    fun preferenceChannel(): PreferenceChannel
    fun preferenceStore(): PreferenceStore
    fun context(): Context
}
