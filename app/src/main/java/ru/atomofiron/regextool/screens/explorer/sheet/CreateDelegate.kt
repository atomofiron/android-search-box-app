package ru.atomofiron.regextool.screens.explorer.sheet

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import app.atomofiron.common.util.hideKeyboard
import app.atomofiron.common.util.showKeyboard
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.screens.explorer.ExplorerPresenter
import ru.atomofiron.regextool.custom.view.bottom_sheet.BottomSheetDelegate

class CreateDelegate(private val output: ExplorerPresenter) : BottomSheetDelegate(R.layout.sheet_explorer_create), View.OnClickListener, TextWatcher {
    private val rGroup: RadioGroup get() = bottomSheetView.contentView.findViewById(R.id.explorer_create_rg)
    private val etName: EditText get() = bottomSheetView.contentView.findViewById(R.id.explorer_create_et)
    private val btnConfirm: Button get() = bottomSheetView.contentView.findViewById(R.id.explorer_create_btn)

    private lateinit var dir: XFile
    private lateinit var dirFiles: List<String>

    override fun onViewReady() {
        etName.text.clear()
        etName.addTextChangedListener(this)
        btnConfirm.setOnClickListener(this)
        btnConfirm.isEnabled = false
    }

    override fun onViewShown() = etName.showKeyboard()

    override fun onViewHidden() = etName.hideKeyboard()

    fun show(dir: XFile) {
        this.dir = dir
        dirFiles = dir.children!!.map { it.name }
        show()
    }

    override fun onClick(view: View) {
        hide()
        val directory = when (rGroup.checkedRadioButtonId) {
            R.id.explorer_create_dir -> true
            R.id.explorer_create_file -> false
            else -> throw Exception()
        }
        output.onCreateClick(dir, etName.text.toString(), directory)
    }

    override fun afterTextChanged(s: Editable?) = Unit
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
        val allow = sequence.isNotEmpty() && !dirFiles.contains(sequence.toString())
        btnConfirm.isEnabled = allow
    }
}