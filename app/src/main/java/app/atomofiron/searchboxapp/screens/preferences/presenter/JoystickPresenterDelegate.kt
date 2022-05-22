package app.atomofiron.searchboxapp.screens.preferences.presenter

import app.atomofiron.searchboxapp.injectable.store.util.PreferenceNode
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.screens.preferences.fragment.JoystickFragmentDelegate

class JoystickPresenterDelegate(
    private val node: PreferenceNode<JoystickComposition, Int>
) : JoystickFragmentDelegate.JoystickPreferenceOutput {
    override fun notify(composition: JoystickComposition) = node.notify(composition)

    override fun push(composition: JoystickComposition) = node.pushByEntity(composition)
}