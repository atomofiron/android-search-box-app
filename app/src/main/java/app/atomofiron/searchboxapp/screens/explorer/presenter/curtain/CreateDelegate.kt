package app.atomofiron.searchboxapp.screens.explorer.presenter.curtain

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.CurtainExplorerCreateBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerCurtainMenuDelegate
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets

class CreateDelegate(
    private val output: ExplorerCurtainMenuDelegate,
) {

    fun getView(dir: Node, inflater: LayoutInflater, container: ViewGroup): View {
        val dirFiles = dir.children?.map { it.name } ?: listOf()
        val binding = CurtainExplorerCreateBinding.inflate(inflater, container, false)
        binding.init(dir, dirFiles)
        return binding.root
    }

    private fun CurtainExplorerCreateBinding.init(dir: Node, dirFiles: List<String>) {
        root.applyPaddingInsets(vertical = true)
        root.requestApplyInsets()
        explorerCreateEt.text?.clear()
        explorerCreateEt.addTextChangedListener(ButtonState(dirFiles, explorerCreateDirBtn))
        explorerCreateEt.addTextChangedListener(ButtonState(dirFiles, explorerCreateFileBtn))
        explorerCreateDirBtn.setOnClickListener(ButtonClick(dir, explorerCreateEt))
        explorerCreateFileBtn.setOnClickListener(ButtonClick(dir, explorerCreateEt))
    }

    private inner class ButtonClick(
        private val dir: Node,
        private val editText: EditText,
    ) : View.OnClickListener {
        override fun onClick(view: View) {
            val directory = when (view.id) {
                R.id.explorer_create_dir_btn -> true
                R.id.explorer_create_file_btn -> false
                else -> throw Exception()
            }
            output.onCreateConfirm(dir, editText.text.toString(), directory)
        }
    }

    private inner class ButtonState(
        private val dirFiles: List<String>,
        private val button: Button,
    ) : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
            val allow = sequence.isNotEmpty() && !dirFiles.contains(sequence.toString())
            button.isEnabled = allow
        }
    }
}