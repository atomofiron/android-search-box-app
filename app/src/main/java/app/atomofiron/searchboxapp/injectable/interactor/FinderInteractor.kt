package app.atomofiron.searchboxapp.injectable.interactor

import app.atomofiron.searchboxapp.injectable.service.FinderService
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.finder.FinderTask
import java.util.*

class FinderInteractor(private val finderService: FinderService) {
    fun search(query: String, where: List<XFile>, ignoreCase: Boolean, useRegex: Boolean, isMultiline: Boolean, forContent: Boolean) {
        finderService.search(query, where, ignoreCase, useRegex, isMultiline, forContent)
    }

    fun stop(uuid: UUID) = finderService.stop(uuid)

    fun drop(task: FinderTask) = finderService.drop(task)
}