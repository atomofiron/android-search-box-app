package ru.atomofiron.regextool.screens.preferences.delegate

import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.preferences.PreferenceViewModel
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetDelegate

class ExportImportDelegate(
        private val viewModel: PreferenceViewModel
) : BottomSheetDelegate(R.layout.sheet_export_import) {

    private val tvPath: TextView get() = bottomSheetView.findViewById(R.id.lei_tv_path)
    private val rgTarget: RadioGroup get() = bottomSheetView.findViewById(R.id.lei_rg_target)
    private val rgAction: RadioGroup get() = bottomSheetView.findViewById(R.id.lei_rg_action)
    private val button: Button get() = bottomSheetView.findViewById(R.id.lei_btn)

    public override fun show() = super.show()

    override fun onViewReady() {
        rgAction.setOnCheckedChangeListener { _, checkedId ->
            val id = when (checkedId) {
                R.id.lei_rb_export -> R.string.export_btn
                R.id.lei_rb_import -> R.string.import_btn
                else -> throw IllegalArgumentException()
            }
            button.setText(id)
        }
        button.setOnClickListener { onButtonClick() }
        tvPath.text = viewModel.externalPath
    }

    private fun onButtonClick() {
        when (rgAction.checkedRadioButtonId) {
            R.id.lei_rb_export -> when (rgTarget.checkedRadioButtonId) {
                R.id.lei_rb_preferences -> viewModel.exportPreferences()
                R.id.lei_rb_history -> viewModel.exportHistory()
                else -> throw Exception()
            }
            R.id.lei_rb_import -> when (rgTarget.checkedRadioButtonId) {
                R.id.lei_rb_preferences -> viewModel.importPreferences()
                R.id.lei_rb_history -> viewModel.importHistory()
                else -> throw Exception()
            }
            else -> throw Exception()
        }
        hide()
    }
}