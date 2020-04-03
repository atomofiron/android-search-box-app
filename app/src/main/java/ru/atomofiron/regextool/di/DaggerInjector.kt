package ru.atomofiron.regextool.di

import android.content.Context

object DaggerInjector {

    lateinit var appComponent: AppComponent

    fun init(context: Context) {
        appComponent = DaggerAppComponent
                .builder()
                .appContext(context)
                .assetManager(context.assets)
                .build()
    }
}
