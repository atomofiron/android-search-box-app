package ru.atomofiron.regextool.screens.preferences

import android.content.Context
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetView

class ExportImportDelegate(
        context: Context,
        private val viewModel: PreferenceViewModel
) {

    val exportSheetView = BottomSheetView(context).apply {
        setView(R.layout.layout_export_import)
    }
    private val tvPath = exportSheetView.findViewById<TextView>(R.id.lei_tv_path)
    private val rgTarget = exportSheetView.findViewById<RadioGroup>(R.id.lei_rg_target)
    private val rgAction = exportSheetView.findViewById<RadioGroup>(R.id.lei_rg_action)
    private val button = exportSheetView.findViewById<Button>(R.id.lei_btn)

    init {
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
        exportSheetView.hide()
    }

    fun show() = exportSheetView.show()

    fun hide() = exportSheetView.hide()
}