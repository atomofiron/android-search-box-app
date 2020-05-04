package ru.atomofiron.regextool.injectable.interactor

import ru.atomofiron.regextool.injectable.service.FinderService
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.model.finder.FinderTask
import java.util.*

class FinderInteractor(private val finderService: FinderService) {
    fun search(query: String, where: List<XFile>, ignoreCase: Boolean, useRegex: Boolean, isMultiline: Boolean, forContent: Boolean) {
        finderService.search(query, where, ignoreCase, useRegex, isMultiline, forContent)
    }

    fun stop(uuid: UUID) = finderService.stop(uuid)

    fun drop(task: FinderTask) = finderService.drop(task)
}