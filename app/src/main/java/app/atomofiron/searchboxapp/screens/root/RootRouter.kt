package app.atomofiron.searchboxapp.screens.root

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.HomeScreen
import app.atomofiron.searchboxapp.screens.explorer.ExplorerFragment
import app.atomofiron.searchboxapp.screens.finder.FinderFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RootRouter(
    property: WeakProperty<out Fragment>,
    scope: CoroutineScope,
    preferenceStore: PreferenceStore,
) : BaseRouter(property) {

    override val currentDestinationId = R.id.rootFragment

    private lateinit var homeScreen: HomeScreen

    init {
        scope.launch {
            homeScreen = preferenceStore.homeScreen.first()
            withContext(Dispatchers.Main) {
                addFragments()
            }
            preferenceStore.homeScreen.collect {
                homeScreen = it
            }
        }
    }

    fun onBack(): Boolean {
        val consumed = fragment {
            val lastVisibleFragment = childFragmentManager.fragments.findLastVisibleFragment()
            var consumed = lastVisibleFragment?.onBack() ?: false
            if (!consumed && lastVisibleFragment?.let { !isHomeFragment(it) } == true) {
                childFragmentManager.switchScreen { isHomeFragment(it) }
                consumed = true
            }
            consumed
        }
        return consumed ?: false
    }

    private fun addFragments() {
        fragment {
            if (childFragmentManager.fragments.isEmpty()) {
                val explorer = ExplorerFragment()
                val finder = FinderFragment()
                val (home, second) = when {
                    isHomeFragment(explorer) -> explorer to finder
                    else -> finder to explorer
                }
                childFragmentManager.beginTransaction()
                    .add(R.id.main_fl_root, home)
                    .add(R.id.main_fl_root, second)
                    .hide(second)
                    .commit()
            }
        }
    }

    private fun isHomeFragment(any: Any): Boolean {
        return when (homeScreen) {
            HomeScreen.Explorer -> any is ExplorerFragment
            HomeScreen.Search -> any is FinderFragment
        }
    }
}