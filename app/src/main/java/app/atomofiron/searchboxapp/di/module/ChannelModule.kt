package app.atomofiron.searchboxapp.di.module

import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.channel.ResultChannel
import javax.inject.Singleton


@Module
open class ChannelModule {

    @Provides
    @Singleton
    open fun providePreferenceChannel(): PreferenceChannel = PreferenceChannel()

    @Provides
    @Singleton
    open fun provideResultChannel(): ResultChannel = ResultChannel()

    @Provides
    @Singleton
    open fun provideCurtainChannel(): CurtainChannel = CurtainChannel()
}