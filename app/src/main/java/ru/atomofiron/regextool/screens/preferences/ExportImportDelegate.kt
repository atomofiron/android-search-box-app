package ru.atomofiron.regextool.screens.preferences

import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetView

class ExportImportDelegate(
        rootView: ViewGroup,
        private val viewModel: PreferenceViewModel
) {

    private val context = rootView.context
    private val exportSheet = BottomSheetView(context).apply {
        setView(R.layout.layout_export_import)
    }
    private val tvPath = exportSheet.findViewById<TextView>(R.id.lei_tv_path)
    private val rgTarget = exportSheet.findViewById<RadioGroup>(R.id.lei_rg_target)
    private val rgAction = exportSheet.findViewById<RadioGroup>(R.id.lei_rg_action)
    private val button = exportSheet.findViewById<Button>(R.id.lei_btn)

    init {
        rootView.addView(exportSheet)

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
        exportSheet.hide()
    }

    fun show() = exportSheet.show()

    fun hide() = exportSheet.hide()
}