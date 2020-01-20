package ru.atomofiron.regextool.common.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

abstract class BaseViewModel<R : BaseRouter>(app: Application) : AndroidViewModel(app) {
    protected abstract val router: R
    protected var provider: ViewModelProvider? = null
    val screenScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Main.immediate)

    open fun onFragmentAttach(fragment: Fragment) {
        router.onFragmentAttach(fragment)
        provider = ViewModelProviders.of(fragment.activity!!)
    }

    open fun onActivityAttach(activity: Activity) {
        router.onActivityAttach(activity as AppCompatActivity)
        provider = ViewModelProviders.of(activity)
    }

    fun onCreate(context: Context, arguments: Bundle?) {
        onCreate(context, Intent().putExtras(arguments ?: Bundle()))
    }

    open fun onCreate(context: Context, intent: Intent) = Unit

    open fun onShow() {
        //router.unblockUi()
    }

    open fun onViewDestroy() = router.onViewDestroy()

    override fun onCleared() {
        screenScope.cancel()
        provider = null
    }

    fun onBackButtonClick() = router.onBack()
}
