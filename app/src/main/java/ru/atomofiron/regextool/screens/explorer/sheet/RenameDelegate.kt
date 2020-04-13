package ru.atomofiron.regextool.screens.explorer.sheet

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import app.atomofiron.common.util.showKeyboard
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.bottom_sheet.BottomSheetDelegate
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.model.ExplorerItemComposition
import ru.atomofiron.regextool.screens.explorer.ExplorerPresenter
import ru.atomofiron.regextool.screens.explorer.adapter.ExplorerHolder

class RenameDelegate(private val output: ExplorerPresenter)
    : BottomSheetDelegate(R.layout.sheet_explorer_rename), View.OnClickListener, TextWatcher {
    private val itemView: View get() = bottomSheetView.contentView.findViewById(R.id.explorer_rename_item)
    private val etName: EditText get() = bottomSheetView.contentView.findViewById(R.id.explorer_rename_et)
    private val btnConfirm: Button get() = bottomSheetView.contentView.findViewById(R.id.explorer_rename_btn)

    private lateinit var data: RenameData

    fun show(data: RenameData) {
        this.data = data
        super.show()
    }

    override fun onViewReady() {
        val holder = ExplorerHolder(itemView)
        holder.bind(data.item)
        holder.bindComposition(data.composition)
        holder.removeBackground()
        holder.disableCheckBox()
        etName.setText(data.item.name)
        etName.addTextChangedListener(this)
        btnConfirm.setOnClickListener(this)
        btnConfirm.isEnabled = false
    }

    override fun onViewShown() {
        etName.showKeyboard()

        val dotIndex = data.item.name.lastIndexOf('.')
        if (dotIndex > 0) {
            etName.setSelection(dotIndex)
        }
    }

    override fun onClick(view: View) {
        hide()
        output.onRenameClick(data.item, etName.text.toString())
    }

    data class RenameData(
            val composition: ExplorerItemComposition,
            val item: XFile,
            val items: List<String>
    )

    override fun afterTextChanged(s: Editable?) = Unit
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
        val allow = sequence.isNotEmpty() && !data.items.contains(sequence.toString())
        btnConfirm.isEnabled = allow
    }
}