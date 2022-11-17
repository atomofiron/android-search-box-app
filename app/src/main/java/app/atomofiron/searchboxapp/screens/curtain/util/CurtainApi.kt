package app.atomofiron.searchboxapp.screens.curtain.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.atomofiron.common.util.Unique
import app.atomofiron.common.util.Equality
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference
import java.util.*

object CurtainApi {

    interface Controller {
        val requestFrom: String
        val requestId: Int

        fun setAdapter(adapter: Adapter<*>)
        fun showNext(layoutId: Int)
        fun showPrev()
        fun close(immediately: Boolean = false)
        fun showSnackbar(string: String, duration: Int)
        fun showSnackbar(stringId: Int, duration: Int)
        fun showSnackbar(provider: SnackbarProvider)
        fun setCancelable(value: Boolean)
    }

    fun interface SnackbarProvider {
        fun getSnackbar(container: ViewGroup): Snackbar
    }

    abstract class Adapter<H : ViewHolder> : Equality by Unique(Unit) {
        companion object {
            private val unused = Any()
        }
        private val holderList = HashMap<Int, H>()
        private var controllerReference = WeakReference<Controller>(null)
        val holders: Map<Int, H> = holderList
        val controller: Controller? get() = controllerReference.get()
        open val data: Any? = unused

        inline fun <reified B : H> holder(): B? = holders.values.find { it is B } as B?

        inline fun <reified B : H> holder(crossinline action: B.() -> Unit) {
            holder<B>()?.run(action)
        }

        inline fun <R> controller(crossinline action: Controller.() -> R): R? = controller?.run(action)

        inline fun <reified B : H> getHolderProvider(): (action: B.() -> Unit) -> Unit = { action -> holder(action) }

        fun setController(controller: Controller?) {
            when (data) {
                null -> controller?.close(immediately = true)
                else -> {
                    this.controllerReference = WeakReference(controller)
                    controller?.setAdapter(this)
                }
            }
        }

        fun drop(layoutId: Int) {
            holderList.remove(layoutId)
        }

        fun clear() = holderList.clear()

        protected abstract fun getHolder(inflater: LayoutInflater, container: ViewGroup, layoutId: Int): H?

        fun getViewHolder(container: ViewGroup, layoutId: Int): ViewHolder? {
            val inflater = LayoutInflater.from(container.context)
            val holder = holderList[layoutId] ?: getHolder(inflater, container, layoutId)?.apply {
                holderList[layoutId] = this
            }
            return holder
        }
    }

    open class ViewHolder(
        val view: View,
        val isCancelable: Boolean = true,
    )
}