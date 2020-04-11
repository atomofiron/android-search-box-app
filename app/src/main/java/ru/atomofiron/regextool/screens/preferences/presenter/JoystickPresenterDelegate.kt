package ru.atomofiron.regextool.screens.preferences.presenter

import ru.atomofiron.regextool.injectable.store.util.PreferenceNode
import ru.atomofiron.regextool.model.JoystickComposition
import ru.atomofiron.regextool.screens.preferences.fragment.JoystickFragmentDelegate

class JoystickPresenterDelegate(
        private val node: PreferenceNode<JoystickComposition, Int>
) : JoystickFragmentDelegate.JoystickPreferenceOutput {
    override fun notify(composition: JoystickComposition) = node.notify(composition)

    override fun push(composition: JoystickComposition) = node.push(composition)
}