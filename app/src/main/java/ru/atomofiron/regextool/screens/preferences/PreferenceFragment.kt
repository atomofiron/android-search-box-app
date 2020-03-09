package ru.atomofiron.regextool.screens.preferences

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import app.atomofiron.common.base.BaseFragment
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.utils.Shell
import kotlin.reflect.KClass

class PreferenceFragment : BaseFragment<PreferenceViewModel>(), InternalPreferenceFragment.Output {
    override val viewModelClass: KClass<PreferenceViewModel> = PreferenceViewModel::class
    override val layoutId: Int = R.layout.fragment_preference

    private lateinit var exportImportDelegate: ExportImportDelegate

    // InternalPreferenceFragment like a View
    private val childFragment = InternalPreferenceFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragment.setAppPreferenceFragmentOutput(this)
        childFragment.setAppPreferenceFragmentProvider(object : InternalPreferenceFragment.Provider {
            override val isExportImportAvailable: Boolean get() = viewModel.isExportImportAvailable
            override fun getCurrentValue(key: String): Any? = viewModel.getCurrentValue(key)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exportImportDelegate = ExportImportDelegate(view as ViewGroup, viewModel)

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

    override fun onBack(): Boolean = exportImportDelegate.hide() || super.onBack()

    override fun onExportImportClick() = exportImportDelegate.show()

    override fun onPreferenceUpdate(key: String, value: Int): Boolean = viewModel.onPreferenceUpdate(key, value)

    override fun onPreferenceUpdate(key: String, value: String): Boolean = viewModel.onPreferenceUpdate(key, value)

    override fun onPreferenceUpdate(key: String, value: Boolean): Boolean = viewModel.onPreferenceUpdate(key, value)

    private fun showAlert(message: String) {
        Snackbar
                .make(theView, message, Snackbar.LENGTH_SHORT)
                .setAnchorView(anchorView)
                .show()
    }

    private fun showOutputSuccess(message: Int) {
        val duration = when (message) {
            R.string.successful_with_restart -> Snackbar.LENGTH_LONG
            else -> Snackbar.LENGTH_SHORT
        }
        Snackbar.make(theView, message, duration).setAnchorView(anchorView).show()
    }

    private fun showOutputError(output: Shell.Output) {
        Snackbar.make(theView, R.string.error, Snackbar.LENGTH_SHORT)
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