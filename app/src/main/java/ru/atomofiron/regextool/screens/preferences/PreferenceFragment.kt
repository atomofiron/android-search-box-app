package ru.atomofiron.regextool.screens.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.fragment.BasePreferenceFragment
import app.atomofiron.common.util.Knife
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.preferences.fragment.ExplorerItemFragmentDelegate
import ru.atomofiron.regextool.screens.preferences.fragment.ExportImportFragmentDelegate
import ru.atomofiron.regextool.screens.preferences.fragment.JoystickFragmentDelegate
import ru.atomofiron.regextool.screens.preferences.fragment.PreferenceFragmentDelegate
import ru.atomofiron.regextool.utils.Shell
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetView
import javax.inject.Inject
import kotlin.reflect.KClass

class PreferenceFragment : BasePreferenceFragment<PreferenceViewModel, PreferencePresenter>() {
    override val viewModelClass: KClass<PreferenceViewModel> = PreferenceViewModel::class

    @Inject
    override lateinit var presenter: PreferencePresenter

    private lateinit var exportImportDelegate: ExportImportFragmentDelegate
    private lateinit var explorerItemDelegate: ExplorerItemFragmentDelegate
    private lateinit var joystickDelegate: JoystickFragmentDelegate
    private lateinit var preferenceDelegate: PreferenceFragmentDelegate

    private val bottomSheetView = Knife<BottomSheetView>(this, R.id.preference_bsv)

    override fun inject() = viewModel.inject(this)

    override fun onCreate() {
        preferenceDelegate = PreferenceFragmentDelegate(this, viewModel, presenter)
        addPreferencesFromResource(R.xml.preferences)

        exportImportDelegate = ExportImportFragmentDelegate(presenter)
        explorerItemDelegate = ExplorerItemFragmentDelegate(viewModel.explorerItemComposition, presenter)
        joystickDelegate = JoystickFragmentDelegate(viewModel.joystickComposition, presenter)
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

        exportImportDelegate.bottomSheetView = bottomSheetView.view
        explorerItemDelegate.bottomSheetView = bottomSheetView.view
        joystickDelegate.bottomSheetView = bottomSheetView.view
    }

    override fun onCreateRecyclerView(inflater: LayoutInflater?, parent: ViewGroup?, savedInstanceState: Bundle?): RecyclerView {
        val recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState)
        recyclerView.clipToPadding = false
        val padding = resources.getDimensionPixelSize(R.dimen.joystick_size)
        recyclerView.setPadding(recyclerView.paddingLeft, recyclerView.paddingTop, recyclerView.paddingRight, padding)
        return recyclerView
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        viewModel.alert.observeData(owner, ::showAlert)
        viewModel.alertOutputSuccess.observeData(owner, ::showOutputSuccess)
        viewModel.alertOutputError.observeData(owner, ::showOutputError)
    }

    override fun onBack(): Boolean = bottomSheetView(default = false) { hide() } || super.onBack()

    fun onExportImportClick() = exportImportDelegate.show()

    fun onExplorerItemClick() = explorerItemDelegate.show()

    fun onJoystickClick() = joystickDelegate.show()

    fun onLeakCanaryClick(isChecked: Boolean) = presenter.onLeakCanaryClick(isChecked)

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