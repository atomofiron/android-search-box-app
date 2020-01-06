package ru.atomofiron.regextool

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        val context: Context get() = appContext
        private lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext
    }
}