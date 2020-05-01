package ru.atomofiron.regextool.injectable.interactor

import ru.atomofiron.regextool.injectable.service.FinderService
import ru.atomofiron.regextool.injectable.service.ResultService
import ru.atomofiron.regextool.model.finder.FinderResult
import java.util.*

class ResultInteractor(private val resultService: ResultService) {
    fun stop(uuid: UUID) = resultService.stop(uuid)

    fun copyToClipboard(finderResult: FinderResult) = resultService.copyToClipboard(finderResult)
}