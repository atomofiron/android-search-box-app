package ru.atomofiron.regextool.di.module

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class CommonModule {

    @Provides
    @Singleton
    open fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @Singleton
    open fun provideWorkManager(context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
