package app.atomofiron.searchboxapp.di.module

import android.content.Context
import android.content.SharedPreferences
import app.atomofiron.searchboxapp.injectable.store.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class StoreModule {

    @Provides
    @Singleton
    open fun provideFinderStore(): FinderStore = FinderStore()

    @Provides
    @Singleton
    open fun provideResultStore(): ResultStore = ResultStore()

    @Provides
    @Singleton
    open fun provideExplorerStore(): ExplorerStore = ExplorerStore()

    @Provides
    @Singleton
    open fun provideSettingsStore(context: Context, sp: SharedPreferences): PreferenceStore = PreferenceStore(context, sp)

    @Provides
    @Singleton
    open fun provideAppStore(context: Context): AppStore = AppStore(context, context.resources)
}
