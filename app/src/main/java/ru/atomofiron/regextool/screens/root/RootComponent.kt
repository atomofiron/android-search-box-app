package ru.atomofiron.regextool.screens.root

import dagger.Component
import dagger.Module
import ru.atomofiron.regextool.channel.RootChannel
import ru.atomofiron.regextool.iss.store.SettingsStore
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class RootScope

@RootScope
@Component(dependencies = [RootDependencies::class], modules = [RootModule::class])
interface RootComponent {
    @Component.Builder
    interface Builder {
        fun dependencies(dependencies: RootDependencies): Builder
        fun build(): RootComponent
    }

    fun inject(target: RootViewModel)
}

@Module
class RootModule

interface RootDependencies {
    fun rootChannel(): RootChannel
    fun settingsStore(): SettingsStore
}
