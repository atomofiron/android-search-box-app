package ru.atomofiron.regextool.di.module

import android.content.SharedPreferences
import android.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.App

@Module
open class CommonModule {

    @Provides
    open fun provideSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(App.context)
    }
}
