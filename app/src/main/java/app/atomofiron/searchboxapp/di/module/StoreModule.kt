package app.atomofiron.searchboxapp.di.module

import android.content.Context
import android.content.SharedPreferences
import app.atomofiron.searchboxapp.injectable.store.*
import app.atomofiron.searchboxapp.utils.AppWatcherProxy
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
open class StoreModule {

    @Provides
    @Singleton
    open fun provideFinderStore(scope: CoroutineScope): FinderStore = FinderStore(scope)

    @Provides
    @Singleton
    open fun provideResultStore(): ResultStore = ResultStore()

    @Provides
    @Singleton
    open fun provideExplorerStore(): ExplorerStore = ExplorerStore()

    @Provides
    @Singleton
    open fun provideSettingsStore(
        context: Context,
        sp: SharedPreferences,
        watcher: AppWatcherProxy,
    ): PreferenceStore = PreferenceStore(context, sp, watcher)

    @Provides
    @Singleton
    open fun provideAppStore(context: Context, scope: CoroutineScope): AppStore {
        return AppStore(context, scope, context.resources)
    }

    @Provides
    @Singleton
    open fun providePreferencesStore(context: Context, scope: CoroutineScope): PreferencesStore {
        return PreferencesStore(context, scope)
    }
}
