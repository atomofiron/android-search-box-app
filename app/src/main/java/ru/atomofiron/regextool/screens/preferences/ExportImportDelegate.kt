package ru.atomofiron.regextool.screens.preferences

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.utils.Shell
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetView

class ExportImportDelegate(
        private val rootView: View,
        private val anchorView: View
) {
    companion object {
        @SuppressLint("InlinedApi")
        private const val LIST_CONTAINER_ID = android.R.id.list_container

        val isAvailable: Boolean get() = App.context.getExternalFilesDir(null) != null
    }

    private val context = rootView.context
    private val externalPath = context.getExternalFilesDir(null)!!.absolutePath
    private val exportSheet = BottomSheetView(context).apply {
        setView(R.layout.layout_export_import)
    }
    private val tvPath = exportSheet.findViewById<TextView>(R.id.lei_tv_path)
    private val rgTarget = exportSheet.findViewById<RadioGroup>(R.id.lei_rg_target)
    private val rgAction = exportSheet.findViewById<RadioGroup>(R.id.lei_rg_action)
    private val button = exportSheet.findViewById<Button>(R.id.lei_btn)

    lateinit var onImportHistoryListener: () -> Unit

    init {
        rootView.findViewById<ViewGroup>(LIST_CONTAINER_ID).addView(exportSheet)

        rgAction.setOnCheckedChangeListener { _, checkedId ->
            val id = when (checkedId) {
                R.id.lei_rb_export -> R.string.export_btn
                R.id.lei_rb_import -> R.string.import_btn
                else -> throw IllegalArgumentException()
            }
            button.setText(id)
        }
        button.setOnClickListener { onButtonClick() }
        tvPath.text = externalPath
    }

    private fun onButtonClick() {
        val packageName = context.packageName
        val toybox = App.pathToybox
        val internalPath = context.applicationInfo.dataDir

        val output = when (rgAction.checkedRadioButtonId) {
            R.id.lei_rb_export -> when (rgTarget.checkedRadioButtonId) {
                R.id.lei_rb_preferences -> exportPreferences(toybox, internalPath, externalPath, packageName)
                R.id.lei_rb_history -> exportHistory(toybox, internalPath, externalPath)
                else -> throw IllegalArgumentException()
            }
            R.id.lei_rb_import -> when (rgTarget.checkedRadioButtonId) {
                R.id.lei_rb_preferences -> importPreferences(toybox, internalPath, externalPath, packageName)
                R.id.lei_rb_history -> importHistory(toybox, internalPath, externalPath).apply {
                    if (success) {
                        onImportHistoryListener.invoke()
                    }
                }
                else -> throw IllegalArgumentException()
            }
            else -> throw IllegalArgumentException()
        }
        exportSheet.hide()
        showOutput(output)
    }

    fun show() = exportSheet.show()

    private fun exportPreferences(toybox: String, internalPath: String, externalPath: String, packageName: String): Shell.Output {
        return Shell.exec("$toybox cp -f $internalPath/shared_prefs/${packageName}_preferences.xml $externalPath/")
    }

    private fun exportHistory(toybox: String, internalPath: String, externalPath: String): Shell.Output {
        return Shell.exec("$toybox cp -f $internalPath/databases/history* $externalPath/")
    }

    private fun importPreferences(toybox: String, internalPath: String, externalPath: String, packageName: String): Shell.Output {
        return Shell.exec("$toybox cp -f $externalPath/${packageName}_preferences.xml $internalPath/shared_prefs/")
    }

    private fun importHistory(toybox: String, internalPath: String, externalPath: String): Shell.Output {
        return Shell.exec("$toybox cp -f $externalPath/history* $internalPath/databases/")
    }

    private fun showOutput(output: Shell.Output) {
        val snackbar = when {
            output.success &&
                    rgTarget.checkedRadioButtonId == R.id.lei_rb_preferences &&
                    rgAction.checkedRadioButtonId == R.id.lei_rb_import -> {
                Snackbar.make(rootView, R.string.successful_with_restart, Snackbar.LENGTH_LONG)
            }
            output.success -> Snackbar.make(rootView, R.string.successful, Snackbar.LENGTH_SHORT)
            else -> {
                Snackbar.make(rootView, R.string.error, Snackbar.LENGTH_SHORT)
                        .apply {
                            if (output.error.isNotEmpty()) {
                                setAction(R.string.more) {
                                    AlertDialog.Builder(context)
                                            .setMessage(output.error)
                                            .show()
                                }
                            }
                        }
            }
        }
        snackbar.setAnchorView(anchorView).show()
    }
}