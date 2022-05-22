package app.atomofiron.common.arch

import app.atomofiron.searchboxapp.model.Response
import kotlinx.coroutines.flow.*

interface Recipient {
    val recipient: String get() = this::class.java.simpleName

    fun <D> Flow<Response<D>>.filterForMe() = filter { it.recipient == recipient }.map { it.data }
}