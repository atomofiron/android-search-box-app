package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.emitNow
import app.atomofiron.common.util.flow.dataFlow
import app.atomofiron.searchboxapp.model.Response
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import kotlinx.coroutines.flow.Flow

typealias CurtainResponse = Response<CurtainApi.Controller?>

class CurtainChannel {
    private val mutableFlow = dataFlow<CurtainResponse>(single = true)
    var flow: Flow<CurtainResponse> = mutableFlow

    fun emit(response: CurtainResponse) = mutableFlow.emitNow(response)
}