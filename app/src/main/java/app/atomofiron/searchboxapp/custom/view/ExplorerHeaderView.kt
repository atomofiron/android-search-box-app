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
    lateinit var composition: ExplorerItemComposition; private set
    private var item: Node? = null

    fun setOnItemActionListener(listener: ExplorerItemActionListener) {
        binder.onItemActionListener = listener
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        bind()
    }

    fun onBind(item: Node? = this.item) {
        this.item = item
        bind()
    }

    private fun bind() {
        item?.let {
            if (!isVisible) isVisible = true
            binder.onBind(it)
            binder.bindComposition(composition)
        }
    }
}