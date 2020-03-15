package app.atomofiron.common.util

import androidx.lifecycle.MutableLiveData

/** NonNullable value */
class LateinitLiveData<D> : MutableLiveData<D>() {
    override fun getValue(): D = super.getValue()!!

    override fun setValue(value: D) = super.setValue(value)
}