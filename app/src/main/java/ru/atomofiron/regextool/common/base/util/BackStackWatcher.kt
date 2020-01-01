package ru.atomofiron.regextool.common.base.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ru.atomofiron.regextool.log
import kotlin.reflect.KClass

class BackStackWatcher(private val manager: FragmentManager) : FragmentManager.OnBackStackChangedListener {
    companion object {
        private val mFragments = ArrayList<KClass<out Fragment>>()
        private var backStack: List<KClass<out Fragment>> = ArrayList()

        fun clearFragments() = mFragments.clear()
    }

    init {
        if (mFragments.isEmpty()) {
            mFragments.addAll(manager.fragments.map { it::class })
        }
    }

    override fun onBackStackChanged() {
        log("onBackStackChanged")
        val newBackStack = manager.fragments.map { it::class }
        backStack
                .filter { !newBackStack.contains(it) }
                .forEach { mFragments.remove(it) }
        backStack = newBackStack
    }

    /**
     * add class if that is not contains
     */
    fun add(kClass: KClass<out Fragment>) = addAll(kClass)

    /**
     * add the all classes if no one is contains
     */
    fun addAll(vararg classes: KClass<out Fragment>) {
        when {
            containsAny(*classes) -> {
                throw IllegalArgumentException("Some fragment(s) is already added!")
            }
            else -> {
                mFragments.addAll(classes)
            }
        }
    }

    fun indexOf(kClass: KClass<out Fragment>) = mFragments.lastIndex - mFragments.indexOf(kClass)

    fun isLast(first: KClass<out Fragment>, vararg classes: KClass<out Fragment>): Boolean {
        // classes.size == 2
        // mFragments.size == 5
        var index = mFragments.indexOf(first) // 2
        if (index == -1) {
            return false
        }
        index++ // 3
        classes.forEachIndexed { i, kClass -> // 0 1
            if (kClass != mFragments[i + index]) { // 3 4
                return false
            }
        }
        return true
    }

    private fun containsAny(vararg classes: KClass<out Fragment>): Boolean {
        classes.forEach {
            if (mFragments.contains(it)) {
                return true
            }
        }
        return false
    }
}