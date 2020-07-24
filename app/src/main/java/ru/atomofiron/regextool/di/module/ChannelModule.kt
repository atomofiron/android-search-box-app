package ru.atomofiron.regextool.di.module

import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.injectable.channel.PreferenceChannel
import ru.atomofiron.regextool.injectable.channel.ResultChannel
import javax.inject.Singleton


@Module
open class ChannelModule {

    @Provides
    @Singleton
    open fun providePreferenceChannel(): PreferenceChannel = PreferenceChannel()

    @Provides
    @Singleton
    open fun provideResultChannel(): ResultChannel = ResultChannel()
}