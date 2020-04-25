package ru.atomofiron.regextool.injectable.interactor

import ru.atomofiron.regextool.injectable.service.FinderService
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile

class FinderInteractor(private val finderService: FinderService) {
    fun search(query: String, where: List<XFile>, caseSensitive: Boolean, useRegex: Boolean, isMultiline: Boolean, forContent: Boolean) {
        finderService.search(query, where, caseSensitive, useRegex, isMultiline, forContent)
    }
}