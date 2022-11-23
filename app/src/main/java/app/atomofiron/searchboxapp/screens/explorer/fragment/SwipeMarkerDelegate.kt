package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.content.res.Resources
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R

class SwipeMarkerDelegate(resources: Resources) : RecyclerView.OnItemTouchListener {

    private val allowedAria = resources.getDimensionPixelSize(R.dimen.edge_size)
    private var downChild: View? = null
    private var allowed = false
    private var makeChecked: Boolean? = null

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (disallowIntercept) allowed = false
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            downChild = rv.findChildViewUnder(e.x, e.y)
            val end = rv.width - rv.paddingEnd
            allowed = e.x.toInt() in (end - allowedAria)..end
            makeChecked = null
        }
        return allowed
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        downChild?.let { child ->
            downChild = null
            val checkbox = child.getCheckBox()
            makeChecked = !checkbox.isChecked
            checkbox.isChecked = !checkbox.isChecked
        }
        val child = rv.findChildViewUnder(e.x, e.y)
        child ?: return
        val checkbox = child.getCheckBox()
        if (makeChecked == null) {
            makeChecked = !checkbox.isChecked
        }
        if (makeChecked != checkbox.isChecked) {
            checkbox.isChecked = !checkbox.isChecked
        }
    }

    private fun View.getCheckBox() = findViewById<CheckBox>(R.id.item_explorer_cb)
}
