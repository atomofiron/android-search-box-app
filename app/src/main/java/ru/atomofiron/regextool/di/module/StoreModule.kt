package ru.atomofiron.regextool.di.module

import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.iss.store.ExplorerStore

@Module
open class StoreModule {

    @Provides
    open fun provideExplorerStore(): ExplorerStore = ExplorerStore()
}
