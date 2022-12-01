package app.atomofiron.searchboxapp.di.module

import android.content.Context
import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.service.ApkService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class InteractorModule {

    @Provides
    @Singleton
    fun apkInteractor(
        context: Context,
        appStore: AppStore,
        apkService: ApkService,
    ): ApkInteractor = ApkInteractor(context, appStore.scope, apkService)
}