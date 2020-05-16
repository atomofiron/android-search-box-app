package ru.atomofiron.regextool.screens.preferences.fragment

import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import app.atomofiron.common.util.RadioGroupImpl
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.bottom_sheet.BottomSheetDelegate
import ru.atomofiron.regextool.model.preference.ToyboxVariant
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Shell

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
        customPath = etPath.text.toString()
        output.onPreferenceUpdate(Const.PREF_TOYBOX, setOf(variant, customPath))
        test()
    }

    private fun test() {
        val output = Shell.exec(Shell.VERSION, su = false)
        when {
            !output.error.isBlank() -> snackbar.setText(output.error)
            else -> snackbar.setText(output.output.trim())
        }
        snackbar.show()
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
                .make(bottomSheetView, "", 1000)
                .setAnchorView(bottomSheetView.anchorView)
    }
}