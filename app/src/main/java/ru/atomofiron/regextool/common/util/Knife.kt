package ru.atomofiron.regextool.common.util

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment

class Knife<T : View> {
    private val activity: Activity?
    private val fragment: Fragment?
    private val root: View?

    private val id: Int

    private var viewOrNull: T? = null
        get() {
            if (field == null) {
                field = when {
                    activity != null -> activity.findViewById(id)
                    fragment != null -> fragment.view?.findViewById(id)
                    root != null -> root.findViewById(id)
                    else -> null
                }
            }
            return field
        }

    val view: T get() = viewOrNull!!

    constructor(activity: Activity, id: Int) {
        this.activity = activity
        this.fragment = null
        this.root = null
        this.id = id
    }

    constructor(fragment: Fragment, id: Int) {
        this.activity = null
        this.fragment = fragment
        this.root = null
        this.id = id
    }

    constructor(view: View, id: Int) {
        this.activity = null
        this.fragment = null
        this.root = view
        this.id = id
    }

    operator fun invoke(body: T.() -> Unit) {
        viewOrNull?.run(body)
    }

    operator fun <R> invoke(default: R, body: T.() -> R) = viewOrNull?.run(body) ?: default
}