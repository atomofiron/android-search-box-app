package ru.atomofiron.regextool.screens.preferences

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import app.atomofiron.common.base.BaseFragment
import app.atomofiron.common.util.Knife
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.preferences.delegate.*
import ru.atomofiron.regextool.utils.Shell
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetView
import kotlin.reflect.KClass

class PreferenceFragment : BaseFragment<PreferenceViewModel>() {
    override val viewModelClass: KClass<PreferenceViewModel> = PreferenceViewModel::class
    override val layoutId: Int = R.layout.fragment_preference

    private lateinit var exportImportDelegate: ExportImportDelegate
    private lateinit var explorerItemDelegate: ExplorerItemDelegate
    private lateinit var joystickDelegate: JoystickDelegate
    private lateinit var preferencesDelegate: PreferencesDelegate

    // InternalPreferenceFragment like a View
    private lateinit var childFragment: InternalPreferenceFragment

    private val bottomSheetView = Knife<BottomSheetView>(this, R.id.preference_bsv)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferencesDelegate = PreferencesDelegate(this, viewModel)
    }

    override fun onCreate() {
        if (!::childFragment.isInitialized) {
            childFragment = InternalPreferenceFragment()
        }
        exportImportDelegate = ExportImportDelegate(viewModel)
        explorerItemDelegate = ExplorerItemDelegate(viewModel)
        joystickDelegate = JoystickDelegate(viewModel.escColorNode)
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        this.childFragment = childFragment as? InternalPreferenceFragment
                ?: return

        childFragment.setAppPreferenceFragmentOutput(preferencesDelegate)
        childFragment.setAppPreferenceFragmentProvider(preferencesDelegate)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exportImportDelegate.bottomSheetView = bottomSheetView.view
        explorerItemDelegate.bottomSheetView = bottomSheetView.view
        joystickDelegate.bottomSheetView = bottomSheetView.view

        if (!childFragment.isAdded) {
            childFragmentManager.beginTransaction()
                    .add(R.id.preference_fl_container, childFragment)
                    .commit()
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        viewModel.alert.observeData(owner, ::showAlert)
        viewModel.alertOutputSuccess.observeData(owner, ::showOutputSuccess)
        viewModel.alertOutputError.observeData(owner, ::showOutputError)
    }

    override fun onBack(): Boolean = bottomSheetView(default = false) { hide() } || super.onBack()

    fun onExportImportClick() = exportImportDelegate.show()

    fun onExplorerItemClick() = explorerItemDelegate.show()

    fun onEscColorClick() = joystickDelegate.show()

    private fun showAlert(message: String) {
        Snackbar
                .make(thisView, message, Snackbar.LENGTH_SHORT)
                .setAnchorView(anchorView)
                .show()
    }

    private fun showOutputSuccess(message: Int) {
        val duration = when (message) {
            R.string.successful_with_restart -> Snackbar.LENGTH_LONG
            else -> Snackbar.LENGTH_SHORT
        }
        Snackbar.make(thisView, message, duration).setAnchorView(anchorView).show()
    }

    private fun showOutputError(output: Shell.Output) {
        Snackbar.make(thisView, R.string.error, Snackbar.LENGTH_SHORT)
                .apply {
                    if (output.error.isNotEmpty()) {
                        setAction(R.string.more) {
                            AlertDialog.Builder(context)
                                    .setMessage(output.error)
                                    .show()
                        }
                    }
                }
                .setAnchorView(anchorView)
                .show()
    }
}