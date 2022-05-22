package app.atomofiron.common.util

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment

class Knife<T : View> private constructor(
    private val activity: Activity? = null,
    private val fragment: Fragment? = null,
    private val root: View? = null,
    private val id: Int
) {
    private var viewNullable: T? = null
        get() {
            val detached = field?.isAttachedToWindow != true
            if (field == null || detached) {
                field = when {
                    activity != null -> activity.findViewById(id)
                    fragment != null -> fragment.view?.findViewById(id)
                    root != null -> root.findViewById(id)
                    else -> throw NullPointerException()
                }
            }
            return field
        }

    val view: T get() = viewNullable!!

    constructor(activity: Activity, id: Int) : this(activity, null, null, id)

    constructor(fragment: Fragment, id: Int) : this(null, fragment, null, id)

    constructor(view: View, id: Int) : this(null, null, view, id)

    operator fun invoke(body: T.() -> Unit) = viewNullable?.run(body)

    operator fun <R> invoke(default: R, body: T.() -> R) = viewNullable?.run(body) ?: default
}