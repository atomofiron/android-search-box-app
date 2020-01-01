package ru.atomofiron.regextool.common.util

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment

class Knife<T : View> {
    private val activity: Activity?
    private val fragment: Fragment?

    private val id: Int

    private var viewOrNull: T? = null
        get() {
            if (field == null) {
                field = when {
                    activity != null -> activity.findViewById(id)
                    fragment != null -> fragment.view?.findViewById(id)
                    else -> null
                }
            }
            return field
        }

    val view: T get() = viewOrNull!!

    constructor(activity: Activity, id: Int) {
        this.activity = activity
        this.fragment = null
        this.id = id
    }

    constructor(fragment: Fragment, id: Int) {
        this.activity = null
        this.fragment = fragment
        this.id = id
    }

    operator fun invoke(body: T.() -> Unit) {
        view.apply(body)
    }
}