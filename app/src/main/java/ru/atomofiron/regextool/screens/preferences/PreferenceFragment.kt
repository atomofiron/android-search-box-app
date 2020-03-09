package ru.atomofiron.regextool.screens.preferences

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import app.atomofiron.common.base.BaseFragment
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.channel.PreferencesChannel
import kotlin.reflect.KClass

class PreferenceFragment : BaseFragment<PreferenceViewModel>(), InternalPreferenceFragment.Output, InternalPreferenceFragment.Provider {
    override val viewModelClass: KClass<PreferenceViewModel> = PreferenceViewModel::class
    override val layoutId: Int = R.layout.fragment_preference

    private lateinit var exportImportDelegate: ExportImportDelegate

    // InternalPreferenceFragment like a View
    private val childFragment = InternalPreferenceFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragment.setAppPreferenceFragmentOutput(this)
        childFragment.setAppPreferenceFragmentProvider(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exportImportDelegate = ExportImportDelegate(view as ViewGroup, anchorView)
        exportImportDelegate.onImportHistoryListener = {
            PreferencesChannel.historyImportedEvent.justNotify()
        }

        if (!childFragment.isAdded) {
            childFragmentManager.beginTransaction()
                    .add(R.id.preference_fl_container, childFragment)
                    .commit()
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        viewModel.warning.observeData(owner) {
            Snackbar
                    .make(theView, it, Snackbar.LENGTH_SHORT)
                    .setAnchorView(anchorView)
                    .show()
        }
    }

    override fun onBack(): Boolean = exportImportDelegate.hide() || super.onBack()

    override fun onExportImportClick() = exportImportDelegate.show()

    override fun onPreferenceUpdate(key: String, value: Int): Boolean = viewModel.onPreferenceUpdate(key, value)

    override fun onPreferenceUpdate(key: String, value: String): Boolean = viewModel.onPreferenceUpdate(key, value)

    override fun onPreferenceUpdate(key: String, value: Boolean): Boolean = viewModel.onPreferenceUpdate(key, value)

    override fun getCurrentValue(key: String): Any? = viewModel.getCurrentValue(key)
}