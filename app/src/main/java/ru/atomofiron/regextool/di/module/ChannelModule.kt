package ru.atomofiron.regextool.di.module

import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.injectable.channel.PreferenceChannel
import ru.atomofiron.regextool.injectable.channel.RootChannel
import javax.inject.Singleton


@Module
open class ChannelModule {

    @Provides
    @Singleton
    open fun provideRootChannel(): RootChannel = RootChannel()

    @Provides
    @Singleton
    open fun providePreferenceChannel(): PreferenceChannel = PreferenceChannel()
}