package app.atomofiron.searchboxapp.screens.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.updatePadding
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.common.util.insets.ViewGroupInsetsProxy
import app.atomofiron.common.util.insets.ViewInsetsController
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.anchorView
import app.atomofiron.searchboxapp.databinding.FragmentPreferenceBinding
import app.atomofiron.searchboxapp.screens.preferences.fragment.*
import app.atomofiron.searchboxapp.utils.Shell

class PreferenceFragment : PreferenceFragmentCompat(),
    BaseFragment<PreferenceFragment, PreferenceViewModel, PreferencePresenter> by BaseFragmentImpl()
{
    private lateinit var exportImportDelegate: ExportImportFragmentDelegate
    private lateinit var explorerItemDelegate: ExplorerItemFragmentDelegate
    private lateinit var joystickDelegate: JoystickFragmentDelegate
    private lateinit var toyboxDelegate: ToyboxFragmentDelegate
    private lateinit var aboutDelegate: AboutFragmentDelegate
    private lateinit var preferenceDelegate: PreferenceFragmentDelegate

    private lateinit var binding: FragmentPreferenceBinding

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        initViewModel(this, PreferenceViewModel::class, savedInstanceState)

        preferenceDelegate = PreferenceFragmentDelegate(this, viewModel, presenter)
        setPreferencesFromResource(R.xml.preferences, rootKey)

        exportImportDelegate = ExportImportFragmentDelegate(presenter)
        explorerItemDelegate = ExplorerItemFragmentDelegate(viewModel.explorerItemComposition, presenter)
        joystickDelegate = JoystickFragmentDelegate(viewModel.joystickComposition, presenter)
        toyboxDelegate = ToyboxFragmentDelegate(viewModel.toyboxVariant, presenter)
        aboutDelegate = AboutFragmentDelegate()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val viewGroup = inflater.inflate(R.layout.fragment_preference, container, false) as ViewGroup
        viewGroup.addView(view, 0)
        return viewGroup
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        preferenceDelegate.onUpdateScreen(preferenceScreen)
        return super.onCreateAdapter(preferenceScreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPreferenceBinding.bind(view)

        exportImportDelegate.bottomSheetView = binding.bottomSheet
        explorerItemDelegate.bottomSheetView = binding.bottomSheet
        joystickDelegate.bottomSheetView = binding.bottomSheet
        toyboxDelegate.bottomSheetView = binding.bottomSheet
        aboutDelegate.bottomSheetView = binding.bottomSheet

        viewModel.onViewCollect()
        onApplyInsets(view)
    }

    override fun onCreateRecyclerView(inflater: LayoutInflater, parent: ViewGroup, savedInstanceState: Bundle?): RecyclerView {
        val recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState)
        recyclerView.clipToPadding = false
        val padding = resources.getDimensionPixelSize(R.dimen.joystick_size)
        recyclerView.updatePadding(bottom = padding)
        ViewInsetsController.bindPadding(recyclerView, start = true, top = true, end = true, bottom = true)
        return recyclerView
    }

    override fun PreferenceViewModel.onViewCollect() {
        viewCollect(alert, collector = ::showAlert)
        viewCollect(alertOutputSuccess, collector = ::showOutputSuccess)
        viewCollect(alertOutputError, collector = ::showOutputError)
    }

    override fun onApplyInsets(root: View) {
        ViewGroupInsetsProxy.set(root)
        ViewGroupInsetsProxy.set(binding.bottomSheet)
    }

    override fun onBack(): Boolean = binding.bottomSheet.hide() || super.onBack()

    fun onAboutClick() = aboutDelegate.show()

    fun onExportImportClick() = exportImportDelegate.show()

    fun onExplorerItemClick() = explorerItemDelegate.show()

    fun onJoystickClick() = joystickDelegate.show()

    fun onToyboxClick() = toyboxDelegate.show()

    fun onLeakCanaryClick(isChecked: Boolean) = presenter.onLeakCanaryClick(isChecked)

    private fun showAlert(message: String) {
        val view = view ?: return
        Snackbar
                .make(view, message, Snackbar.LENGTH_SHORT)
                .setAnchorView(anchorView)
                .show()
    }

    private fun showOutputSuccess(message: Int) {
        val view = view ?: return
        val duration = when (message) {
            R.string.successful_with_restart -> Snackbar.LENGTH_LONG
            else -> Snackbar.LENGTH_SHORT
        }
        Snackbar.make(view, message, duration).setAnchorView(anchorView).show()
    }

    private fun showOutputError(output: Shell.Output) {
        val view = view ?: return
        Snackbar.make(view, R.string.error, Snackbar.LENGTH_SHORT)
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