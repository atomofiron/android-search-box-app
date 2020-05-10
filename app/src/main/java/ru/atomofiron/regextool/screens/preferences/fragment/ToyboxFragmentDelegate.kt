package ru.atomofiron.regextool.screens.preferences.fragment

import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import app.atomofiron.common.util.RadioGroupImpl
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.bottom_sheet.BottomSheetDelegate
import ru.atomofiron.regextool.model.preference.ToyboxVariant
import ru.atomofiron.regextool.utils.Const

class ToyboxFragmentDelegate(
        toyboxVariant: ToyboxVariant,
        private val output: PreferenceUpdateOutput
) : BottomSheetDelegate(R.layout.sheet_preference_toybox) {

    private val rbPath: RadioButton get() = bottomSheetView.findViewById(R.id.preference_rb_path_to_toybox)
    private val etPath: EditText get() = bottomSheetView.findViewById(R.id.preference_et_path_to_toybox)
    private val rbToybox64: RadioButton get() = bottomSheetView.findViewById(R.id.preference_rb_toybox_64)
    private val rbToybox32: RadioButton get() = bottomSheetView.findViewById(R.id.preference_rb_toybox_32)

    private val radioGroup = RadioGroupImpl()

    private var variant = toyboxVariant.variant
    private var customPath = toyboxVariant.customPath

    init {
        radioGroup.onCheckedChangeListener = ::onPathChanged
    }

    private fun onPathChanged() {
        variant = when (radioGroup.checkedId) {
            R.id.preference_rb_toybox_32 -> Const.VALUE_TOYBOX_ARM_32
            R.id.preference_rb_toybox_64 -> Const.VALUE_TOYBOX_ARM_64
            else -> Const.VALUE_TOYBOX_CUSTOM
        }
        customPath = etPath.text.toString()
        output.onPreferenceUpdate(Const.PREF_TOYBOX, setOf(variant, customPath))
    }

    public override fun show() = super.show()

    override fun onViewReady() {
        etPath.setText(customPath)

        when (variant) {
            Const.VALUE_TOYBOX_ARM_32 -> rbToybox32.isChecked = true
            Const.VALUE_TOYBOX_ARM_64 -> rbToybox64.isChecked = true
            else -> rbPath.isChecked = true
        }
        radioGroup.clear()
                .syncWith(rbPath)
                .syncWith(rbToybox64)
                .syncWith(rbToybox32)

        etPath.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                rbPath.isChecked = true
                view.clearFocus()
                onPathChanged()
            }
            false
        }
    }
}