package app.atomofiron.searchboxapp.screens.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.viewCollect
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.anchorView
import app.atomofiron.searchboxapp.screens.preferences.fragment.*
import app.atomofiron.searchboxapp.utils.PreferenceKeys
import app.atomofiron.searchboxapp.utils.Shell
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets
import lib.atomofiron.android_window_insets_compat.insetsProxying

class PreferenceFragment : PreferenceFragmentCompat(),
    BaseFragment<PreferenceFragment, PreferenceViewModel, PreferencePresenter> by BaseFragmentImpl()
{
    private lateinit var preferenceDelegate: PreferenceFragmentDelegate

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        initViewModel(this, PreferenceViewModel::class, savedInstanceState)

        preferenceManager.preferenceDataStore = viewModel.preferenceDataStore
        preferenceDelegate = PreferenceFragmentDelegate(this, viewModel, presenter)
        setPreferencesFromResource(R.xml.preferences, rootKey)
        preferenceDelegate.onCreatePreference(preferenceScreen)

        val deepBlack = findPreference<Preference>(PreferenceKeys.KeyDeepBlack.name)!!
        viewModel.showDeepBlack.collect(lifecycleScope) {
            deepBlack.isVisible = it
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val container = inflater.inflate(R.layout.fragment_preference, container, false)
        container as ViewGroup
        val view = super.onCreateView(inflater, container, savedInstanceState)
        container.addView(view, 0)
        return container
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.insetsProxying()
        view.setBackgroundColor(view.context.findColorByAttr(R.attr.colorBackground))
        preferenceScreen.fixIcons()
        viewModel.onViewCollect()
        onApplyInsets(view)
    }

    override fun onCreateRecyclerView(inflater: LayoutInflater, parent: ViewGroup, savedInstanceState: Bundle?): RecyclerView {
        val recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState)
        recyclerView.clipToPadding = false
        val padding = resources.getDimensionPixelSize(R.dimen.joystick_size)
        recyclerView.updatePadding(bottom = padding)
        recyclerView.applyPaddingInsets()
        return recyclerView
    }

    override fun PreferenceViewModel.onViewCollect() {
        viewCollect(alert, collector = ::showAlert)
        viewCollect(alertOutputSuccess, collector = ::showOutputSuccess)
        viewCollect(alertOutputError, collector = ::showOutputError)
    }

    override fun onApplyInsets(root: View) {
        root.insetsProxying()
    }

    private fun PreferenceGroup.fixIcons() {
        // todo foresee NoticeableDrawable and colored icons
        val iconTint = requireContext().findColorByAttr(R.attr.colorControlNormal)
        forEach {
            it.icon?.setTint(iconTint)
            if (it is PreferenceGroup) it.fixIcons()
        }
    }

    private fun showAlert(message: String) {
        val view = view ?: return
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
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
        Snackbar.make(view, R.string.error, Snackbar.LENGTH_SHORT).apply {
            if (output.error.isNotEmpty()) {
                setAction(R.string.more) {
                    AlertDialog.Builder(context)
                            .setMessage(output.error)
                            .show()
                }
            }
            anchorView = anchorView
            show()
        }
    }
}