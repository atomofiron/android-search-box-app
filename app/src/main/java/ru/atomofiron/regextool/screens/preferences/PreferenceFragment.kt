package ru.atomofiron.regextool.screens.preferences

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import app.atomofiron.common.base.BaseFragment
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.channel.PreferencesChannel
import kotlin.reflect.KClass

class PreferenceFragment : BaseFragment<PreferenceViewModel>(), AppPreferenceFragment.Output {
    override val viewModelClass: KClass<PreferenceViewModel> = PreferenceViewModel::class
    override val layoutId: Int = R.layout.fragment_preference

    private lateinit var exportImportDelegate: ExportImportDelegate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exportImportDelegate = ExportImportDelegate(view as ViewGroup, anchorView)
        exportImportDelegate.onImportHistoryListener = {
            PreferencesChannel.historyImportedEvent.justNotify()
        }

        val childFragment = parentFragmentManager.fragments.findLast { it is AppPreferenceFragment }
        childFragment as AppPreferenceFragment
        childFragment.setAppPreferenceFragmentOutput(this)
    }

    override fun onBack(): Boolean = exportImportDelegate.hide() || super.onBack()

    override fun onExportImportClick() = exportImportDelegate.show()
}