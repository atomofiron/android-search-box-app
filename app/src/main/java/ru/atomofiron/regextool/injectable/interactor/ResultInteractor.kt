package ru.atomofiron.regextool.injectable.interactor

import ru.atomofiron.regextool.injectable.service.FinderService
import java.util.*

class ResultInteractor(private val finderService: FinderService) {
    fun stop(uuid: UUID) = finderService.stop(uuid)
}