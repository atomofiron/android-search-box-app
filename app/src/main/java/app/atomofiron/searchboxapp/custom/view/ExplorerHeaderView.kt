package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.ExplorerItemBinder

class ExplorerHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.item_explorer, this)
    }

    private val binder = ExplorerItemBinder(getChildAt(0))
    lateinit var composition: ExplorerItemComposition; private set
    private var item: XFile? = null

    fun setOnItemActionListener(listener: ExplorerItemActionListener) {
        binder.onItemActionListener = listener
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        bind()
    }

    fun onBind(item: XFile? = this.item) {
        this.item = item
        bind()
    }

    private fun bind() {
        when (val item = item) {
            null -> isGone = true
            else -> {
                if (!isVisible) {
                    isVisible = true
                }
                binder.onBind(item)
                binder.bindComposition(composition)
            }
        }
    }
}