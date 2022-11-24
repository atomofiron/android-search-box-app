package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.ExplorerItemBinderImpl

class ExplorerHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.item_explorer, this)
    }

    private val binder = ExplorerItemBinderImpl(getChildAt(0))
    private var composition: ExplorerItemComposition? = null
    private var item: Node? = null
    private var mBottom = 0

    fun setOnItemActionListener(listener: ExplorerItemActionListener) {
        binder.onItemActionListener = listener
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        tryBind()
    }

    fun bind(item: Node?) {
        this.item = item
        tryBind()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (bottom != mBottom) {
            move(mBottom)
        }
    }

    fun move(bottom: Int) {
        mBottom = bottom
        this.top = bottom - measuredHeight
        this.bottom = bottom
    }

    private fun tryBind() {
        val composition = composition ?: return
        val item = item ?: return
        if (!isVisible) isVisible = true
        binder.onBind(item)
        binder.bindComposition(composition)
    }
}