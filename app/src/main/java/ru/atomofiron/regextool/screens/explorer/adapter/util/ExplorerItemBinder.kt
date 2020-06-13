package ru.atomofiron.regextool.screens.explorer.adapter.util

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.BallsView
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Tool
import ru.atomofiron.regextool.utils.setVisibility

class ExplorerItemBinder(private val itemView: View) {
    companion object {
        private const val BYTE_LETTER = "B"
    }
    /*
    16842910 enabled
    16842912 checked
    ColorStateList{
        mThemeAttrs=null
        mChangingConfigurations=0
        mStateSpecs=[[16842910, 16842912], [16842910, -16842912], [-16842910, 16842912], [-16842910, -16842912]]
        mColors=[-14845836, -9079435, -6381922, -6381922]
        mDefaultColor=-14845836
    }
     */

    private lateinit var item: XFile

    private val ivIcon = itemView.findViewById<ImageView>(R.id.item_explorer_iv_icon)
    private val tvName = itemView.findViewById<TextView>(R.id.item_explorer_tv_title)
    private val tvDescription = itemView.findViewById<TextView>(R.id.item_explorer_tv_description)
    private val tvDate = itemView.findViewById<TextView>(R.id.item_explorer_tv_date)
    private val tvSize = itemView.findViewById<TextView>(R.id.item_explorer_tv_size)
    private val cbBox = itemView.findViewById<MaterialCheckBox>(R.id.item_explorer_cb)
    private val psProgress = itemView.findViewById<BallsView>(R.id.item_explorer_ps)

    var onItemActionListener: ExplorerItemBinderActionListener? = null

    var rootsAliases = HashMap<String, Int>()

    private val defaultBoxTintList: ColorStateList by lazy(LazyThreadSafetyMode.NONE) { cbBox.buttonTintList!! }
    private val transparentBoxTintList: ColorStateList

    private var onClickListener: ((View) -> Unit) = {
        onItemActionListener?.onItemClick(item)
    }
    private var onLongClickListener: ((View) -> Boolean) = {
        onItemActionListener?.onItemLongClick(item)
        true
    }
    private var onCheckListener: ((View) -> Unit) = { view ->
        view as CheckBox
        onItemActionListener?.onItemCheck(item, view.isChecked)
    }

    init {
        if (cbBox.buttonTintList == null) {
            cbBox.isUseMaterialThemeColors = true
        }

        val stateEnabledChecked = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked)
        val stateDisabledChecked = intArrayOf(-android.R.attr.state_enabled, android.R.attr.state_checked)
        val stateEnabledUnchecked = intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_checked)
        val stateDisabledUnchecked = intArrayOf(-android.R.attr.state_enabled, -android.R.attr.state_checked)
        val colorEnabledChecked = defaultBoxTintList.getColorForState(stateEnabledChecked, Color.RED)
        val colorDisabledChecked = defaultBoxTintList.getColorForState(stateDisabledChecked, Color.RED)
        val colorDisabledUnchecked = defaultBoxTintList.getColorForState(stateDisabledUnchecked, Color.RED)
        val states = arrayOf(stateEnabledChecked, stateDisabledChecked, stateEnabledUnchecked, stateDisabledUnchecked)
        val colors = intArrayOf(colorEnabledChecked, colorDisabledChecked, Color.TRANSPARENT, colorDisabledUnchecked)
        transparentBoxTintList = ColorStateList(states, colors)

        val externalStoragePath = Tool.getExternalStorageDirectory(itemView.context)
        if (externalStoragePath != null) {
            rootsAliases[externalStoragePath] = R.string.internal_storage
        }
        rootsAliases[Const.SDCARD] = R.string.internal_storage
        rootsAliases[Const.ROOT] = R.string.root
    }

    fun onBind(item: XFile) {
        this.item = item

        itemView.setOnClickListener(onClickListener)
        itemView.setOnLongClickListener(onLongClickListener)
        cbBox.setOnClickListener(onCheckListener)

        val image = when {
            !item.isDirectory -> R.drawable.ic_file_circle
            item.children?.isEmpty() == true -> R.drawable.ic_explorer_folder_empty
            else -> R.drawable.ic_explorer_folder
        }
        ivIcon.setImageResource(image)
        ivIcon.alpha = if (item.isDirectory && !item.isCached) .4f else 1f

        val aliasId = rootsAliases[item.completedPath]
        tvName.text = when {
            aliasId != null -> itemView.context.getString(aliasId)
            else -> item.name
        }
        tvName.typeface = if (item.isDirectory) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

        tvSize.text = when {
            item.isFile && item.size.length == 1 -> item.size + BYTE_LETTER
            item.isFile -> item.size
            else -> ""
        }

        cbBox.isChecked = item.isChecked
        cbBox.setVisibility(!item.isDeleting)
        when {
            item.isDeleting -> cbBox.setVisibility(false)
            item.isRoot && !item.isOpened -> cbBox.setVisibility(false, View.INVISIBLE)
            else -> cbBox.setVisibility(true)
        }
        psProgress.setVisibility(item.isDeleting)
    }

    fun bindComposition(composition: ExplorerItemComposition) {
        val string = StringBuilder()
        if (composition.visibleAccess) {
            string.append(item.access).append(" ")
        }
        if (composition.visibleOwner) {
            string.append(item.owner).append(" ")
        }
        if (composition.visibleGroup) {
            string.append(item.group)
        }
        tvDescription.text = string.toString()
        string.clear()
        if (composition.visibleDate) {
            string.append(item.date)
        }
        if (composition.visibleTime) {
            string.append(" ").append(item.time)
        }
        tvDate.text = string.toString()
        tvSize.setVisibility(composition.visibleSize)
        cbBox.buttonTintList = if (composition.visibleBox) defaultBoxTintList else transparentBoxTintList
    }

    fun disableCheckBox() {
        cbBox.isFocusable = false
        cbBox.isClickable = false
    }

    interface ExplorerItemBinderActionListener {
        fun onItemClick(item: XFile)
        fun onItemLongClick(item: XFile)
        fun onItemCheck(item: XFile, isChecked: Boolean)
    }
}