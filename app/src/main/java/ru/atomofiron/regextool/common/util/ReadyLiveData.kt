package ru.atomofiron.regextool.common.util

import androidx.lifecycle.MutableLiveData

/** NonNullable value */
class ReadyLiveData<D> : MutableLiveData<D>() {
    override fun getValue(): D = super.getValue()!!

    override fun setValue(value: D) = super.setValue(value)
}