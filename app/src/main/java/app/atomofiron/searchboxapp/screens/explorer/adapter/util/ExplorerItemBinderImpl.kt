package app.atomofiron.searchboxapp.screens.explorer.adapter.util

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.checkbox.MaterialCheckBox
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.BallsView
import app.atomofiron.searchboxapp.model.explorer.DirectoryMediaType
import app.atomofiron.searchboxapp.model.explorer.MediaDirectories
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.Tool

class ExplorerItemBinderImpl(
    private val itemView: View,
) : ExplorerItemBinder {
    companion object {
        private const val BYTE_LETTER = "B"
        private const val SPACE = " "
        private const val EMPTY = ""
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
    private var mediaDirectories: MediaDirectories? = null

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

    override fun onBind(item: XFile) {
        this.item = item

        itemView.setOnClickListener(onClickListener)
        itemView.setOnLongClickListener(onLongClickListener)
        cbBox.setOnClickListener(onCheckListener)

        ivIcon.setImageResource(item.defineIcon())
        ivIcon.alpha = if (item.isDirectory && !item.isCached) Const.ALPHA_DISABLED else Const.ALPHA_ENABLED

        val aliasId = rootsAliases[item.completedPath]
        tvName.text = when (aliasId) {
            null -> item.name
            else -> itemView.context.getString(aliasId)
        }
        tvName.typeface = if (item.isDirectory) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

        tvSize.text = when {
            item.isFile -> item.size + BYTE_LETTER
            else -> EMPTY
        }

        cbBox.isChecked = item.isChecked
        cbBox.isGone = item.isDeleting
        when {
            item.isDeleting -> cbBox.isGone = true
            item.isRoot && !item.isOpened -> cbBox.isInvisible = true
            else -> cbBox.isVisible = true
        }
        psProgress.isVisible = item.isDeleting
    }

    override fun setOnItemActionListener(listener: ExplorerItemActionListener?) {
        onItemActionListener = listener
    }

    override fun bindComposition(composition: ExplorerItemComposition) {
        val string = StringBuilder()
        if (composition.visibleAccess) {
            string.append(item.access).append(SPACE)
        }
        if (composition.visibleOwner) {
            string.append(item.owner).append(SPACE)
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
            string.append(SPACE).append(item.time)
        }
        tvDate.text = string.toString()
        tvSize.isVisible = composition.visibleSize
        cbBox.buttonTintList = if (composition.visibleBox) defaultBoxTintList else transparentBoxTintList
    }

    override fun setMediaDirectories(mediaDirectories: MediaDirectories) {
        this.mediaDirectories = mediaDirectories
    }

    override fun disableClicks() {
        itemView.isFocusable = false
        itemView.isClickable = false
        itemView.isLongClickable = false
        itemView.setOnClickListener(null)
    }

    override fun hideCheckBox() {
        cbBox.isVisible = false
    }

    override fun setGreyBackgroundColor(visible: Boolean) {
        val color = when {
            visible -> ContextCompat.getColor(itemView.context, R.color.item_explorer_background)
            else -> Color.TRANSPARENT
        }
        itemView.setBackgroundColor(color)
    }

    private fun XFile.defineIcon(): Int {
        val empty = children?.isEmpty() == true
        return when {
            !isDirectory -> R.drawable.ic_file_circle
            else -> when (mediaDirectories?.getMediaType(completedPath)) {
                DirectoryMediaType.Android -> when {
                    empty -> R.drawable.ic_explorer_folder_android_empty
                    else -> R.drawable.ic_explorer_folder_android
                }
                DirectoryMediaType.Camera -> when {
                    empty -> R.drawable.ic_explorer_folder_camera_empty
                    else -> R.drawable.ic_explorer_folder_camera
                }
                DirectoryMediaType.Download -> when {
                    empty -> R.drawable.ic_explorer_folder_download_empty
                    else -> R.drawable.ic_explorer_folder_download
                }
                DirectoryMediaType.Movies -> when {
                    empty -> R.drawable.ic_explorer_folder_movies_empty
                    else -> R.drawable.ic_explorer_folder_movies
                }
                DirectoryMediaType.Music -> when {
                    empty -> R.drawable.ic_explorer_folder_music_empty
                    else -> R.drawable.ic_explorer_folder_music
                }
                DirectoryMediaType.Pictures -> when {
                    empty -> R.drawable.ic_explorer_folder_pictures_empty
                    else -> R.drawable.ic_explorer_folder_pictures
                }
                else -> when {
                    empty -> R.drawable.ic_explorer_folder_empty
                    else -> R.drawable.ic_explorer_folder
                }
            }
        }
    }

    interface ExplorerItemBinderActionListener {
        fun onItemClick(item: XFile)
        fun onItemLongClick(item: XFile)
        fun onItemCheck(item: XFile, isChecked: Boolean)
    }
}