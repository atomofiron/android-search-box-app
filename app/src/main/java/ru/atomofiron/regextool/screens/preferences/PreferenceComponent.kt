package ru.atomofiron.regextool.screens.preferences

import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.channel.PreferenceChannel
import ru.atomofiron.regextool.iss.service.PreferenceService
import ru.atomofiron.regextool.iss.store.SettingsStore
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
        fun dependencies(dependencies: PreferenceDependencies): Builder
        fun build(): PreferenceComponent
    }

    fun inject(target: PreferenceViewModel)
}

@Module
class PreferenceModule {
    @Provides
    @PreferenceScope
    fun preferenceService(context: Context): PreferenceService = PreferenceService(context)
}

interface PreferenceDependencies {
    fun preferenceChannel(): PreferenceChannel
    fun settingsStore(): SettingsStore
    fun context(): Context
}
