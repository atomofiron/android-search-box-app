package app.atomofiron.common.util.navigation

import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavHostController
import androidx.navigation.fragment.DialogFragmentNavigator

class CustomNavHostFragment : NavHostFragment() {

    override fun onCreateNavHostController(navHostController: NavHostController) {
        super.onCreateNavHostController(navHostController)
        val provider = navController.navigatorProvider
        provider.addNavigator(DialogFragmentNavigator(requireContext(), childFragmentManager))
        provider.addNavigator(CustomFragmentNavigator(requireContext(), childFragmentManager, id))
    }
}