package app.atomofiron.searchboxapp.screens.preferences.fragment

import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import app.atomofiron.common.util.RadioGroupImpl
import app.atomofiron.common.util.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.bottom_sheet.BottomSheetDelegate
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.Shell
import java.io.File

class ToyboxFragmentDelegate(
        toyboxVariant: ToyboxVariant,
        private val output: PreferenceUpdateOutput
) : BottomSheetDelegate(R.layout.sheet_preference_toybox) {

    private val rbPath: RadioButton get() = bottomSheetView.findViewById(R.id.preference_rb_path_to_toybox)
    private val etPath: EditText get() = bottomSheetView.findViewById(R.id.preference_et_path_to_toybox)
    private val rbToybox8664: RadioButton get() = bottomSheetView.findViewById(R.id.preference_rb_toybox_x86_64)
    private val rbToybox64: RadioButton get() = bottomSheetView.findViewById(R.id.preference_rb_toybox_64)
    private val rbToybox32: RadioButton get() = bottomSheetView.findViewById(R.id.preference_rb_toybox_32)

    private val radioGroup = RadioGroupImpl()

    private var variant = toyboxVariant.variant
    private var customPath = toyboxVariant.customPath

    private lateinit var snackbar: Snackbar

    init {
        radioGroup.onCheckedChangeListener = ::onPathChanged
    }

    private fun onPathChanged() {
        variant = when (radioGroup.checkedId) {
            R.id.preference_rb_toybox_x86_64 -> Const.VALUE_TOYBOX_X86_64
            R.id.preference_rb_toybox_32 -> Const.VALUE_TOYBOX_ARM_32
            R.id.preference_rb_toybox_64 -> Const.VALUE_TOYBOX_ARM_64
            else -> Const.VALUE_TOYBOX_CUSTOM
        }
        var cpOutput: Shell.Output? = null
        var importedPath: String? = null
        if (variant == Const.VALUE_TOYBOX_CUSTOM) {
            customPath = etPath.text.toString()
            importedPath = ToyboxVariant.getToyboxPath(etPath.context, Const.VALUE_TOYBOX_IMPORTED)

            if (!File(customPath).canExecute()) {
                val cp = Shell[Shell.CP_F, Const.EMPTY].format(customPath, importedPath)
                cpOutput = Shell.exec(cp, su = false)
                File(importedPath).setExecutable(true, true)
                customPath = importedPath
            }
        }
        when {
            cpOutput == null && test() ->
                output.onPreferenceUpdate(Const.PREF_TOYBOX, setOf(variant, customPath))
            cpOutput?.success == true && test() ->
                output.onPreferenceUpdate(Const.PREF_TOYBOX, setOf(variant, importedPath!!))
            cpOutput?.success == false ->
                snackbar.setText(cpOutput.error).show()
        }
    }

    private fun test(): Boolean {
        val version = Shell[Shell.VERSION, customPath]
        val output = Shell.exec(version, su = false)
        val works = output.error.isBlank()
        when {
            works -> snackbar.setText(output.output.trim()).show()
            else -> snackbar.setText(output.error).show()
        }
        return works
    }

    public override fun show() = super.show()

    override fun onViewReady() {
        etPath.setText(customPath)

        when (variant) {
            Const.VALUE_TOYBOX_X86_64 -> rbToybox8664.isChecked = true
            Const.VALUE_TOYBOX_ARM_32 -> rbToybox32.isChecked = true
            Const.VALUE_TOYBOX_ARM_64 -> rbToybox64.isChecked = true
            else -> rbPath.isChecked = true
        }
        radioGroup.clear()
                .syncWith(rbPath)
                .syncWith(rbToybox64)
                .syncWith(rbToybox32)
                .syncWith(rbToybox8664)

        etPath.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                rbPath.isChecked = true
                view.clearFocus()
                onPathChanged()
            }
            false
        }

        snackbar = Snackbar
                .make(bottomSheetView, "", Snackbar.LENGTH_LONG)
                .setAnchorView(bottomSheetView.anchorView)
    }

    override fun onViewHidden() = etPath.hideKeyboard()
}