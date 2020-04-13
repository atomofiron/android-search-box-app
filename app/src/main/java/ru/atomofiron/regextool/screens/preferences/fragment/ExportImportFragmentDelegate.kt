package ru.atomofiron.regextool.screens.preferences.fragment

import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.bottom_sheet.BottomSheetDelegate

class ExportImportFragmentDelegate(
        private val output: ExportImportOutput
) : BottomSheetDelegate(R.layout.sheet_preference_export_import) {

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
        tvPath.text = output.externalPath
    }

    private fun onButtonClick() {
        when (rgAction.checkedRadioButtonId) {
            R.id.lei_rb_export -> when (rgTarget.checkedRadioButtonId) {
                R.id.lei_rb_preferences -> output.exportPreferences()
                R.id.lei_rb_history -> output.exportHistory()
                else -> throw Exception()
            }
            R.id.lei_rb_import -> when (rgTarget.checkedRadioButtonId) {
                R.id.lei_rb_preferences -> output.importPreferences()
                R.id.lei_rb_history -> output.importHistory()
                else -> throw Exception()
            }
            else -> throw Exception()
        }
        hide()
    }

    interface ExportImportOutput {
        val externalPath: String
        fun exportPreferences()
        fun exportHistory()
        fun importPreferences()
        fun importHistory()
    }
}